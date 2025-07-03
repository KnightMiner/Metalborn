package knightminer.metalborn.core.inventory;

import knightminer.metalborn.core.inventory.MetalInventory.StackHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Collection;
import java.util.List;

/** Shared logic between each inventory variant */
public abstract class MetalInventory<T extends StackHolder<T>> implements IItemHandlerModifiable, INBTSerializable<ListTag> {
  /** Internal inventory, will be set in the constructor */
  protected List<T> inventory;

  @Override
  public int getSlots() {
    return inventory.size();
  }

  @Override
  public int getSlotLimit(int slot) {
    return 1;
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    if (slot >= 0 && slot < inventory.size()) {
      if (stack.isEmpty()) {
        inventory.get(slot).setStack(ItemStack.EMPTY);
      } else {
        // TODO: copy?
        if (stack.getCount() > 1) {
          stack.setCount(1);
        }
        inventory.get(slot).setStack(stack);
      }
    }
  }

  @Override
  public ItemStack getStackInSlot(int slot) {
    if (slot >= 0 && slot < inventory.size()) {
      return inventory.get(slot).stack;
    }
    return ItemStack.EMPTY;
  }

  @Override
  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    // if nothing to insert or at an invalid location, do nothing
    if (stack.isEmpty() || slot < 0 || slot >= inventory.size()) {
      return stack;
    }
    // can only insert if we have nothing and the new thing is a metalmind
    StackHolder<?> current = inventory.get(slot);
    if (current.stack.isEmpty() && isItemValid(slot, stack)) {
      if (!simulate) {
        current.setStack(stack.copyWithCount(1));
      }
      if (stack.getCount() == 1) {
        return ItemStack.EMPTY;
      }
      return stack.copyWithCount(stack.getCount() - 1);
    }
    return stack;
  }

  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    if (amount <= 0 || slot < 0 || slot >= inventory.size()) {
      return ItemStack.EMPTY;
    }
    StackHolder<?> current = inventory.get(slot);
    ItemStack currentStack = current.stack;
    if (currentStack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    if (!simulate) {
      current.setStack(ItemStack.EMPTY);
    }
    return currentStack.copyWithCount(1);
  }

  @Override
  public ListTag serializeNBT() {
    ListTag list = new ListTag();
    for (int i = 0; i < inventory.size(); i++) {
      StackHolder<?> stack = inventory.get(i);
      if (!stack.stack.isEmpty()) {
        CompoundTag tag = stack.serializeNBT();
        tag.putInt("Slot", i);
        list.add(tag);
      }
    }
    return list;
  }

  @Override
  public void deserializeNBT(ListTag list) {
    clear();
    for (int i = 0; i < list.size(); i++) {
      CompoundTag tag = list.getCompound(i);
      int slot = tag.getInt("Slot");
      if (slot >= 0 && slot < inventory.size()) {
        StackHolder<?> current = inventory.get(slot);
        current.deserializeNBT(tag);
      }
    }
    refreshActive();
  }

  /** Attempts to equip the given item */
  public boolean equip(ItemStack stack) {
    for (int i = 0; i < inventory.size(); i++) {
      StackHolder<?> slot = inventory.get(i);
      if (slot.stack.isEmpty() && isItemValid(i, stack)) {
        slot.setStack(stack.split(1));
        return true;
      }
    }
    return false;
  }

  /** Called on death to drop all items */
  public void dropItems(Entity entity, Collection<ItemEntity> drops) {
    for (StackHolder<?> stack : inventory) {
      stack.drop(entity, drops);
    }
  }

  /** Copies all stacks from the other inventory */
  public void copyFrom(MetalInventory<T> other) {
    for (int i = 0; i < inventory.size(); i++) {
      inventory.get(i).copyFrom(other.inventory.get(i));
    }
    refreshActive();
  }

  /** Refreshes the properties of this inventory */
  protected abstract void refreshActive();

  /** Clears the inventory */
  public void clear() {
    for (StackHolder<?> stack : inventory) {
      stack.clear();
    }
  }

  /** Object which holds a stack inside */
  public static class StackHolder<T extends StackHolder<T>> implements INBTSerializable<CompoundTag> {
    ItemStack stack = ItemStack.EMPTY;

    /** Updates the stack in this holder */
    protected void setStack(ItemStack stack) {
      this.stack = stack;
    }

    /** Drops this item */
    protected void drop(Entity entity, Collection<ItemEntity> drops) {
      if (!stack.isEmpty()) {
        ItemEntity itemEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack.copy());
        itemEntity.setDefaultPickUpDelay();
        drops.add(itemEntity);
        setStack(ItemStack.EMPTY);
      }
    }

    /** Updates the contents based on the other stack */
    protected void copyFrom(T other) {
      this.stack = other.stack.copy();
    }

    /** Called on all stacks during read from NBT to reset their data to an empty state */
    protected void clear() {
      stack = ItemStack.EMPTY;
    }

    @Override
    public CompoundTag serializeNBT() {
      return stack.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
      stack = ItemStack.of(tag);
    }
  }
}

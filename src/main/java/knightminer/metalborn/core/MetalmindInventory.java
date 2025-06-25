package knightminer.metalborn.core;

import knightminer.metalborn.item.Metalmind;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;

/** Inventory of metalminds held on the player */
public class MetalmindInventory implements IItemHandlerModifiable, INBTSerializable<ListTag>, ContainerData {
  private final ActiveMetalminds active;
  private final Player player;
  private final List<MetalmindStack> inventory;

  MetalmindInventory(ActiveMetalminds active, Player player) {
    this.active = active;
    this.player = player;
    this.inventory = IntStream.range(0, 10).mapToObj(i -> new MetalmindStack()).toList();
  }

  @Override
  public int getSlots() {
    return inventory.size();
  }

  @Override
  public int getSlotLimit(int slot) {
    return 1;
  }

  @Override
  public boolean isItemValid(int slot, @NotNull ItemStack stack) {
    return stack.isEmpty() || stack.getItem() instanceof Metalmind;
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    if (slot >= 0 && slot < inventory.size()) {
      if (stack.isEmpty()) {
        inventory.get(slot).setStack(ItemStack.EMPTY, Metalmind.EMPTY);
      } else if (stack.getItem() instanceof Metalmind metalmind) {
        // TODO: copy?
        if (stack.getCount() > 1) {
          stack.setCount(1);
        }
        inventory.get(slot).setStack(stack, metalmind);
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
    MetalmindStack current = inventory.get(slot);
    if (current.stack.isEmpty() && stack.getItem() instanceof Metalmind metalmind) {
      if (!simulate) {
        current.setStack(stack.copyWithCount(1), metalmind);
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
    MetalmindStack current = inventory.get(slot);
    ItemStack currentStack = current.stack;
    if (currentStack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    if (!simulate) {
      current.setStack(ItemStack.EMPTY, Metalmind.EMPTY);
    }
    return currentStack.copyWithCount(1);
  }

  @Override
  public ListTag serializeNBT() {
    ListTag list = new ListTag();
    for (int i = 0; i < inventory.size(); i++) {
      MetalmindStack stack = inventory.get(i);
      if (!stack.stack.isEmpty()) {
        CompoundTag tag = stack.stack.serializeNBT();
        tag.putInt("Slot", i);
        tag.putInt("level", stack.getLevel());
        list.add(tag);
      }
    }
    return list;
  }

  @Override
  public void deserializeNBT(ListTag list) {
    for (int i = 0; i < list.size(); i++) {
      CompoundTag tag = list.getCompound(i);
      int slot = tag.getInt("Slot");
      if (slot >= 0 && slot < inventory.size()) {
        ItemStack stack = ItemStack.of(tag);
        MetalmindStack current = inventory.get(slot);
        current.setStack(stack, stack.getItem() instanceof Metalmind m ? m : Metalmind.EMPTY);
        if (!stack.isEmpty()) {
          current.level = tag.getInt("level");
        }
      }
    }
  }

  /** Gets the metalmind slot for the given index */
  MetalmindStack getSlot(int slot) {
    if (slot >= 0 && slot < inventory.size()) {
      return inventory.get(slot);
    }
    throw new IndexOutOfBoundsException("Slot out of bounds: " + slot);
  }

  /** Copies all stacks from the other inventory */
  void copyFrom(MetalmindInventory other) {
    for (int i = 0; i < inventory.size(); i++) {
      inventory.get(i).copyFrom(other.inventory.get(i));
    }
    refreshActive();
  }

  /** Refreshes the metalminds in the active metalminds list */
  void refreshActive() {
    active.clear();
    for (MetalmindStack stack : inventory) {
      stack.refresh();
    }
    active.refresh();
  }

  /** Clears the inventory */
  public void clear() {
    for (MetalmindStack stack : inventory) {
      stack.setStack(ItemStack.EMPTY, Metalmind.EMPTY);
    }
  }

  @Override
  public int get(int i) {
    if (i >= 0 && i < inventory.size()) {
      return inventory.get(i).level;
    }
    return 0;
  }

  @Override
  public void set(int i, int level) {
    if (i >= 0 && i < inventory.size()) {
      inventory.get(i).setLevel(level);
    }
  }

  @Override
  public int getCount() {
    return inventory.size();
  }

  /** Represents a single slot in the metalmind inventory */
  class MetalmindStack {
    private ItemStack stack = ItemStack.EMPTY;
    private Metalmind metalmind = Metalmind.EMPTY;
    private MetalId metal = MetalId.NONE;
    private int level = 0;

    /** Gets the current level */
    int getLevel() {
      return level;
    }

    /** Gets the current metal */
    MetalId getMetal() {
      return metal;
    }

    /** Returns true if this metalmind is usable by the player */
    boolean canUse() {
      return metalmind.canUse(stack, player);
    }

    /** Updates the item stack */
    private void setStack(ItemStack stack, Metalmind metalmind) {
      // if we are currently tapping or storing, stop as something changed
      if (level != 0 && (this.stack.getItem() != stack.getItem() || !this.metal.equals(metalmind.getMetal(stack)))) {
        active.getMetal(this.metal).update(this, 0);
        level = 0;
      }
      this.stack = stack;
      this.metalmind = metalmind;
      this.metal = this.metalmind.getMetal(stack);
    }

    /** Updates the level on the stack */
    public void setLevel(int newLevel) {
      // ensure the metalmind is actually usable to change the level
      if (!metalmind.canUse(stack, player)) {
        newLevel = 0;
      }
      if (newLevel != level) {
        // TODO: let the metalmind choose whether active metals or the power list is used
        active.getMetal(this.metal).update(this, newLevel);
        level = newLevel;
      }
    }

    /**
     * Fills the metalmind by the given amount.
     * @param amount  Amount to add. Always positive.
     * @return Amount that was stored.
     */
    int fill(int amount) {
      if (amount == 0) {
        return 0;
      }
      // can't fill more than our current level
      if (amount > -this.level) {
        amount = -this.level;
      }
      // update the stack
      int used = metalmind.fill(stack, player, amount);
      // if its now empty, stop filling
      if (metalmind.getAmount(stack) >= metalmind.getCapacity(stack)) {
        level = 0;
      }
      return used;
    }

    /**
     * Updates the amount in the metalmind with the passed value
     * @param amount  Amount to remove. Always positive.
     * @return Amount that was drained.
     */
    int drain(int amount) {
      if (amount == 0) {
        return 0;
      }
      // can't drain more than our current level
      if (amount > this.level) {
        amount = this.level;
      }
      // update the stack
      int used = metalmind.drain(stack, player, amount);
      // if its now empty, stop draining
      if (metalmind.getAmount(stack) <= 0) {
        level = 0;
      }
      return used;
    }

    /** Updates the contents based on the other stack */
    private void copyFrom(MetalmindStack other) {
      this.stack = other.stack.copy();
      this.metalmind = other.metalmind;
      this.metal = other.metal;
      this.level = other.level;
    }

    /** Refreshes the stack in the active metalmind list */
    private void refresh() {
      active.getMetal(metal).add(this);
    }
  }
}

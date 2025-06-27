package knightminer.metalborn.block;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Implementation of crafting container from an item stack handler.
 * The container implementation provides a view of just the crafting grid slots, mapping the indices to the appropiate slots in the main inventory.
 */
public class ForgeCraftingInventory extends ForgeInventory implements CraftingContainer {
  private final ForgeBlockEntity parent;
  public ForgeCraftingInventory(ForgeBlockEntity parent) {
    this.parent = parent;
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    validateSlotIndex(slot);
    // if an input slot changed, we need to update our current recipe
    boolean didChange = slot >= ForgeInventory.GRID_START && (stack.isEmpty() || !ItemStack.isSameItemSameTags(stack, this.stacks.get(slot)));
    this.stacks.set(slot, stack);
    if (didChange) {
      parent.resetRecipe();
    }
    parent.setChanged();
  }

  @Override
  protected void onContentsChanged(int slot) {
    if (slot >= ForgeInventory.GRID_SIZE) {
      parent.resetRecipe();
    }
    parent.setChanged();
  }


  /** Crafting container */

  @Override
  public int getWidth() {
    return ForgeInventory.WIDTH;
  }

  @Override
  public int getHeight() {
    return ForgeInventory.HEIGHT;
  }

  @Override
  public int getContainerSize() {
    return ForgeInventory.GRID_SIZE;
  }

  @Override
  public ItemStack getItem(int slot) {
    if (slot < 0 || slot >= ForgeInventory.GRID_SIZE) {
      return ItemStack.EMPTY;
    }
    return getStackInSlot(ForgeInventory.GRID_START + slot);
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    if (slot >= 0 && slot < ForgeInventory.GRID_SIZE) {
      setStackInSlot(ForgeInventory.GRID_START + slot, stack);
    }
  }

  @Override
  public boolean isEmpty() {
    for (int i = 0; i < ForgeInventory.GRID_SIZE; i++) {
      if (!getStackInSlot(ForgeInventory.GRID_START + i).isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public ItemStack removeItem(int slot, int amount) {
    if (amount > 0) {
      ItemStack current = getItem(slot);
      if (!current.isEmpty()) {
        return current.split(amount);
      }
    }
    return ItemStack.EMPTY;
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) {
    ItemStack current = getItem(slot);
    if (!current.isEmpty()) {
      setItem(slot, ItemStack.EMPTY);
      return current;
    }
    return ItemStack.EMPTY;
  }

  @Override
  public void setChanged() {
    parent.setChanged();
  }

  @Override
  public boolean stillValid(Player player) {
    return Container.stillValidBlockEntity(parent, player, 8);
  }

  @Override
  public List<ItemStack> getItems() {
    return stacks.subList(ForgeInventory.GRID_START, stacks.size());
  }

  /** Gets all items in the inventory */
  public List<ItemStack> getAllItems() {
    return stacks;
  }

  @Override
  public void clearContent() {
    getItems().clear();
  }

  @Override
  public void fillStackedContents(StackedContents contents) {
    for (int i = 0; i < ForgeInventory.GRID_SIZE; i++) {
      contents.accountStack(getItem(i));
    }
  }
}

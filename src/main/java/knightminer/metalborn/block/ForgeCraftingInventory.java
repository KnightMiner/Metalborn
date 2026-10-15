package knightminer.metalborn.block;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

import java.util.List;

/**
 * Implementation of crafting container from an item stack handler.
 * The container implementation provides a view of just the crafting grid slots, mapping the indices to the appropiate slots in the main inventory.
 */
public class ForgeCraftingInventory extends ForgeInventory {
  private final ForgeBlockEntity parent;
  private CraftingInput input = null;
  public ForgeCraftingInventory(ForgeBlockEntity parent) {
    this.parent = parent;
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    // replace super call as we can be more lazy about updating the inputs
    validateSlotIndex(slot);
    // if an input slot changed, we need to update our current recipe
    boolean didChange = slot >= ForgeInventory.GRID_START && (stack.isEmpty() || !ItemStack.isSameItemSameComponents(stack, this.stacks.get(slot)));
    this.stacks.set(slot, stack);
    if (didChange) {
      input = null;
      parent.resetRecipe();
    }
    parent.setChanged();
  }

  @Override
  protected void onContentsChanged(int slot) {
    if (slot >= ForgeInventory.GRID_SIZE) {
      input = null;
      parent.resetRecipe();
    }
    parent.setChanged();
  }


  /* Crafting input */

  /** Checks if the crafting grid is empty */
  public boolean isInputEmpty() {
    for (int i = 0; i < ForgeInventory.GRID_SIZE; i++) {
      if (!getStackInSlot(ForgeInventory.GRID_START + i).isEmpty()) {
        return false;
      }
    }
    return true;
  }
  /** Gets items in the crafting inventory */
  protected List<ItemStack> getInputItems() {
    return stacks.subList(ForgeInventory.GRID_START, stacks.size());
  }

  /** Gets all items in the inventory */
  public List<ItemStack> getAllItems() {
    return stacks;
  }

  /** Gets the crafting input, computing it if not yet computed. */
  public CraftingInput craftingInput() {
    if (input == null) {
      input = CraftingInput.of(ForgeInventory.WIDTH, ForgeInventory.HEIGHT, getInputItems());
    }
    return input;
  }
}

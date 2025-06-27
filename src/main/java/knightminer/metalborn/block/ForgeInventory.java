package knightminer.metalborn.block;

import knightminer.metalborn.core.Registration;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemStackHandler;

public class ForgeInventory extends ItemStackHandler {
  // slots
  public static final int RESULT_SLOT = 0;
  public static final int FUEL_SLOT = 1;
  public static final int GRID_START = 2;
  public static final int WIDTH = 2;
  public static final int HEIGHT = 2;
  public static final int GRID_SIZE = WIDTH * HEIGHT;
  public static final int SIZE = WIDTH * HEIGHT + 2;

  public ForgeInventory() {
    super(SIZE);
  }

  @Override
  public void setSize(int size) {
    if (size != SIZE) {
      throw new UnsupportedOperationException("Forge inventory size cannot be changed");
    }
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    if (slot == ForgeInventory.RESULT_SLOT) {
      return false;
    }
    if (slot == ForgeInventory.FUEL_SLOT) {
      return ForgeHooks.getBurnTime(stack, Registration.FORGE_RECIPE.get()) > 0;
    }
    return true;
  }
}

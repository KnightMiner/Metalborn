package knightminer.metalborn.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public abstract class BaseMenu extends AbstractContainerMenu {
  protected BaseMenu(@Nullable MenuType<?> menuType, int id) {
    super(menuType, id);
  }

  /** Adds the player inventory rows */
  protected void addPlayerInventory(Inventory inventory, int y) {
    // inventory rows
    for (int r = 0; r < 3; r++) {
      for (int c = 0; c < 9; c++) {
        this.addSlot(new Slot(inventory, c + r * 9 + 9, 8 + c * 18, y + r * 18));
      }
    }
    // hotbar
    for (int c = 0; c < 9; c++) {
      this.addSlot(new Slot(inventory, c, 8 + c * 18, y + 58));
    }
  }

  /** Adds the player inventory rows with a slot marked read only. */
  @SuppressWarnings("SameParameterValue")
  protected void addPlayerInventory(Inventory inventory, int y, int reservedSlot) {
    // inventory rows
    for (int r = 0; r < 3; r++) {
      for (int c = 0; c < 9; c++) {
        int index = c + r * 9 + 9;
        if (index == reservedSlot) {
          this.addSlot(new ReadOnlySlot(inventory, index, 8 + c * 18, y + r * 18));
        } else {
          this.addSlot(new Slot(inventory, index, 8 + c * 18, y + r * 18));
        }
      }
    }
    // hotbar
    for (int c = 0; c < 9; c++) {
      if (c == reservedSlot) {
        this.addSlot(new ReadOnlySlot(inventory, c, 8 + c * 18, y + 58));
      } else {
        this.addSlot(new Slot(inventory, c, 8 + c * 18, y + 58));
      }
    }
  }
}

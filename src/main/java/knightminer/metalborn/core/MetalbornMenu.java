package knightminer.metalborn.core;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.inventory.MetalmindInventory;
import knightminer.metalborn.core.inventory.MetalmindInventory.MetalmindStack;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.inventory.SmartItemHandlerSlot;

import java.util.List;

/** Menu for managing metalminds */
public class MetalbornMenu extends AbstractContainerMenu {
  private static final int PLAYER_INVENTORY_START = 10;

  @Nullable
  private final MetalmindInventory metalminds;
  private final List<Slot> metalmindSlots;
  protected MetalbornMenu(@Nullable MenuType<?> type, int id, Inventory inventory) {
    super(type, id);
    if (MetalbornCapability.getData(inventory.player) instanceof MetalbornCapability capability) {
      metalminds = capability.getMetalminds();
      Item bracer = Registration.BRACER.get();
      Item ring = Registration.RING.get();
      metalmindSlots = List.of(
        addSlot(new MetalmindSlot(metalminds, bracer, 0,  26, 57)),
        addSlot(new MetalmindSlot(metalminds, bracer, 1, 134, 57)),
        addSlot(new MetalmindSlot(metalminds, ring,   2,  16,  9)),
        addSlot(new MetalmindSlot(metalminds, ring,   3,  36,  9)),
        addSlot(new MetalmindSlot(metalminds, ring,   4, 124,  9)),
        addSlot(new MetalmindSlot(metalminds, ring,   5, 144,  9)),
        addSlot(new MetalmindSlot(metalminds, ring,   6,  16, 33)),
        addSlot(new MetalmindSlot(metalminds, ring,   7,  36, 33)),
        addSlot(new MetalmindSlot(metalminds, ring,   8, 124, 33)),
        addSlot(new MetalmindSlot(metalminds, ring,   9, 144, 33))
      );

      // inventory rows
      for (int r = 0; r < 3; r++) {
        for (int c = 0; c < 9; c++) {
          this.addSlot(new Slot(inventory, c + r * 9 + 9, 8 + c * 18, 84 + r * 18));
        }
      }
      // hotbar
      for (int c = 0; c < 9; c++) {
        this.addSlot(new Slot(inventory, c, 8 + c * 18, 142));
      }
      // other data slots
      addDataSlots(metalminds);
    } else {
      Metalborn.LOG.error("Missing capability for {}, this should not be possible", inventory.player.getGameProfile().getName());
      this.metalminds = null;
      this.metalmindSlots = List.of();
    }
  }

  public MetalbornMenu(int id, Inventory inventory) {
    this(Registration.MENU.get(), id, inventory);
  }

  /** Opens the menu on the client */
  public static MetalbornMenu forClient(int id, Inventory inventory, FriendlyByteBuf buffer) {
    MetalId ferringType = new MetalId(buffer.readResourceLocation());
    MetalbornData data = MetalbornCapability.getData(inventory.player);
    data.clear();
    data.setFerringType(ferringType);
    return new MetalbornMenu(id, inventory);
  }

  @Override
  public boolean stillValid(Player pPlayer) {
    return true;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    ItemStack result = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);
    if (slot.hasItem()) {
      ItemStack slotStack = slot.getItem();
      result = slotStack.copy();
      int end = this.slots.size();
      // if its a metalmind slot, move to inventory
      if (index < PLAYER_INVENTORY_START) {
        if (!this.moveItemStackTo(slotStack, PLAYER_INVENTORY_START, end, true)) {
          return ItemStack.EMPTY;
        }
      // move player inventory into metalminds
      } else if (!this.moveItemStackTo(slotStack, 0, PLAYER_INVENTORY_START, false)) {
        return ItemStack.EMPTY;
      }
      // if we moved the whole stack, clear the slot
      if (slotStack.isEmpty()) {
        slot.set(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }
      // if we moved nothing, give up
      if (slotStack.getCount() == result.getCount()) {
        return ItemStack.EMPTY;
      }
    }
    return result;
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    // on clicking a plus or minus button, update the tapping/storing value
    if (id < 20 && metalminds != null) {
      // metalmind inventory index
      int slot = id / 2;
      // if true, we hit the plus button (right side)
      boolean plus = id % 2 == 1;
      MetalmindStack stack = metalminds.getSlot(slot);
      int current = stack.getLevel();
      if (plus) {
        stack.setLevel(current > 0 ? 0 : 1);
      } else {
        stack.setLevel(current < 0 ? 0 : -1);
      }
      return true;
    }
    return false;
  }


  /* Screen helpers */

  /** Gets all metalmind slots */
  public List<Slot> getMetalmindSlots() {
    return metalmindSlots;
  }

  /** Gets the level of the given metalmind */
  public int getMetalmindLevel(int slot) {
    if (slot >= 0 && metalminds != null && slot < metalmindSlots.size()) {
      return metalminds.getSlot(slot).getLevel();
    }
    return 0;
  }

  /** Gets the metal for the given slot */
  public MetalId getMetal(int slot) {
    if (slot >= 0 && metalminds != null && slot < metalmindSlots.size()) {
      return metalminds.getSlot(slot).getMetal();
    }
    return MetalId.NONE;
  }

  /** Gets the metal for the given slot */
  public boolean canUse(int slot) {
    if (slot >= 0 && metalminds != null && slot < metalmindSlots.size()) {
      return metalminds.getSlot(slot).canUse();
    }
    return false;
  }

  /** Slot filtered to a specific metalmind type */
  private static class MetalmindSlot extends SmartItemHandlerSlot {
    private final Item filter;
    public MetalmindSlot(IItemHandler itemHandler, Item filter, int index, int xPosition, int yPosition) {
      super(itemHandler, index, xPosition, yPosition);
      this.filter = filter;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
      return stack.is(filter);
    }
  }
}

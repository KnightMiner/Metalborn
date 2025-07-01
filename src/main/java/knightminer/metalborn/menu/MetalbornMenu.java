package knightminer.metalborn.menu;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornCapability;
import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.core.inventory.MetalmindInventory;
import knightminer.metalborn.core.inventory.MetalmindInventory.MetalmindStack;
import knightminer.metalborn.core.inventory.SpikeInventory;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.inventory.SmartItemHandlerSlot;

import java.util.List;

/** Menu for managing metalminds */
public class MetalbornMenu extends BaseMenu {
  private static final int PLAYER_INVENTORY_START = 10 + 4;

  @Nullable
  private final MetalmindInventory metalminds;
  private final List<Slot> metalmindSlots;
  protected MetalbornMenu(@Nullable MenuType<?> type, int id, Inventory inventory) {
    super(type, id);
    if (MetalbornData.getData(inventory.player) instanceof MetalbornCapability capability) {
      metalminds = capability.getMetalminds();
      metalmindSlots = List.of(
        addSlot(new SmartItemHandlerSlot(metalminds, 0,  26, 57)),
        addSlot(new SmartItemHandlerSlot(metalminds, 1, 134, 57)),
        addSlot(new SmartItemHandlerSlot(metalminds, 2,  16,  9)),
        addSlot(new SmartItemHandlerSlot(metalminds, 3,  36,  9)),
        addSlot(new SmartItemHandlerSlot(metalminds, 4, 124,  9)),
        addSlot(new SmartItemHandlerSlot(metalminds, 5, 144,  9)),
        addSlot(new SmartItemHandlerSlot(metalminds, 6,  16, 33)),
        addSlot(new SmartItemHandlerSlot(metalminds, 7,  36, 33)),
        addSlot(new SmartItemHandlerSlot(metalminds, 8, 124, 33)),
        addSlot(new SmartItemHandlerSlot(metalminds, 9, 144, 33))
      );
      SpikeInventory spikes = capability.getSpikes();
      addSlot(new SmartItemHandlerSlot(spikes, 0, 71, 26));
      addSlot(new SmartItemHandlerSlot(spikes, 1, 89, 26));
      addSlot(new SmartItemHandlerSlot(spikes, 2, 71, 44));
      addSlot(new SmartItemHandlerSlot(spikes, 3, 89, 44));

      // inventory rows
      addPlayerInventory(inventory, 84);

      // other data slots
      addDataSlots(metalminds);
    } else {
      Metalborn.LOG.error("Missing capability for {}, this should not be possible", inventory.player.getGameProfile().getName());
      this.metalminds = null;
      this.metalmindSlots = List.of();
    }
  }

  public MetalbornMenu(int id, Inventory inventory) {
    this(Registration.METALBORN_MENU.get(), id, inventory);
  }

  /** Opens the menu on the client */
  public static MetalbornMenu forClient(int id, Inventory inventory, FriendlyByteBuf buffer) {
    MetalId ferringType = new MetalId(buffer.readResourceLocation());
    MetalbornData data = MetalbornData.getData(inventory.player);
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
      // index is doubled for this button click
      MetalmindStack stack = metalminds.getSlot(id / 2);
      if (stack != null) {
        int current = stack.getLevel();
        // last bit is set to indicate plus or minus button
        if (id % 2 == 1) {
          stack.setLevel(current > 0 ? 0 : 1);
        } else {
          stack.setLevel(current < 0 ? 0 : -1);
        }
        return true;
      }
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
    if (metalminds != null) {
      MetalmindStack stack = metalminds.getSlot(slot);
      if (stack != null) {
        return stack.getLevel();
      }
    }
    return 0;
  }

  /** Gets the metal for the given slot */
  public Component getStores(int slot) {
    if (metalminds != null) {
      MetalmindStack stack = metalminds.getSlot(slot);
      if (stack != null) {
        return stack.getStores();
      }
    }
    return MetalId.NONE.getStores();
  }

  /** Gets the metal for the given slot */
  public boolean canUse(int slot) {
    if (metalminds != null) {
      MetalmindStack stack = metalminds.getSlot(slot);
      if (stack != null) {
        return stack.canUse();
      }
    }
    return false;
  }
}

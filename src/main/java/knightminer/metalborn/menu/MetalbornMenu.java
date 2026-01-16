package knightminer.metalborn.menu;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornCapability;
import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.core.inventory.MetalmindInventory;
import knightminer.metalborn.core.inventory.MetalmindInventory.MetalmindStack;
import knightminer.metalborn.core.inventory.SpikeInventory;
import knightminer.metalborn.item.SatchelItem.SatchelInventory;
import knightminer.metalborn.item.SatchelItem.SatchelType;
import knightminer.metalborn.item.metalmind.Metalmind.Usable;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.inventory.EmptyItemHandler;
import slimeknights.mantle.inventory.SmartItemHandlerSlot;

import java.util.List;

/** Menu for managing metalminds */
public class MetalbornMenu extends BaseMenu {
  /** Index of the first slot after the end of the metalminds. */
  private static final int METAL_END = 10 + 4;

  @Nullable
  private final MetalmindInventory metalminds;
  private final List<Slot> metalmindSlots;
  private final ItemStack satchelStack;
  /** Reference to the satchel inventory, for clearing the satchel stack on inventory close */
  private final IItemHandler satchelInventory;
  @Nullable
  private final SatchelType satchelType;
  /** Index in the player inventory of the slot containing the satchel. -1 if no satchel. */
  private final int satchelSlot;
  /** Index in the slots array of the slot containing the satchel. -1 if no satchel. */
  private final int highlightSlot;
  /** First slot in the player inventory */
  private final int playerStart;
  protected MetalbornMenu(@Nullable MenuType<?> type, int id, Inventory inventory, ItemStack satchelStack, IItemHandler satchelInventory, @Nullable SatchelType satchelType, int satchelSlot) {
    super(type, id);
    this.satchelStack = satchelStack;
    this.satchelInventory = satchelInventory;
    this.satchelType = satchelType;
    this.satchelSlot = satchelSlot;
    if (MetalbornData.getData(inventory.player) instanceof MetalbornCapability capability) {
      metalminds = capability.getMetalminds();
      metalmindSlots = List.of(
        addSlot(new SmartItemHandlerSlot(metalminds, 0,  26, 57)),
        addSlot(new SmartItemHandlerSlot(metalminds, 1, 134, 57)),
        addSlot(new SmartItemHandlerSlot(metalminds, 2,  16,  9)),
        addSlot(new SmartItemHandlerSlot(metalminds, 3,  36,  9)),
        addSlot(new SmartItemHandlerSlot(metalminds, 4,  16, 33)),
        addSlot(new SmartItemHandlerSlot(metalminds, 5,  36, 33)),
        addSlot(new SmartItemHandlerSlot(metalminds, 6, 124,  9)),
        addSlot(new SmartItemHandlerSlot(metalminds, 7, 144,  9)),
        addSlot(new SmartItemHandlerSlot(metalminds, 8, 124, 33)),
        addSlot(new SmartItemHandlerSlot(metalminds, 9, 144, 33))
      );
      SpikeInventory spikes = capability.getSpikes();
      addSlot(new SmartItemHandlerSlot(spikes, 0, 71, 26));
      addSlot(new SmartItemHandlerSlot(spikes, 1, 89, 26));
      addSlot(new SmartItemHandlerSlot(spikes, 2, 71, 44));
      addSlot(new SmartItemHandlerSlot(spikes, 3, 89, 44));

      // add satchel slots if passed
      if (satchelType != null) {
        int slots = satchelType.getSize();
        // first row
        int max = Math.min(slots, 9);
        for (int i = 0; i < max; i++) {
          addSlot(new SmartItemHandlerSlot(satchelInventory, i, 8 + 18 * i, 92));
        }
        // second row
        if (slots > 9) {
          max = Math.min(slots - 9, 9);
          for (int i = 0; i < max; i++) {
            addSlot(new SmartItemHandlerSlot(satchelInventory, i + 9, 8 + 18 * i, 110));
          }
        }
        // inventory rows without satchel
        playerStart = this.slots.size();
        addPlayerInventory(inventory, slots > 9 ? 140 : 122, satchelSlot);

        // determine which slot in the overall slots array with the satchel to highlight
        if (satchelSlot < 9) {
          // hotbar slots are after all our slots, and after the main inventory 27
          highlightSlot = playerStart + satchelSlot + 27;
        } else if (satchelSlot < Inventory.INVENTORY_SIZE) {
          // main inventory 27 is after our slots, but the index is 9 too high (hotbar)
          highlightSlot = playerStart + satchelSlot - 9;
        } else {
          // hotbar does not show in our inventory
          this.highlightSlot = -1;
        }
      } else {
        this.playerStart = this.slots.size();
        // inventory rows without satchel
        addPlayerInventory(inventory, 92);
        this.highlightSlot = -1;
      }

      // other data slots
      addDataSlots(metalminds);
    } else {
      Metalborn.LOG.error("Missing capability for {}, this should not be possible", inventory.player.getGameProfile().getName());
      this.metalminds = null;
      this.metalmindSlots = List.of();
      this.highlightSlot = -1;
      this.playerStart = 0;
    }
  }

  /** Opens the menu for a satchel */
  protected MetalbornMenu(int id, Inventory inventory, ItemStack stack, IItemHandler itemHandler, @Nullable SatchelType satchelType, int satchelSlot) {
    this(Registration.METALBORN_MENU.get(), id, inventory, stack, itemHandler, satchelType, satchelSlot);
  }

  /** Opens the menu without a satchel from the keybinding */
  public MetalbornMenu(int id, Inventory inventory) {
    this(id, inventory, ItemStack.EMPTY, EmptyItemHandler.INSTANCE, null, -1);
  }

  /** Opens the menu for a satchel */
  public MetalbornMenu(int id, Inventory inventory, ItemStack stack, SatchelType satchelType, int satchelSlot) {
    this(id, inventory, stack, stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(EmptyItemHandler.INSTANCE), satchelType, satchelSlot);
  }

  /** Opens the menu on the client */
  public static MetalbornMenu forClient(int id, Inventory inventory, FriendlyByteBuf buffer) {
    MetalId ferringType = new MetalId(buffer.readResourceLocation());
    MetalbornData data = MetalbornData.getData(inventory.player);
    data.clear();
    data.setFerringType(ferringType);

    // find our satchel if needed
    int satchelSlot = buffer.readByte();
    ItemStack satchelStack = ItemStack.EMPTY;
    IItemHandler satchelInventory = EmptyItemHandler.INSTANCE;
    SatchelType satchelType = null;
    if (satchelSlot >= 0) {
      satchelStack = inventory.getItem(satchelSlot);
      satchelType = buffer.readEnum(SatchelType.class);
      // while we could fetch the cap from the satchel, it may not have synced properly, so just construct a dummy handler for the client
      satchelInventory = new SatchelInventory(satchelType);
    }
    return new MetalbornMenu(id, inventory, satchelStack, satchelInventory, satchelType, satchelSlot);
  }

  @Override
  public boolean stillValid(Player player) {
    if (satchelSlot >= 0) {
      // ensure satchel is not shrunk or dropped
      return !satchelStack.isEmpty() && player.getInventory().getItem(satchelSlot) == satchelStack;
    }
    return true;
  }

  @Override
  public void removed(Player player) {
    super.removed(player);
    // if a mist satchel is now empty, remove it from the inventory
    // though make sure its still in the proper slot
    if (satchelType == SatchelType.MIST && player.isAlive() && player.getInventory().getItem(satchelSlot) == satchelStack
        && player instanceof ServerPlayer serverPlayer && !serverPlayer.hasDisconnected()) {
      // empty check
      for (int i = 0; i < satchelInventory.getSlots(); i++) {
        if (!satchelInventory.getStackInSlot(i).isEmpty()) {
          return;
        }
      }
      // clear the empty mist satchel
      player.getInventory().setItem(satchelSlot, ItemStack.EMPTY);
    }
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
      if (index < METAL_END) {
        if (!this.moveItemStackTo(slotStack, METAL_END, end, true)) {
          return ItemStack.EMPTY;
        }
      // move satchel into metalminds, then inventory. Note this will be skipped if player start is metal end
      } else if (index < playerStart) {
        if (!this.moveItemStackTo(slotStack, 0, METAL_END, false)
            && !this.moveItemStackTo(slotStack, playerStart, end, true)) {
          return ItemStack.EMPTY;
        }
      // move hotbar to metalminds, then satchel, then inventory
      } else if (index >= playerStart + 27) {
        if (!this.moveItemStackTo(slotStack, 0, playerStart + 27, false)) {
          return ItemStack.EMPTY;
        }
      // move player inventory into metalminds, then satchel, then hotbar
      } else if (!this.moveItemStackTo(slotStack, 0, playerStart, false)
          && !this.moveItemStackTo(slotStack, playerStart + 27, end, false)) {
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
    if (metalminds != null) {
      // metalminds button stops all action
      if (id == 40) {
        metalminds.stopAll();
        return true;
      }
      // other buttons start or stop specific actions
      if (id < 40) {
        // index is doubled for this button click
        MetalmindStack stack = metalminds.getSlot(id >> 2);
        if (stack != null) {
          int current = stack.getLevel();
          // last bit is set to indicate plus or minus button
          int newLevel;
          if (id % 2 == 1) {
            newLevel = current > 0 ? 0 : 1;
          } else {
            newLevel = current < 0 ? 0 : -1;
          }
          // if the shift bit is set, update all matching metalminds
          if ((id & 2) > 0) {
            stack.setMatchingLevel(newLevel);
          } else {
            stack.setLevel(newLevel);
          }
          return true;
        }
      }
    }
    return false;
  }


  /* Screen helpers */

  /** Gets the current satchel variant, or null if no satchel present */
  @Nullable
  public SatchelType getSatchelType() {
    return satchelType;
  }

  /** Gets the index of the slot containing the satchel, or -1 if not present. */
  public int getSatchelSlot() {
    return satchelSlot;
  }

  /** Gets the read only slot index from {@link #slots} containing the satchel to render the highlight. */
  public int getHighlightSlot() {
    return highlightSlot;
  }

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
  public Usable canUse(int slot) {
    if (metalminds != null) {
      MetalmindStack stack = metalminds.getSlot(slot);
      if (stack != null) {
        return stack.canUse();
      }
    }
    return Usable.NEVER;
  }
}

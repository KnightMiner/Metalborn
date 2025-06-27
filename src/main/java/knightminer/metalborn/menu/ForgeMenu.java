package knightminer.metalborn.menu;

import knightminer.metalborn.block.ForgeBlockEntity;
import knightminer.metalborn.block.ForgeInventory;
import knightminer.metalborn.core.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.inventory.SmartItemHandlerSlot;

import static knightminer.metalborn.block.ForgeInventory.FUEL_SLOT;
import static knightminer.metalborn.block.ForgeInventory.GRID_SIZE;
import static knightminer.metalborn.block.ForgeInventory.GRID_START;
import static knightminer.metalborn.block.ForgeInventory.HEIGHT;
import static knightminer.metalborn.block.ForgeInventory.RESULT_SLOT;
import static knightminer.metalborn.block.ForgeInventory.WIDTH;

/** Menu for the forge */
public class ForgeMenu extends BaseMenu {
  public static final int PLAYER_INVENTORY_START = ForgeInventory.SIZE;

  @Nullable
  private final ForgeBlockEntity blockEntity;
  private final ContainerData data;

  protected ForgeMenu(int id, Inventory playerInventory, @Nullable ForgeBlockEntity blockEntity, IItemHandlerModifiable itemHandler, ContainerData data) {
    super(Registration.FORGE_MENU.get(), id);
    this.blockEntity = blockEntity;
    this.data = data;

    // result slot, 126
    addSlot(new ResultSlot(itemHandler, 126, 35, playerInventory.player, blockEntity));
    // fuel slot
    addSlot(new ForgeSlot(itemHandler, ForgeInventory.FUEL_SLOT, 26, 44));
    // input slots
    for (int y = 0; y < HEIGHT; y++) {
      for (int x = 0; x < WIDTH; x++) {
        addSlot(new ForgeSlot(itemHandler, ForgeInventory.GRID_START + y * WIDTH + x, 48 + x * 18, 26 + y * 18));
      }
    }

    // player inventory
    addPlayerInventory(playerInventory, 84);
    // progress bars
    addDataSlots(data);
  }

  public ForgeMenu(int id, Inventory playerInventory, @Nullable FriendlyByteBuf ignoredBuffer) {
    this(id, playerInventory, null, new ForgeInventory(), new SimpleContainerData(ForgeBlockEntity.DATA_SLOT_COUNT));
  }

  public ForgeMenu(int id, Inventory playerInventory, ForgeBlockEntity forge) {
    this(id, playerInventory, forge, forge.getInventory(), forge);
  }

  /**
   * Gets the recipe progress
   */
  public int getRecipeProgress() {
    int progress = this.data.get(ForgeBlockEntity.INDEX_RECIPE_PROGRESS);
    int duration = this.data.get(ForgeBlockEntity.INDEX_RECIPE_DURATION);
    return duration != 0 && progress != 0 ? progress * 24 / duration : 0;
  }

  /** Checks if we have fuel */
  public boolean isFueled() {
    return this.data.get(ForgeBlockEntity.INDEX_FUEL) > 0;
  }

  /** Gets the fuel amount */
  public int getFuelAmount() {
    int duration = this.data.get(ForgeBlockEntity.INDEX_FUEL_DURATION);
    if (duration == 0) {
      duration = 200;
    }
    return this.data.get(ForgeBlockEntity.INDEX_FUEL) * 13 / duration;
  }

  @Override
  public boolean stillValid(Player player) {
    return blockEntity == null || Container.stillValidBlockEntity(blockEntity, player, 8);
  }

  @Override
  public ItemStack quickMoveStack(Player pPlayer, int index) {
    ItemStack result = ItemStack.EMPTY;
    Slot slot = slots.get(index);
    if (slot.hasItem()) {
      ItemStack slotStack = slot.getItem();
      result = slotStack.copy();

      // result slot moves into the player inventory
      if (index == RESULT_SLOT) {
        if (!this.moveItemStackTo(slotStack, PLAYER_INVENTORY_START, slots.size(), true)) {
          return ItemStack.EMPTY;
        }
        slot.onQuickCraft(slotStack, result);
      // fuel and input slots move to player inventory
      } else if (index < PLAYER_INVENTORY_START) {
        if (!this.moveItemStackTo(slotStack, PLAYER_INVENTORY_START, slots.size(), false)) {
          return ItemStack.EMPTY;
        }
      } else {
        // player inventory moves based on the item type
        // fuel goes in the fuel slot
        if (ForgeHooks.getBurnTime(slotStack, Registration.FORGE_RECIPE.get()) > 0) {
          if (!this.moveItemStackTo(slotStack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
            return ItemStack.EMPTY;
          }
        }
        // recipes are too complex to bother with can smelt, so just send the rest to the input slot
        if (!this.moveItemStackTo(slotStack, GRID_START, GRID_START + GRID_SIZE, false)) {
          return ItemStack.EMPTY;
        }
      }
      // if we moved the whole stack, clear the slot
      if (slotStack.isEmpty()) {
        slot.setByPlayer(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }
      // if we moved nothing, give up
      if (slotStack.getCount() == result.getCount()) {
        return ItemStack.EMPTY;
      }

      slot.onTake(pPlayer, slotStack);
    }
    return result;
  }

  /** Slot fixing pickup for JEI */
  private static class ForgeSlot extends SmartItemHandlerSlot {
    public ForgeSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
      super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
      // forge item handler returns false for empty stacks, interpreting "may pickup" as "may currently pickup"
      // JEI expects it to be a global "may ever pickup"
      return true;
    }
  }

  /** Logic to grant XP when taking the result */
  private static class ResultSlot extends ForgeSlot {
    private final Player player;
    @Nullable
    private final ForgeBlockEntity blockEntity;
    private int removeCount;
    public ResultSlot(IItemHandler itemHandler, int xPosition, int yPosition, Player player, @Nullable ForgeBlockEntity blockEntity) {
      super(itemHandler, ForgeInventory.RESULT_SLOT, xPosition, yPosition);
      this.player = player;
      this.blockEntity = blockEntity;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
      return false;
    }

    @Override
    public ItemStack remove(int amount) {
      if (hasItem()) {
        removeCount += Math.min(amount, getItem().getCount());
      }
      return super.remove(amount);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
      this.checkTakeAchievements(stack);
      super.onTake(player, stack);
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
      removeCount += amount;
      checkTakeAchievements(stack);
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack) {
      stack.onCraftedBy(player.level(), player, removeCount);
      if (blockEntity != null && player instanceof ServerPlayer server) {
        blockEntity.awardUsedRecipesAndPopExperience(server);
      }
      removeCount = 0;
      // TODO: smelted event?
    }
  }
}

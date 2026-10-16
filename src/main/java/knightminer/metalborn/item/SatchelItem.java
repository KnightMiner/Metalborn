package knightminer.metalborn.item;

import knightminer.metalborn.core.Config;
import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.menu.MetalbornMenu;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.item.book.AbstractBookItem;

import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

/**
 * Implements the satchel item, which stores metalminds and has a UI that can unequip and reequip metalminds.
 */
public class SatchelItem extends Item {
  private static final Component CLICK_TO_OPEN = Mantle.makeComponent("item", "book.click_to_open").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC);

  private final SatchelType type;
  public SatchelItem(Properties properties, SatchelType type) {
    super(properties);
    this.type = type;
  }

  @SuppressWarnings("deprecation") // don't need the context
  @Override
  public boolean canFitInsideContainerItems() {
    return false;
  }

  @Override
  public boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
    return true;
  }

  @Override
  public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
    // anything that is not steel takes normal damage. Steel only takes damage from things that hurt creative (e.g. the void)
    return type != SatchelType.STEEL || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
  }

  @Override
  public int getEntityLifespan(ItemStack stack, Level level) {
    // steel should not despawn
    if (type == SatchelType.STEEL) {
      // since age is a short, this value should never be reachable so the item will never despawn
      return Integer.MAX_VALUE;
    }
    return super.getEntityLifespan(stack, level);
  }

  @SuppressWarnings("deprecation") // don't need it
  @Override
  public void onDestroyed(ItemEntity entity) {
    if (!entity.level().isClientSide) {
      IItemHandler handler = entity.getItem().getCapability(Capabilities.ItemHandler.ITEM);
      if (handler != null && handler.getSlots() > 0) {
        ItemUtils.onContainerDestroyed(entity, IntStream.range(0, handler.getSlots()).mapToObj(handler::getStackInSlot).filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList());
      }
    }
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    // if the stack is in the player inventory, show the right click to open tooltip
    Level level = context.level();
    if (level != null && level.isClientSide) {
      Player player = SafeClientAccess.getPlayer();
      if (player != null && AbstractBookItem.isValidContainer(player.containerMenu)) {
        Inventory inventory = player.getInventory();
        if (inventory.items.contains(stack) || inventory.offhand.contains(stack)) {
          tooltip.add(CLICK_TO_OPEN);
        }
      }
    }
    tooltip.add(Component.translatable(stack.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    // TODO: tooltip about what it contains?
  }


  /* Menu */

  /** Common logic to open the menu */
  private void openMenu(Player player, ItemStack stack, int slot, boolean isHeld) {
    if (player instanceof ServerPlayer serverPlayer) {
      MetalId ferringType = MetalbornData.getData(player).getFerringType();
      serverPlayer.openMenu(new SatchelMenuProvider(stack, type, slot, isHeld), buffer -> {
        buffer.writeResourceLocation(ferringType);
        buffer.writeByte(slot);
        buffer.writeEnum(type);
      });
    }
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level pLevel, Player player, InteractionHand hand) {
    // open when right cicked
    ItemStack stack = player.getItemInHand(hand);
    boolean isClient = player.level().isClientSide;
    if (!isClient) {
      openMenu(player, stack, hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND, true);
    }
    return InteractionResultHolder.sidedSuccess(stack, isClient);
  }

  @Override
  public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack held, Slot slot, ClickAction action, Player player, SlotAccess access) {
    if (action == ClickAction.SECONDARY && held.isEmpty() && slot.container == player.getInventory() && AbstractBookItem.isValidContainer(player.containerMenu)) {
      if (!player.level().isClientSide) {
        // during inventory slot interactions, the menu calls `suppressRemoteUpdates()` and calls `resumeRemoteUpdates()` later
        // But we are swapping the open menu, so resume will never get called and we are possibly improperly resumed
        // mostly is an issue for inventoryMenu itself
        // so resume updates on the container we are about to open
        player.containerMenu.resumeRemoteUpdates();
        openMenu(player, stack, slot.getSlotIndex(), false);
        // then suppress updates on the new container after opening
        player.containerMenu.suppressRemoteUpdates();
      }
      return true;
    }
    return false;
  }


  /* Helpers */

  /** Enum of satchel variants */
  public enum SatchelType implements StringRepresentable {
    /** Holds 9 items */
    BRONZE(9),
    /** Holds 18 items */
    PEWTER(18),
    /** Holds 9 items, immune to damage from any source */
    STEEL(9),
    /** Holds 9 items, remains in inventory on death */
    NICROSIL(9),
    /** Holds 18 items, but items can only be removed, not added */
    MIST(18);

    private final int size;
    SatchelType(int size) {
      this.size = size;
    }

    /** Gets the number of slots for this type */
    public int getSize() {
      return size;
    }

    @Override
    public String getSerializedName() {
      return name().toLowerCase(Locale.ROOT);
    }
  }

  /** Checks if the given stack is valid for the given satchel type. */
  public static boolean isValid(ItemStack stack) {
    if (stack.isEmpty()) {
      return true;
    }
    // limited satchels forces a tag whitelist
    if (Config.LIMITED_SATCHELS.get() && !stack.is(Registration.SATCHEL_ITEMS)) {
      return false;
    }
    // no bags
    return stack.canFitInsideContainerItems() && stack.getCapability(Capabilities.ItemHandler.ITEM) == null;
  }

  /** Inventory for satchel items */
  public static class SatchelInventory extends ComponentItemHandler {
    public SatchelInventory(MutableDataComponentHolder stack, SatchelType type) {
      super(stack, DataComponents.CONTAINER, type.size);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
      return isValid(stack);
    }
  }

  /** Menu provider for opening the satchel */
  private record SatchelMenuProvider(ItemStack stack, SatchelType type, int slot, boolean isHeld) implements MenuProvider {
    @Override
    public Component getDisplayName() {
      return stack.getHoverName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
      return new MetalbornMenu(id, inventory, stack, type, slot);
    }

    @Override
    public boolean shouldTriggerClientSideContainerClosingOnOpen() {
      return false;

    }
  }
}

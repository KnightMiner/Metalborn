package knightminer.metalborn.item;

import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.menu.MetalbornMenu;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.item.AbstractBookItem;

import java.util.List;
import java.util.Locale;

/**
 * Implements the satchel item, which stores metalminds and has a UI that can unequip and reequip metalminds.
 * TODO: Config to limit satchel to metalmind items? While doing the predicate, prevent other bags inside satchels
 */
public class SatchelItem extends Item implements DyeableLeatherItem {
  private final SatchelType type;
  public SatchelItem(Properties properties, SatchelType type) {
    super(properties);
    this.type = type;
  }

  @Override
  public boolean canFitInsideContainerItems() {
    return false;
  }

  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
    return new SatchelCapability(type);
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable(stack.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    // TODO: tooltip about what it contains?
  }


  /* Menu */

  /** Common logic to open the menu */
  private void openMenu(Player player, ItemStack stack, int slot) {
    if (player instanceof ServerPlayer serverPlayer) {
      MetalId ferringType = MetalbornData.getData(player).getFerringType();
      NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
        (id, inventory, p) -> new MetalbornMenu(id, inventory, stack, type, slot),
        stack.getHoverName()
      ), buffer -> {
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
      openMenu(player, stack, hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND);
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
        openMenu(player, stack, slot.getSlotIndex());
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

  /** Inventory for satchel items */
  public static class SatchelInventory extends ItemStackHandler {
    private final SatchelType type;
    public SatchelInventory(SatchelType type) {
      super(type.size);
      this.type = type;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
      if (type == SatchelType.MIST) {
        return false;
      }
      return stack.isEmpty() || stack.getItem().canFitInsideContainerItems() && !stack.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent();
    }
  }

  /** Capability logic for a satchel item. Instantiated on the item class. */
  public static class SatchelCapability implements ICapabilitySerializable<CompoundTag> {
    private final ItemStackHandler inventory;
    private final LazyOptional<IItemHandler> capability;
    public SatchelCapability(SatchelType type) {
      inventory = new SatchelInventory(type);
      capability = LazyOptional.of(() -> inventory);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @javax.annotation.Nullable Direction side) {
      if (cap == ForgeCapabilities.ITEM_HANDLER) {
        return capability.cast();
      }
      return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
      CompoundTag tag = inventory.serializeNBT();
      // serializing size is dumb, we want size to be the one we were passed
      tag.remove("Size");
      return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
      nbt.remove("Size");
      inventory.deserializeNBT(nbt);
    }
  }
}

package knightminer.metalborn.item;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornCapability;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/** Base class for a metalmind */
public class MetalmindItem extends Item implements MetalItem, Metalmind {
  // translation keys
  private static final String KEY_AMOUNT = Metalborn.key("item", "metalmind.amount");
  private static final String KEY_STORES = Metalborn.key("item", "metalmind.stores");
  private static final String KEY_OWNER = Metalborn.key("item", "metalmind.owner");
  private static final Component UNKNOWN_OWNER = Component.translatable(KEY_OWNER, Metalborn.component("item", "metalmind.owner.unknown").withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.GRAY);
  private static final Component UNSEALED = Component.translatable(KEY_OWNER, Metalborn.component("item", "metalmind.owner.none").withStyle(ChatFormatting.ITALIC)).withStyle(ChatFormatting.GRAY);
  // NBT keys
  private static final String TAG_AMOUNT = "amount";
  private static final String TAG_OWNER = "owner";
  private static final String TAG_OWNER_NAME = "owner_name";

  /** Amount to multiply capacity by, for larger metalminds */
  private final int capacityMultiplier;

  public MetalmindItem(Properties props, int capacityMultiplier) {
    super(props);
    this.capacityMultiplier = capacityMultiplier;
  }

  @Override
  public int getMaxStackSize(ItemStack stack) {
    return 1;
  }


  /* Metal */

  @Override
  public MetalId getMetal(ItemStack stack) {
    return MetalItem.getMetal(stack);
  }

  @Override
  public boolean canUse(ItemStack stack, Player player) {
    // must have a metal and a tag
    CompoundTag tag = stack.getTag();
    if (tag != null) {
      MetalId metal = getMetal(stack);
      if (metal != MetalId.NONE) {
        // if we have an owner, must match the owner
        // TODO: identity shenanigans
        if (getAmount(stack) > 0 && tag.hasUUID(TAG_OWNER)) {
          UUID uuid = tag.getUUID(TAG_OWNER);
          if (!player.getUUID().equals(uuid)) {
            return false;
          }
        }
        return MetalbornCapability.getData(player).canUse(metal);
      }
    }
    return false;
  }


  /* Storage */

  /** Gets the amount stored in this stack */
  public static int getAmount(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    if (tag != null) {
      return tag.getInt(TAG_AMOUNT);
    }
    return 0;
  }

  /** Gets the capacity of this metalmind */
  public int getCapacity(ItemStack stack) {
    MetalId metal = getMetal(stack);
    if (metal != MetalId.NONE) {
      return MetalManager.INSTANCE.get(metal).capacity() * this.capacityMultiplier;
    }
    return 0;
  }

  @Override
  public boolean isEmpty(ItemStack stack) {
    return getAmount(stack) <= 0;
  }

  @Override
  public boolean isFull(ItemStack stack) {
    return getAmount(stack) >= getCapacity(stack);
  }

  /** Empties out the metalmind entirely */
  private static void emptyMetalmind(ItemStack stack) {
    // completely drained? clear amount and owner
    CompoundTag tag = stack.getTag();
    if (tag != null) {
      tag.remove(TAG_AMOUNT);
      tag.remove(TAG_OWNER);
      tag.remove(TAG_OWNER_NAME);
    }
  }

  @Override
  public int fill(ItemStack stack, Player player, int amount) {
    if (amount == 0) {
      return 0;
    }
    int stored = getAmount(stack);
    CompoundTag tag = stack.getOrCreateTag();
    // if we are the first to fill it, set the owner
    if (stored == 0) {
      // TODO: identity shenanigans
      tag.putUUID(TAG_OWNER, player.getUUID());
      tag.putString(TAG_OWNER_NAME, player.getGameProfile().getName());
    }

    // if given more than we can hold, return the leftover
    int updated = stored + amount;
    int capacity = getCapacity(stack);
    if (updated >= capacity) {
      tag.putInt(TAG_AMOUNT, capacity);
      return capacity - stored;
    } else {
      // store everything
      tag.putInt(TAG_AMOUNT, updated);
      return amount;
    }
  }

  @Override
  public int drain(ItemStack stack, Player player, int amount) {
    if (amount == 0) {
      return 0;
    }
    int updated = getAmount(stack) - amount;
    if (updated > 0) {
      // drained but not completely?
      stack.getOrCreateTag().putInt(TAG_AMOUNT, updated);
      return amount;
    } else {
      // completely drained? clear amount and owner
      emptyMetalmind(stack);
      return amount - updated;
    }
  }


  /* Transfer */

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    // TODO: consider some status bar messages for unusable metal and wrong owner
    // if using an offhand metalmind, try to transfer into main hand
    ItemStack stack = player.getItemInHand(hand);
    if (hand == InteractionHand.OFF_HAND) {
      int amount = getAmount(stack);
      // can use the offhand metalmind
      if (amount > 0 && canUse(stack, player)) {
        ItemStack mainhand = player.getMainHandItem();
        // can use the mainhand metalmind, and the two are storing the same thing
        if (mainhand.getItem() instanceof Metalmind metalmind && getMetal(stack).equals(metalmind.getMetal(mainhand)) && metalmind.canUse(mainhand, player)) {
          // if we could not transfer everything, drain the offhand by how much transferred
          int used = metalmind.fill(mainhand, player, amount);
          if (amount > used) {
            drain(stack, player, used);
          } else {
            // clear offhand entirely if it all transferred
            emptyMetalmind(stack);
          }

          return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
      }
    }
    return InteractionResultHolder.pass(stack);
  }

  /* Bar */

  @Override
  public boolean isBarVisible(ItemStack stack) {
    return true;
  }

  @Override
  public int getBarWidth(ItemStack stack) {
    int capacity = getCapacity(stack);
    return capacity > 0 ? Math.min(13, getAmount(stack) * 13 / capacity) : 0;
  }

  @Override
  public int getBarColor(ItemStack stack) {
    return 0x00BBFF;
  }


  /* Tooltip */

  @Override
  public Component getName(ItemStack stack) {
    return MetalItem.getMetalName(stack);
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag flag) {
    MetalId metal = getMetal(stack);
    if (metal != MetalId.NONE) {
      if (flag.isAdvanced()) {
        MetalItem.appendMetalId(metal, tooltip);
      }
      // stores
      tooltip.add(Component.translatable(KEY_STORES, metal.getStores().withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY));

      // amount
      int amount = getAmount(stack);
      tooltip.add(Component.translatable(KEY_AMOUNT, amount, getCapacity(stack)).withStyle(ChatFormatting.GRAY));

      // owner name
      if (amount > 0) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
          if (tag.contains(TAG_OWNER_NAME, Tag.TAG_STRING)) {
            tooltip.add(Component.translatable(KEY_OWNER, Component.literal(tag.getString(TAG_OWNER_NAME)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
          } else if (tag.hasUUID(TAG_OWNER)) {
            tooltip.add(UNKNOWN_OWNER);
          } else {
            tooltip.add(UNSEALED);
          }
        }
      }
    }
  }

  @Override
  public String getCreatorModId(ItemStack stack) {
    return MetalItem.getCreatorModId(stack);
  }
}

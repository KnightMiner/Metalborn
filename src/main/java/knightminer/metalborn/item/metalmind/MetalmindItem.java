package knightminer.metalborn.item.metalmind;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornData;
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
import net.minecraft.world.level.Level;

import java.util.List;

/** Common logic for a metalmind, with or without metal variants. */
public abstract class MetalmindItem extends Item implements Metalmind {
  // translation keys
  private static final String KEY_AMOUNT = Metalborn.key("item", "metalmind.amount");
  protected static final String KEY_STORES = Metalborn.key("item", "metalmind.stores");
  private static final String KEY_OWNER = Metalborn.key("item", "metalmind.owner");
  private static final Component UNKNOWN_OWNER = Component.translatable(KEY_OWNER, Metalborn.component("item", "metalmind.owner.unknown").withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.GRAY);
  private static final Component UNKEYED = Component.translatable(KEY_OWNER, Metalborn.component("item", "metalmind.owner.none").withStyle(ChatFormatting.ITALIC)).withStyle(ChatFormatting.GRAY);
  // NBT keys
  public static final String TAG_AMOUNT = "amount";
  private static final String TAG_OWNER = "owner";
  private static final String TAG_OWNER_NAME = "owner_name";

  /** Amount to multiply capacity by, for larger metalminds */
  protected final int capacityMultiplier;
  
  public MetalmindItem(Properties props, int capacityMultiplier) {
    super(props);
    this.capacityMultiplier = capacityMultiplier;
  }

  /** Checks if the given player is the owner of this metalmind */
  @SuppressWarnings("unused")  // keeping around for potential future feature
  protected static boolean isOwner(ItemStack stack, Player player, MetalbornData data) {
    CompoundTag tag = stack.getTag();
    if (tag != null && getAmount(stack) > 0 && tag.hasUUID(TAG_OWNER)) {
      // TODO: identity shenanigans?
      return tag.getUUID(TAG_OWNER).equals(player.getUUID());
    }
    return true;
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

  /** Gets the capacity for this metalmind */
  public abstract int getCapacity(ItemStack stack);

  @Override
  public boolean isEmpty(ItemStack stack) {
    return getAmount(stack) <= 0;
  }

  @Override
  public boolean isFull(ItemStack stack) {
    return getAmount(stack) >= getCapacity(stack);
  }


  /* Filling and draining */

  /** Empties out the metalmind entirely */
  protected void emptyMetalmind(CompoundTag tag) {
    tag.remove(TAG_AMOUNT);
    tag.remove(TAG_OWNER);
    tag.remove(TAG_OWNER_NAME);
  }

  /** Called when the metalmind is first filled to set any relevant data */
  protected void startFillingMetalmind(CompoundTag tag, Player player, MetalbornData data) {
    tag.putUUID(TAG_OWNER, player.getUUID());
    tag.putString(TAG_OWNER_NAME, player.getGameProfile().getName());
  }

  @Override
  public int fill(ItemStack stack, Player player, int amount, MetalbornData data) {
    if (amount <= 0) {
      return 0;
    }
    int stored = getAmount(stack);
    int capacity = getCapacity(stack);
    // if already full, no work to do. Also prevents us from deleting from an overfilled metalmind
    if (stored >= capacity) {
      return 0;
    }

    // set owner if it's missing and we have identity
    // TODO: should we set identity on storing in an unkeyed metalmind?
    CompoundTag tag = stack.getOrCreateTag();
    if (stored == 0) {
      startFillingMetalmind(tag, player, data);
    }

    // if now filled, we can't use the full amount
    int updated = stored + amount;
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
  public int drain(ItemStack stack, Player player, int amount, MetalbornData data) {
    if (amount <= 0) {
      return 0;
    }
    int updated = getAmount(stack) - amount;
    if (updated > 0) {
      // drained but not completely?
      stack.getOrCreateTag().putInt(TAG_AMOUNT, updated);
      return amount;
    } else {
      // completely drained? clear amount and owner
      CompoundTag tag = stack.getTag();
      if (tag != null) {
        emptyMetalmind(tag);
        if (tag.isEmpty()) {
          stack.setTag(null);
        }
      }
      return amount - updated;
    }
  }


  /* Transfer */

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    // attempt to wear the metalmind, requires a free slot
    ItemStack stack = player.getItemInHand(hand);
    if (canUse(stack, player)) {
      if (!level.isClientSide) {
        MetalbornData.getData(player).equip(stack);
      }
      return InteractionResultHolder.consume(stack);
    }
    return InteractionResultHolder.pass(stack);
  }


  /* Bar */

  @Override
  public boolean isBarVisible(ItemStack stack) {
    return stack.getCount() == 1;
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

  /** Appends the current amount to the tooltip */
  protected void appendAmount(MetalId metal, int amount, List<Component> tooltip) {
    tooltip.add(Component.translatable(KEY_AMOUNT, MetalManager.INSTANCE.get(metal).format(amount, capacityMultiplier)).withStyle(ChatFormatting.GRAY));
  }

  /** Appends the owner to the tooltip */
  protected static void appendOwner(ItemStack stack, List<Component> tooltip) {
    CompoundTag tag = stack.getTag();
    if (tag != null) {
      if (tag.contains(TAG_OWNER_NAME, Tag.TAG_STRING)) {
        tooltip.add(Component.translatable(KEY_OWNER, Component.literal(tag.getString(TAG_OWNER_NAME)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
      } else if (tag.hasUUID(TAG_OWNER)) {
        tooltip.add(UNKNOWN_OWNER);
      } else {
        tooltip.add(UNKEYED);
      }
    }
  }
}

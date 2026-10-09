package knightminer.metalborn.item.metalmind;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static knightminer.metalborn.core.Registration.AMOUNT;
import static knightminer.metalborn.core.Registration.OWNER;
import static knightminer.metalborn.core.Registration.OWNER_NAME;

/** Common logic for a metalmind, with or without metal variants. */
public abstract class MetalmindItem extends Item implements Metalmind {
  // translation keys
  private static final String KEY_AMOUNT = Metalborn.key("item", "metalmind.amount");
  public static final String KEY_STORES = Metalborn.key("item", "metalmind.stores");
  private static final String KEY_OWNER = Metalborn.key("item", "metalmind.owner");
  public static final Component UNKNOWN_OWNER = ownerComponent(Metalborn.component("item", "metalmind.owner.unknown").withStyle(ChatFormatting.RED));
  public static final Component UNKEYED = ownerComponent(Metalborn.component("item", "metalmind.owner.none").withStyle(ChatFormatting.ITALIC));

  /** Amount to multiply capacity by, for larger metalminds */
  protected final int capacityMultiplier;
  
  public MetalmindItem(Properties props, int capacityMultiplier) {
    super(props);
    this.capacityMultiplier = capacityMultiplier;
  }


  /* Identity */

  /** Checks if the given player has the same identity as this metalmind */
  protected static Usable matchesIdentity(ItemStack stack, MetalbornData data, boolean isTransfer) {
    UUID identity = data.getIdentity();
    UUID owner = stack.get(OWNER);
    if (owner != null) {
      // identity must match that inside the metalmind to use
      return identity != null && identity.equals(owner) ? Usable.ALWAYS : Usable.NEVER;
    }
    // no identity stored? tapping is always fine but storing is only fine if we also lack identity or are transferring; no overwriting identity
    return identity == null || isTransfer ? Usable.ALWAYS : Usable.TAPPING;
  }

  /** Checks if the given player can use this metalmind */
  protected static Usable checkIdentity(ItemStack stack, MetalbornData data, boolean isTransfer) {
    // if empty, identity is always valid
    return getAmount(stack) == 0 ? Usable.ALWAYS : matchesIdentity(stack, data, isTransfer);
  }

  /** Gets the identity of the given metalmind */
  @Nullable
  protected static UUID getIdentity(ItemStack stack) {
    return stack.get(OWNER);
  }

  /** Checks if the two stacks contain the same identity, or one of them has no identity */
  protected static boolean isSameIdentity(ItemStack stack1, ItemStack stack2) {
    // either being empty means it the same as we can fill one from the other
    // otherwise, need UUIDs to match
    return getAmount(stack1) == 0 || getAmount(stack2) == 0 || Objects.equals(getIdentity(stack1), getIdentity(stack2));
  }


  /* Storage */

  /** Gets the amount stored in this stack */
  public static int getAmount(ItemStack stack) {
    return stack.getOrDefault(AMOUNT, 0);
  }

  /** Sets the amount on the stack */
  @Override
  public void setAmount(ItemStack stack, @Nullable Player player, int amount, @Nullable MetalbornData data) {
    // if amount is 0, empty it out
    if (amount <= 0) {
      emptyMetalmind(stack);
    } else {
      // if amount is non-zero, start filling
      // if we were empty before, set on fill properties
      if (player != null && data != null && getAmount(stack) == 0) {
        startFillingMetalmind(stack, player, data);
      }
      stack.set(AMOUNT, amount);
    }
  }

  @Override
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
  protected void emptyMetalmind(ItemStack stack) {
    stack.remove(AMOUNT);
    stack.remove(OWNER);
    stack.remove(OWNER_NAME);
  }

  /** Called when the metalmind is first filled to set any relevant data */
  protected void startFillingMetalmind(ItemStack stack, Player player, MetalbornData data) {
    UUID identity = data.getIdentity();
    if (identity != null) {
      stack.set(OWNER, identity);
      stack.set(OWNER_NAME, data.getIdentityName());
    }
  }

  @Override
  public int fill(ItemStack stack, Player player, int amount, MetalbornData data) {
    int size = stack.getCount();
    amount /= size;
    if (amount <= 0) {
      return 0;
    }
    int stored = getAmount(stack);
    int capacity = getCapacity(stack);
    // if already full, no work to do. Also prevents us from deleting from an overfilled metalmind
    if (stored >= capacity) {
      return 0;
    }

    // set any data set when you first start storing
    if (stored == 0) {
      startFillingMetalmind(stack, player, data);
    }
    return fill(stack, stored, capacity, amount) * size;
  }

  /** Adds the given amount into the tag */
  protected static int fill(ItemStack stack, int stored, int capacity, int amount) {
    int updated = stored + amount;
    // if unable to use the full amount, return the remainder
    if (updated >= capacity) {
      stack.set(AMOUNT, capacity);
      return capacity - stored;
    } else {
      // store everything
      stack.set(AMOUNT, updated);
      return amount;
    }
  }

  @Override
  public void setFull(ItemStack stack) {
    stack.set(AMOUNT, getCapacity(stack));
  }

  /** Copies fill related data (other than amount) from the passed source to the given tag. */
  protected void copyDataFrom(ItemStack to, ItemStack from) {
    UUID owner = getIdentity(from);
    if (owner != null) {
      to.set(OWNER, owner);
      to.set(OWNER_NAME, from.get(OWNER_NAME));
    }
  }

  /** Fills this stack from the source stack. Handles copying the relevant data that goes along capacity. Ignores size of {@code source}. */
  protected int fillFrom(ItemStack stack, ItemStack source) {
    int amount = getAmount(source) / stack.getCount();
    if (amount <= 0) {
      return 0;
    }
    int stored = getAmount(stack);
    int capacity = getCapacity(stack);
    // if already full, no work to do. Also prevents us from deleting from an overfilled metalmind
    if (stored >= capacity) {
      return 0;
    }

    // copy properties from the target into the destination. Since we are transferring, it should keep identity the same
    if (stored == 0) {
      copyDataFrom(stack, source);
    }

    return fill(stack, stored, capacity, amount) * stack.getCount();
  }

  @Override
  public int drain(ItemStack stack, Player player, int amount, MetalbornData data) {
    int size = stack.getCount();
    amount /= size;
    if (amount <= 0) {
      return 0;
    }
    int current = getAmount(stack);
    int updated = current - amount;
    if (updated > 0) {
      // drained but not completely?
      stack.set(AMOUNT, updated);
      return amount * size;
    } else {
      // completely drained? clear amount and owner
      emptyMetalmind(stack);
      return current * size;
    }
  }


  /**
   * Checks if transfer is possible for {@link #overrideOtherStackedOnMe(ItemStack, ItemStack, Slot, ClickAction, Player, SlotAccess)}
   * @param destination Stack of this class receiving power.
   * @param source      Stack of unknown item giving power. Validation should make it instanceof MetalmindItem by the end.
   * @return true if power can be transferred from {@code held} into {@code stack}
   */
  protected abstract boolean isTransferrable(ItemStack destination, ItemStack source);

  @Override
  public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack held, Slot slot, ClickAction action, Player player, SlotAccess access) {
    // we can transfer a single metalmind of power into the slot stack, provided its the same power
    if (action == ClickAction.SECONDARY && slot.allowModification(player) && isTransferrable(stack, held)
        // if stackable, do not attempt transfer if either empty or full
        && (!ItemStack.isSameItemSameComponents(stack, held) || !isEmpty(stack) && !isFull(stack))) {
      MetalmindItem other = (MetalmindItem) held.getItem();
      MetalbornData data = MetalbornData.getData(player);
      // ensure both are usable (e.g. no identity issues)
      if (!other.isEmpty(held) && canUse(stack, -1, player, data).canStore() && other.canUse(held, -1, player, data).canTap()) {
        // attempt transfer
        int filled = fillFrom(stack, held);
        if (filled > 0) {
          int drained;
          // if we have more than 1, drain just one and keep the rest held
          if (held.getCount() > 1) {
            ItemStack split = held.split(1);
            drained = other.drain(split, player, filled, data);
            if (!player.getInventory().add(split)) {
              player.drop(split, false);
            }
          } else {
            drained = other.drain(held, player, filled, data);
          }
          // ensure we transferred the same amount
          if (drained != filled) {
            Metalborn.LOG.error("Failed to drain {} from {}, drained {} instead. Happened in stack on {}", filled, held, drained, stack);
          }
        }
      }
      return true;
    }
    return false;
  }

  /* Transfer */

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    // attempt to wear the metalmind, requires a free slot
    ItemStack stack = player.getItemInHand(hand);
    if (canUse(stack, player) != Usable.NEVER) {
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

  /** Makes the component for the "Stores: " tooltip text */
  public static Component makeStores(MetalId metal) {
    return Component.translatable(KEY_STORES, metal.getStores().withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY);
  }

  /** Appends the current amount to the tooltip */
  protected void appendAmount(MetalPower power, int amount, List<Component> tooltip) {
    tooltip.add(Component.translatable(KEY_AMOUNT, power.format(amount, capacityMultiplier)).withStyle(ChatFormatting.GRAY));
  }

  /** Gets the name of the owner */
  public static Component ownerComponent(Component name) {
    return Component.translatable(KEY_OWNER, name).withStyle(ChatFormatting.GRAY);
  }

  /** Gets the name of the owner */
  public static Component ownerComponent(String name) {
    return ownerComponent(Component.literal(name).withStyle(ChatFormatting.GOLD));
  }

  /** Appends the owner to the tooltip */
  protected static void appendOwner(ItemStack stack, List<Component> tooltip, boolean showUnkeyed) {
    String ownerName = stack.getOrDefault(OWNER_NAME, "");
    if (!ownerName.isEmpty()) {
      tooltip.add(ownerComponent(ownerName));
    } else if (stack.has(OWNER)) {
      tooltip.add(UNKNOWN_OWNER);
    } else if (showUnkeyed) {
      tooltip.add(UNKEYED);
    }
  }
}

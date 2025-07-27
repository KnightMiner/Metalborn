package knightminer.metalborn.item.metalmind;

import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.item.Fillable;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Interface of various methods available to metalmind items */
public interface Metalmind extends Fillable {
  /** Checks if the two stacks have the same power */
  default boolean isSamePower(ItemStack stack1, ItemStack stack2) {
    return true;
  }

  /** Gets text explaining what is stored by this stack */
  default Component getStores(ItemStack stack) {
    return MetalId.NONE.getStores();
  }

  /** Checks if the given player can use this metalmind */
  default Usable canUse(ItemStack stack, Player player) {
    return canUse(stack, -1, player, MetalbornData.getData(player));
  }

  /**
   * Checks if the given player can use this metalmind.
   * @param stack  Metalmind stack.
   * @param index  Inventory index containing the metalmind.
   * @param player Player attempting to use the metalmind.
   * @param data   Metalborn data.
   */
  default Usable canUse(ItemStack stack, int index, Player player, MetalbornData data) {
    return Usable.NEVER;
  }

  /**
   * Called when we start tapping or storing this metal. Only called if {@link #canUse(ItemStack, int, Player, MetalbornData)} returns true.
   * @param stack     Stack being used
   * @param index     Location of the stack in the metalmind inventory
   * @param newLevel  Updated level
   * @param oldLevel  Previous level
   * @param player    Player using the metalmind
   * @param data      Metalborn data capability
   * @return true to indicate we can continue to update. false will reset the level to 0.
   */
  default boolean onUpdate(ItemStack stack, int index, int newLevel, int oldLevel, Player player, MetalbornData data) {
    return false;
  }

  /**
   * Called when this metalmind is stopped to deactivate any powers not tracked directly by active metalminds.
   * Notably, {@link MetalbornData#stopUsingUnsealed(int)} needs to be called here; most power methods are handled automatically.
   * @param stack  Stack being used
   * @param index  Location of the stack in the metalmind inventory
   * @param level  Level before this metalmind was stopped
   * @param player Player using the metalmind
   * @param data   Metalborn data capability
   */
  default void onStop(ItemStack stack, int index, int level, Player player, MetalbornData data) {}

  /** Checks if the metalmind is currently empty (and thus cannot tap) */
  @Override
  default boolean isEmpty(ItemStack stack) {
    return true;
  }

  /** Checks if the metalmind is currently full (and thus cannot store) */
  @Override
  default boolean isFull(ItemStack stack) {
    return true;
  }

  @Override
  default void setFull(ItemStack stack) {}

  /** Gets the capacity for this metalmind */
  default int getCapacity(ItemStack stack) {
    return 0;
  }

  /**
   * Fills the metalmind by the given amount.
   * @param stack   Stack being filled.
   * @param player  Player filling the metalmind.
   * @param amount  Amount to fill. Always positive.
   * @return Amount actually filled.
   */
  default int fill(ItemStack stack, Player player, int amount, MetalbornData data) {
    return 0;
  }

  /**
   * Drains the metalmind by the given amount.
   * @param stack   Stack being filled.
   * @param player  Player filling the metalmind.
   * @param amount  Amount to drain. Always positive
   * @return Amount Actually drained.
   */
  default int drain(ItemStack stack, Player player, int amount, MetalbornData data) {
    return 0;
  }

  /** Sets the amount on the stack */
  default void setAmount(ItemStack stack, @Nullable Player player, int amount, @Nullable MetalbornData data) {}

  /** Default metalmind instance */
  Metalmind EMPTY = new Metalmind() {
    @Override
    public Usable canUse(ItemStack stack, Player player) {
      return Usable.NEVER;
    }
  };

  /** Enum return for {@link #canUse(ItemStack, int, Player, MetalbornData)} */
  enum Usable {
    /** Metalmind cannot be used */
    NEVER,
    /** Can tap metalmind, but not store */
    TAPPING,
    /** Can store in metalmind, but not tap */
    STORING,
    /** Can both store and tap metalmind */
    ALWAYS;

    /** Converts a pair of booleans into a usable value */
    public static Usable from(boolean canTap, boolean canStore) {
      if (canTap) {
        return canStore ? ALWAYS : TAPPING;
      } else {
        return canStore ? STORING : NEVER;
      }
    }

    /** Metalmind can be tapped */
    public boolean canTap() {
      return this == ALWAYS || this == TAPPING;
    }

    /** Can store in metalmind */
    public boolean canStore() {
      return this == ALWAYS || this == STORING;
    }

    /** Checks if the level is valid for this usable state. */
    public boolean isValid(int level) {
      if (level > 0) {
        return canTap();
      }
      if (level < 0) {
        return canStore();
      }
      return true;
    }
  }
}

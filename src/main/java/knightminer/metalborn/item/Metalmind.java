package knightminer.metalborn.item;

import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Interface of various methods available to metalmind items */
public interface Metalmind {
  /** Gets the metal type for this metalmind */
  default MetalId getMetal(ItemStack stack) {
    return MetalId.NONE;
  }

  /** Checks if the given player can use this metalmind */
  default boolean canUse(ItemStack stack, Player player) {
    return canUse(stack, player, MetalbornData.getData(player));
  }

  /**
   * Checks if the given player can use this metalmind.
   * @param stack  Metalmind stack.
   * @param player Player attempting to use the metalmind.
   * @param data   Metalborn data.
   */
  default boolean canUse(ItemStack stack, Player player, MetalbornData data) {
    return false;
  }

  /** Checks if the metalmind is currently empty (and thus cannot tap) */
  default boolean isEmpty(ItemStack stack) {
    return true;
  }

  /** Checks if the metalmind is currently full (and thus cannot store) */
  default boolean isFull(ItemStack stack) {
    return true;
  }

  /**
   * Fills the metalmind by the given amount.
   * @param stack   Stack being filled.
   * @param player  Player filling the metalmind.
   * @param amount  Amount to fill. Always positive.
   * @return Amount actually filled.
   */
  default int fill(ItemStack stack, Player player, int amount) {
    return 0;
  }

  /**
   * Drains the metalmind by the given amount.
   * @param stack   Stack being filled.
   * @param player  Player filling the metalmind.
   * @param amount  Amount to drain. Always positive
   * @return Amount Actually drained.
   */
  default int drain(ItemStack stack, Player player, int amount) {
    return 0;
  }

  /** Default metalmind instance */
  Metalmind EMPTY = new Metalmind() {
    @Override
    public boolean canUse(ItemStack stack, Player player) {
      return false;
    }
  };
}

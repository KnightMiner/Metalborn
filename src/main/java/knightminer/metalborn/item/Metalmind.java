package knightminer.metalborn.item;

import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Interface of various methods available to metalmind items */
public interface Metalmind {
  /** Gets the metal type for this metalmind */
  MetalId getMetal(ItemStack stack);

  /** Checks if the given player can use this metalmind */
  default boolean canUse(ItemStack stack, Player player) {
  return canUse(stack, player, MetalbornData.getData(player));
  }

  /** Checks if the given player can use this metalmind */
  boolean canUse(ItemStack stack, Player player, MetalbornData data);

  /** Checks if the metalmind is currently empty (and thus cannot tap) */
  boolean isEmpty(ItemStack stack);

  /** Checks if the metalmind is currently full (and thus cannot store) */
  boolean isFull(ItemStack stack);

  /**
   * Fills the metalmind by the given amount.
   * @param stack   Stack being filled.
   * @param player  Player filling the metalmind.
   * @param amount  Amount to fill. Always positive.
   * @return Amount actually filled.
   */
  int fill(ItemStack stack, Player player, int amount);

  /**
   * Drains the metalmind by the given amount.
   * @param stack   Stack being filled.
   * @param player  Player filling the metalmind.
   * @param amount  Amount to drain. Always positive
   * @return Amount Actually drained.
   */
  int drain(ItemStack stack, Player player, int amount);

  /** Default metalmind instance */
  Metalmind EMPTY = new Metalmind() {
    @Override
    public MetalId getMetal(ItemStack stack) {
      return MetalId.NONE;
    }

    @Override
    public boolean canUse(ItemStack stack, Player player) {
      return false;
    }

    @Override
    public boolean canUse(ItemStack stack, Player player, MetalbornData data) {
      return false;
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
      return true;
    }

    @Override
    public boolean isFull(ItemStack stack) {
      return true;
    }

    @Override
    public int fill(ItemStack stack, Player player, int amount) {
      return 0;
    }

    @Override
    public int drain(ItemStack stack, Player player, int amount) {
      return 0;
    }
  };
}

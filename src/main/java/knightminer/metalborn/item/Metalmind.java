package knightminer.metalborn.item;

import knightminer.metalborn.metal.MetalId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Interface of various methods available to metalmind items */
public interface Metalmind {
  /** Gets the metal type for this metalmind */
  MetalId getMetal(ItemStack stack);

  /** Checks if the given player can use this metalmind */
  boolean canUse(ItemStack stack, Player player);

  /** Gets the amount currently in the metalmind */
  int getAmount(ItemStack stack);

  /** Gets the maximum capacity in the metalmind */
  int getCapacity(ItemStack stack);

  /**
   * Fills the metalmind by the given amount.
   * @param stack   Stack being filled.
   * @param player  Player filling the metalmind.
   * @param amount  Amount to fill. Can be negative or positive.
   * @return Amount leftover after filling.
   */
  int fill(ItemStack stack, Player player, int amount);
}

package knightminer.metalborn.item;

import knightminer.metalborn.metal.MetalId;
import net.minecraft.world.item.ItemStack;

/** Interface of various methods available to spikes */
public interface Spike {
  /** Gets the metal type for this spike */
  default MetalId getMetal(ItemStack stack) {
    return MetalId.NONE;
  }

  /** Checks if the spike is currently filled, and thus usable */
  default boolean isFull(ItemStack stack) {
    return false;
  }

  /**
   * Fills the spike by the given amount.
   * @param stack   Stack being filled.
   * @param amount  Amount to fill. Always positive.
   * @return Amount actually filled.
   */
  default int fill(ItemStack stack, int amount) {
    return 0;
  }

  /** Default spike instance */
  Spike EMPTY = new Spike() {};
}

package knightminer.metalborn.item;

import knightminer.metalborn.metal.MetalId;
import net.minecraft.world.item.ItemStack;

/** Interface of various methods available to spikes */
public interface Spike extends Fillable {
  /** Gets the metal type for this spike */
  default MetalId getMetal(ItemStack stack) {
    return MetalId.NONE;
  }

  /**
   * Sets the charge on the spike to the given amount.
   * @param stack  Spike stack
   * @param amount Amount to set. Must be between 0 and the max charge.
   * @return Amount actually set.
   */
  default int setCharge(ItemStack stack, int amount) {
    return 0;
  }

  /** Gets the amount of charge needed to be full */
  default int getMaxCharge(ItemStack stack) {
    return 0;
  }

  /** Checks if the spike is currently filled, and thus usable */
  @Override
  default boolean isFull(ItemStack stack) {
    return false;
  }

  @Override
  default void setFull(ItemStack stack) {
    setCharge(stack, getMaxCharge(stack));
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

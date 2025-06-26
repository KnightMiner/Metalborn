package knightminer.metalborn.item;

import knightminer.metalborn.metal.MetalId;
import net.minecraft.world.item.ItemStack;

/** Interface of various methods available to spikes */
public interface Spike {
  /** Gets the metal type for this spike */
  MetalId getMetal(ItemStack stack);

  /** Checks if the spike is currently filled, and thus usable */
  boolean isFull(ItemStack stack);

  /**
   * Fills the spike by the given amount.
   * @param stack   Stack being filled.
   * @param amount  Amount to fill. Always positive.
   * @return Amount actually filled.
   */
  int fill(ItemStack stack, int amount);

  /** Default spike instance */
  Spike EMPTY = new Spike() {
    @Override
    public MetalId getMetal(ItemStack stack) {
      return MetalId.NONE;
    }

    @Override
    public boolean isFull(ItemStack stack) {
      return true;
    }

    @Override
    public int fill(ItemStack stack, int amount) {
      return 0;
    }
  };
}

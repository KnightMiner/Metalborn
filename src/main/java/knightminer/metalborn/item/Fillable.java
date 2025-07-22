package knightminer.metalborn.item;

import net.minecraft.world.item.ItemStack;

/** Interface for items that can be filled, such as {@link Spike} and {@link knightminer.metalborn.item.metalmind.Metalmind} */
public interface Fillable {
  /** Checks if the item is currently full */
  boolean isFull(ItemStack stack);

  /** Checks if the item is currently empty */
  boolean isEmpty(ItemStack stack);

  /** Fills the item stack */
  void setFull(ItemStack stack);
}

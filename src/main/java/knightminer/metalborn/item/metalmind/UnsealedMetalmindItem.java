package knightminer.metalborn.item.metalmind;

import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Metalmind that is usable without having the metal power */
public class UnsealedMetalmindItem extends PowerMetalmindItem {
  public UnsealedMetalmindItem(Properties props, int capacityMultiplier) {
    super(props, capacityMultiplier);
  }

  @Override
  public Usable canUse(ItemStack stack, int index, Player player, MetalbornData data) {
    // must have a metal, and be the owner. No need to be able to use the metal
    return MetalItem.getMetal(stack) != MetalId.NONE ? checkIdentity(stack, data) : Usable.NEVER;
  }
}

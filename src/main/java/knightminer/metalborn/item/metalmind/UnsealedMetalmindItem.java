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
    return MetalItem.getMetal(stack) != MetalId.NONE && data.canUseUnsealed(index) ? checkIdentity(stack, data) : Usable.NEVER;
  }

  @Override
  public boolean onUpdate(ItemStack stack, int index, int newLevel, int oldLevel, Player player, MetalbornData data) {
    // if we were not previously using and now are, mark it as used
    if (newLevel != 0 && oldLevel == 0) {
      data.useUnsealed(index);
    }
    // if we were previously using and no longer are, mark it unused
    else if (newLevel == 0 && oldLevel != 0) {
      data.stopUsingUnsealed(index);
    }
    return super.onUpdate(stack, index, newLevel, oldLevel, player, data);
  }
}

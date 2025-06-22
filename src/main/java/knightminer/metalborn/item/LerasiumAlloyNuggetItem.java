package knightminer.metalborn.item;

import knightminer.metalborn.core.MetalbornCapability;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Item which on consumption changes your ferring type */
public class LerasiumAlloyNuggetItem extends ConsumableItem implements MetalItem {
  public LerasiumAlloyNuggetItem(Properties props) {
    super(props);
  }

  @Override
  protected void onEat(ItemStack stack, LivingEntity entity) {
    MetalId metal = getMetal(stack);
    if (metal != MetalId.NONE) {
      MetalbornCapability.getData(entity).setFerring(metal);
      // TODO: play a message here?
      // TODO: clear hemalurgic effects for this metal
    }
  }
}

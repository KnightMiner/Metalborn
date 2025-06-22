package knightminer.metalborn.item;

import knightminer.metalborn.core.MetalbornCapability;
import knightminer.metalborn.metal.MetalManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Item which can be consumed to randomly change ferring type and cure hemalurgic effects */
public class LerasiumNuggetItem extends ConsumableItem {
  public LerasiumNuggetItem(Properties props) {
    super(props);
  }

  @Override
  protected void onEat(ItemStack stack, LivingEntity entity) {
    MetalbornCapability.getData(entity).setFerring(MetalManager.INSTANCE.getRandomFerring(entity.getRandom()).id());
    // TODO: remove all heamlurgic effects
  }
}

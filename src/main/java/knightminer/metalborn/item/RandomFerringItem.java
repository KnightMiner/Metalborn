package knightminer.metalborn.item;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.metal.MetalManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Item which can be consumed to randomly change ferring type */
public class RandomFerringItem extends ConsumableItem {
  private static final Component ON_EAT = Metalborn.component("item", "random_ferring.on_consume").withStyle(ChatFormatting.GRAY);

  public RandomFerringItem(Properties props) {
    super(props);
  }

  @Override
  public boolean isFoil(ItemStack stack) {
    return true;
  }

  @Override
  protected void onEat(ItemStack stack, LivingEntity entity) {
    MetalbornData.getData(entity).setFerringType(MetalManager.INSTANCE.getRandomFerring(entity.getRandom()).id());
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(ON_EAT);
  }
}

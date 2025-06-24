package knightminer.metalborn.item;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornCapability;
import knightminer.metalborn.metal.MetalManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Item which can be consumed to randomly change ferring type and cure hemalurgic effects */
public class LerasiumNuggetItem extends ConsumableItem {
  private static final Component ON_EAT = Metalborn.component("item", "lerasium_nugget.on_consume").withStyle(ChatFormatting.GRAY);

  public LerasiumNuggetItem(Properties props) {
    super(props);
  }

  @Override
  protected void onEat(ItemStack stack, LivingEntity entity) {
    MetalbornCapability.getData(entity).setFerringType(MetalManager.INSTANCE.getRandomFerring(entity.getRandom()).id());
    // TODO: remove all heamlurgic effects
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(ON_EAT);
  }
}

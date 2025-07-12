package knightminer.metalborn.item;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Item which on consumption changes your ferring type */
public class LerasiumAlloyNuggetItem extends ConsumableItem implements MetalItem {
  private static final String KEY_EAT = Metalborn.key("item", "lerasium_alloy_nugget.on_consume");
  public LerasiumAlloyNuggetItem(Properties props) {
    super(props);
  }

  @Override
  protected void onEat(ItemStack stack, LivingEntity entity) {
    MetalId metal = MetalItem.getMetal(stack);
    if (metal != MetalId.NONE) {
      MetalbornData.getData(entity).setFerringType(metal);
    }
  }


  /* Metal */

  @Override
  public Component getName(ItemStack stack) {
    return MetalItem.getMetalName(stack);
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag flag) {
    MetalId metal = MetalItem.getMetal(stack);
    if (metal != MetalId.NONE) {
      if (flag.isAdvanced()) {
        MetalItem.appendMetalId(metal, tooltip);
      }
      tooltip.add(Component.translatable(KEY_EAT, metal.getFerring()).withStyle(ChatFormatting.GRAY));
    }
  }

  @Override
  public String getCreatorModId(ItemStack stack) {
    return MetalItem.getCreatorModId(stack);
  }
}

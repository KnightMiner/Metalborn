package knightminer.metalborn.item;

import knightminer.metalborn.core.MetalbornCapability;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Item which on consumption changes your ferring type */
public class LerasiumAlloyNuggetItem extends ConsumableItem implements MetalItem {
  public LerasiumAlloyNuggetItem(Properties props) {
    super(props);
  }

  @Override
  protected void onEat(ItemStack stack, LivingEntity entity) {
    MetalId metal = MetalItem.getMetal(stack);
    if (metal != MetalId.NONE) {
      MetalbornCapability.getData(entity).setFerringType(metal);
      // TODO: play a message here?
      // TODO: clear hemalurgic effects for this metal
    }
  }


  /* Metal */

  @Override
  public Component getName(ItemStack stack) {
    return MetalItem.getMetalName(stack);
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag flag) {
    if (flag.isAdvanced()) {
      MetalItem. appendMetalId(stack, tooltip);
    }
  }

  @Override
  public String getCreatorModId(ItemStack stack) {
    return MetalItem.getCreatorModId(stack);
  }
}

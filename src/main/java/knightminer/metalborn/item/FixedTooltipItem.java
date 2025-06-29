package knightminer.metalborn.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Similar to {@link slimeknights.mantle.item.TooltipItem}, except the tooltip is always added */
public class FixedTooltipItem extends Item {
  private final Component tooltip;
  public FixedTooltipItem(Properties props, Component tooltip) {
    super(props);
    this.tooltip = tooltip;
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(this.tooltip);
  }
}

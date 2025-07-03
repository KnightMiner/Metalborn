package knightminer.metalborn.item.metalmind;

import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static knightminer.metalborn.item.MetalItem.getMetal;

/** Metalmind that grants a power */
public class PowerMetalmindItem extends MetalmindItem implements MetalItem {

  public PowerMetalmindItem(Properties props, int capacityMultiplier) {
    super(props, capacityMultiplier);
  }


  /* Metal */

  @Override
  public boolean isSamePower(ItemStack stack1, ItemStack stack2) {
    return getMetal(stack1).equals(getMetal(stack2));
  }

  @Override
  public Component getStores(ItemStack stack) {
    return getMetal(stack).getStores();
  }

  /** Gets the capacity of this metalmind */
  @Override
  public int getCapacity(ItemStack stack) {
    MetalId metal = getMetal(stack);
    if (metal != MetalId.NONE) {
      return MetalManager.INSTANCE.get(metal).capacity() * this.capacityMultiplier;
    }
    return 0;
  }

  @Override
  public boolean canUse(ItemStack stack, int index, Player player, MetalbornData data) {
    // must have a metal, be able to use it, and be the owner
    MetalId metal = getMetal(stack);
    return metal != MetalId.NONE && data.canUse(metal) && isOwner(stack, player, data);
  }

  @Override
  public boolean onUpdate(ItemStack stack, int index, int newLevel, int oldLevel, Player player, MetalbornData data) {
    data.updatePower(getMetal(stack), index, newLevel, oldLevel);
    return true;
  }


  /* Tooltip */

  @Override
  public Component getName(ItemStack stack) {
    return MetalItem.getMetalName(stack);
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag flag) {
    MetalId metal = getMetal(stack);
    if (metal != MetalId.NONE) {
      if (flag.isAdvanced()) {
        MetalItem.appendMetalId(metal, tooltip);
      }
      // stores
      tooltip.add(Component.translatable(KEY_STORES, metal.getStores().withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY));

      // amount
      int amount = getAmount(stack);
      appendAmount(metal, amount, tooltip);

      // owner name
      if (amount > 0) {
        appendOwner(stack, tooltip);
      }
    }
  }

  @Override
  public String getCreatorModId(ItemStack stack) {
    return MetalItem.getCreatorModId(stack);
  }
}

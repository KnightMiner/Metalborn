package knightminer.metalborn.item.metalmind;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static knightminer.metalborn.item.MetalItem.getMetal;

/** Metalmind that grants access to powers instead of granting powers */
public class InvestitureMetalmindItem extends MetalmindItem {
  public static final MetalId METAL = new MetalId(Metalborn.MOD_ID, "investiture");
  private static final String KEY_INVESTITURE = Metalborn.key("item", "metalmind.investiture");
  private static final Component STORES = Component.translatable(KEY_STORES, METAL.getStores().withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY);

  public InvestitureMetalmindItem(Properties props, int capacityMultiplier) {
    super(props, capacityMultiplier);
  }


  /* Metal */

  @Override
  public boolean isSamePower(ItemStack stack1, ItemStack stack2) {
    MetalId metal1 = getMetal(stack1);
    MetalId metal2 = getMetal(stack2);
    return metal1 == MetalId.NONE || metal2 == MetalId.NONE || metal1.equals(metal2);
  }

  @Override
  public Component getStores(ItemStack stack) {
    MetalId metalId = getMetal(stack);
    if (metalId == MetalId.NONE) {
      return METAL.getStores();
    }
    return metalId.getFerring();
  }

  @Override
  public int getCapacity(ItemStack stack) {
    return MetalManager.INSTANCE.get(METAL).capacity() * capacityMultiplier;
  }

  @Override
  public boolean canUse(ItemStack stack, int index, Player player, MetalbornData data) {
    return true;
  }

  @Override
  public boolean onUpdate(ItemStack stack, int index, int newLevel, int oldLevel, Player player, MetalbornData data) {
    MetalId metalId = getMetal(stack);
    // tapping
    if (metalId != MetalId.NONE) {
      // if we were not tapping this power before, grant it
      if (newLevel > 0 && oldLevel <= 0) {
        data.grantPower(metalId, index);
      }
      // if we were are no longer tapping this power, revoke it
      if (newLevel <= 0 && oldLevel > 0) {
        data.revokePower(metalId, index);
      }
    }
    // storing
    // if we were storing ferring type, stop
    if (newLevel >= 0 && oldLevel < 0) {
      data.stopStoringFerring(index);
      return true;
    }
    // store the ferring if the type matches
    if (newLevel < 0 && oldLevel >= 0) {
      MetalId ferringType = data.getFerringType();
      int amount = getAmount(stack);
      // must have a ferring type, and it must match the type we are storing (or we have no current type)
      if (ferringType != MetalId.NONE && (amount == 0 || metalId == MetalId.NONE || ferringType.equals(metalId))) {
        data.storeFerring(index);
        return true;
      }
      return false;
    }
    return true;
  }

  @Override
  protected void emptyMetalmind(CompoundTag tag) {
    super.emptyMetalmind(tag);
    tag.remove(MetalItem.TAG_METAL);
  }

  @Override
  protected void startFillingMetalmind(CompoundTag tag, Player player, MetalbornData data) {
    // onUpdate already ensured this is a valid ferring type, so just store it
    // note we don't store identity for investiture metalminds
    tag.putString(MetalItem.TAG_METAL, data.getFerringType().toString());
  }


  /* Tooltip */

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(STORES);

    // active metal
    MetalId metal = getMetal(stack);
    if (metal != MetalId.NONE) {
      tooltip.add(Component.translatable(KEY_INVESTITURE, metal.getFerring().withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
      if (flag.isAdvanced()) {
        MetalItem.appendMetalId(metal, tooltip);
      }
    }

    // amount
    int amount = getAmount(stack);
    appendAmount(METAL, amount, tooltip);
  }
}

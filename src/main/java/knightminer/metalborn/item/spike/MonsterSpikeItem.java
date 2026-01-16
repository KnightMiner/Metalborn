package knightminer.metalborn.item.spike;

import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.item.metalmind.MetalmindItem;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static knightminer.metalborn.item.metalmind.MetalmindItem.TAG_AMOUNT;

/** Represents a hemalurgic spike, which can be filled with power from monsters */
public class MonsterSpikeItem extends SpikeItem implements MetalItem, Spike {

  public MonsterSpikeItem(Properties props) {
    super(props);
  }


  /* Spike */

  /** Gets the amount of charge needed to be full */
  @Override
  public int getMaxCharge(ItemStack stack) {
    return MetalManager.INSTANCE.get(getMetal(stack)).hemalurgyCharge();
  }

  @Override
  public int setCharge(ItemStack stack, int amount) {
    // zero amount? clean up NBT
    if (amount <= 0) {
      CompoundTag tag = stack.getTag();
      if (tag != null) {
        tag.remove(TAG_FULL);
        tag.remove(TAG_AMOUNT);
        if (tag.isEmpty()) {
          stack.setTag(null);
        }
      }
      return 0;
    }

    // if now full, set the full tag
    CompoundTag tag = stack.getOrCreateTag();
    int max = getMaxCharge(stack);
    if (amount >= getMaxCharge(stack)) {
      tag.putBoolean(TAG_FULL, true);
      tag.remove(TAG_AMOUNT);
      return max;
    }

    // otherwise clear full and set amount
    tag.remove(TAG_FULL);
    tag.putInt(TAG_AMOUNT, amount);
    return amount;
  }

  @Override
  public boolean isFull(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    return tag != null && tag.getBoolean(TAG_FULL);
  }

  @Override
  public boolean isEmpty(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    return tag == null || (!tag.getBoolean(TAG_FULL) && tag.getInt(TAG_AMOUNT) == 0);
  }

  @Override
  public int fill(ItemStack stack, int amount) {
    if (amount <= 0) {
      return 0;
    }
    CompoundTag tag = stack.getOrCreateTag();
    if (tag.getBoolean(TAG_FULL)) {
      return 0;
    }
    int maxCharge = getMaxCharge(stack);
    int stored = MetalmindItem.getAmount(stack);
    int updated = stored + amount;
    if (updated >= maxCharge) {
      tag.putBoolean(TAG_FULL, true);
      tag.remove(TAG_AMOUNT);
      return maxCharge - stored;
    } else {
      tag.putInt(TAG_AMOUNT, updated);
      return amount;
    }
  }

  @Override
  public void addVariants(Consumer<ItemStack> consumer) {
    for (MetalPower power : MetalManager.INSTANCE.getSortedPowers()) {
      if (!power.feruchemy().isEmpty() && power.hemalurgyCharge() > 0) {
        ItemStack stack = withMetal(power.id());
        consumer.accept(stack.copy());
        stack.getOrCreateTag().putBoolean(TAG_FULL, true);
        consumer.accept(stack);
      }
    }
  }


  /* Bar */

  @Override
  public boolean isBarVisible(ItemStack stack) {
    return stack.getCount() == 1 && !isFull(stack);
  }

  @Override
  public int getBarWidth(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    if (tag == null) {
      return 0;
    }
    if (tag.getBoolean(TAG_FULL)) {
      return 13;
    }
    int capacity = getMaxCharge(stack);
    return capacity > 0 ? Math.min(13, MetalmindItem.getAmount(stack) * 13 / capacity) : 0;
  }

  @Override
  public int getBarColor(ItemStack stack) {
    return 0xFF0000;
  }


  /* Tooltip */

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag flag) {
    MetalId metal = getMetal(stack);
    if (metal != MetalId.NONE) {
      if (flag.isAdvanced()) {
        MetalItem.appendMetalId(metal, tooltip);
      }
      // steals
      tooltip.add(Component.translatable(KEY_STEALS, metal.getStores().withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY));

      // amount
      if (isFull(stack)) {
          tooltip.add(FULLY_CHARGED);
      } else {
        tooltip.add(Component.translatable(KEY_TARGET, metal.getTarget().withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.GRAY));
        int amount = MetalmindItem.getAmount(stack);
        tooltip.add(Component.translatable(KEY_CHARGE, amount, getMaxCharge(stack)).withStyle(ChatFormatting.GRAY));
      }
    }
  }


  /* Stabbing */

  @Override
  public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
    if (stack.getCount() == 1 && !isFull(stack)) {
      MetalId metal = getMetal(stack);
      if (metal != MetalId.NONE && metal.equals(MetalManager.INSTANCE.fromTarget(target.getType()).id())) {
        if (target.isDeadOrDying()) {
          fill(stack, 1);
        }
        return true;
      }
    }
    return false;
  }
}

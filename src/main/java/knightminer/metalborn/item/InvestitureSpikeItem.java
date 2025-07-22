package knightminer.metalborn.item;

import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.metalmind.InvestitureMetalmindItem;
import knightminer.metalborn.item.metalmind.MetalmindItem;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.util.CombatHelper;

import java.util.List;
import java.util.function.Consumer;

/** Spike that steals powers from players */
public class InvestitureSpikeItem extends SpikeItem {
  private static final Component STEALS = Component.translatable(KEY_STEALS, InvestitureMetalmindItem.METAL.getStores().withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY);
  private static final Component TARGET = Component.translatable(KEY_TARGET, InvestitureMetalmindItem.METAL.getTarget().withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.GRAY);

  public InvestitureSpikeItem(Properties props) {
    super(props);
  }

  @Override
  public int getMaxCharge(ItemStack stack) {
    return 1;
  }


  /* Tooltip */

  @Override
  public Component getName(ItemStack stack) {
    // don't show metal in tooltip
    return Component.translatable(this.getDescriptionId(stack));
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag flag) {
    // if filled, we have a metal, so display metal info
    if (isFull(stack)) {
      MetalId metal = getMetal(stack);
      if (flag.isAdvanced()) {
        MetalItem.appendMetalId(metal, tooltip);
      }
      tooltip.add(Component.translatable(KEY_STEALS, metal.getStores().withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY));
      tooltip.add(FULLY_CHARGED);
    } else {
      // no charge - generic tooltip
      tooltip.add(STEALS);
      tooltip.add(TARGET);
      int amount = MetalmindItem.getAmount(stack);
      tooltip.add(Component.translatable(KEY_CHARGE, amount, getMaxCharge(stack)).withStyle(ChatFormatting.GRAY));
    }
  }

  @Override
  public void addVariants(Consumer<ItemStack> consumer) {
    // want the empty item and a spike with each power
    consumer.accept(new ItemStack(this));
    for (MetalPower power : MetalManager.INSTANCE.getSortedPowers()) {
      if (!power.feruchemy().isEmpty() && power.hemalurgyCharge() > 0) {
        ItemStack stack = withMetal(power.id());
        stack.getOrCreateTag().putBoolean(TAG_FULL, true);
        consumer.accept(stack);
      }
    }
  }


  /* Stabbing */

  @Override
  public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
    if (stack.getCount() == 1 && target.getType() == EntityType.PLAYER && !isFull(stack)) {
      if (target.isDeadOrDying()) {
        MetalbornData data = MetalbornData.getData(target);
        MetalId type = data.getFerringType();
        if (type.isPresent()) {
          MetalItem.setMetal(stack, type);
          data.setFerringType(MetalId.NONE);
          fill(stack, 1);
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
    // if super did nothing, and it wasn't a not-ready attack, use it
    if (!result.getResult().consumesAction() && hand == InteractionHand.MAIN_HAND) {
      ItemStack stack = player.getItemInHand(hand);

      // if the spike is not full, main hand lets you fill it from yourself
      // though offhand must be empty to perform this technique, for the sake of dual wielding spikes
      if (stack.getCount() == 1 && player.getOffhandItem().isEmpty()) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
      }
    }
    return result;
  }

  @Override
  public int getUseDuration(ItemStack pStack) {
    return 72000;
  }

  @Override
  public UseAnim getUseAnimation(ItemStack pStack) {
    return UseAnim.SPEAR;
  }

  @Override
  public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
    // stab power from self
    if (!level.isClientSide && timeCharged >= 32) {
      // if the target has a metal, fill the spike
      MetalbornData data = MetalbornData.getData(entity);
      MetalId target = data.getFerringType();
      if (target.isPresent()) {
        MetalItem.setMetal(stack, target);
        data.setFerringType(MetalId.NONE);
        fill(stack, 1);
      }
      // hurt the target regardless of stabbing success
      entity.hurt(CombatHelper.damageSource(level, Registration.MAKE_SPIKE), 10);
    }
  }

  @Override
  public boolean useOnRelease(ItemStack stack) {
    return stack.is(this);
  }
}

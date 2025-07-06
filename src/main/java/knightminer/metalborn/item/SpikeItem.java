package knightminer.metalborn.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Config;
import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.metalmind.MetalmindItem;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.util.CombatHelper;
import slimeknights.mantle.util.OffhandCooldownTracker;

import java.util.List;
import java.util.function.Consumer;

import static knightminer.metalborn.item.metalmind.MetalmindItem.TAG_AMOUNT;

/** Represents a hemalurgic spike, which can be filled with power from monsters */
public class SpikeItem extends Item implements MetalItem, Spike {
  // translation keys
  private static final String KEY_CHARGE = Metalborn.key("item", "spike.charge");
  private static final String KEY_STEALS = Metalborn.key("item", "spike.steals");
  private static final String KEY_TARGET = Metalborn.key("item", "spike.target");
  private static final Component FULLY_CHARGED = Metalborn.component("item", "spike.charge.full").withStyle(ChatFormatting.GRAY);
  // weapon properties
  private static final float ATTACK_DAMAGE = 2;
  private static final float ATTACK_SPEED = 2;
  // NBT keys
  /** Tag marking a spike as full. Ensures datapack changes don't change a spike's fullness. */
  private static final String TAG_FULL = "full";

  /** Attribute modifiers for this as a weapon */
  private static final Multimap<Attribute,AttributeModifier> DEFAULT_MODIFIERS;
  static {
    ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
    builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", ATTACK_DAMAGE, AttributeModifier.Operation.ADDITION));
    builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", ATTACK_SPEED - 4, AttributeModifier.Operation.ADDITION));
    DEFAULT_MODIFIERS = builder.build();
  }

  public SpikeItem(Properties props) {
    super(props);
  }

  @Override
  public int getMaxStackSize(ItemStack stack) {
    return 1;
  }


  /* Spike */

  @Override
  public MetalId getMetal(ItemStack stack) {
    return MetalItem.getMetal(stack);
  }

  /** Gets the amount of charge needed to be full */
  public int getMaxCharge(ItemStack stack) {
    return MetalManager.INSTANCE.get(getMetal(stack)).hemalurgyCharge();
  }

  /** Sets the charge on the spike */
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
    } else {
      // if now full, set the full tag
      CompoundTag tag = stack.getOrCreateTag();
      int max = getMaxCharge(stack);
      if (amount >= getMaxCharge(stack)) {
        tag.putBoolean(TAG_FULL, true);
        tag.remove(TAG_AMOUNT);
        return max;
      } else {
        // otherwise clear full and set amount
        tag.remove(TAG_FULL);
        tag.putInt(TAG_AMOUNT, amount);
      }
    }
    return amount;
  }

  @Override
  public boolean isFull(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    return tag != null && tag.getBoolean(TAG_FULL);
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
    int maxCharge = MetalManager.INSTANCE.get(getMetal(stack)).hemalurgyCharge();
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
  public boolean isFoil(ItemStack stack) {
    return isFull(stack);
  }

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

  @Override
  public String getCreatorModId(ItemStack stack) {
    return MetalItem.getCreatorModId(stack);
  }


  /* Stabbing */

  @Override
  public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
    return slot == EquipmentSlot.MAINHAND ? DEFAULT_MODIFIERS : ImmutableMultimap.of();
  }

  @SuppressWarnings("deprecation") // this is faster if someone uses the old API, we don't need stack sensitive here
  @Override
  public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
    return slot == EquipmentSlot.MAINHAND ? DEFAULT_MODIFIERS : ImmutableMultimap.of();
  }

  @Override
  public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
    if (stack.getCount() == 1) {
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

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    // if using the offhand, swing for missing an entity
    if (hand == InteractionHand.OFF_HAND && Config.OFFHAND_SPIKE_ATTACK.get()) {
      if (OffhandCooldownTracker.isAttackReady(player)) {
        OffhandCooldownTracker.applyCooldown(player, CombatHelper.getOffhandAttribute(stack, player, Attributes.ATTACK_SPEED), 20);
        OffhandCooldownTracker.swingHand(player, hand, false);
        return InteractionResultHolder.consume(stack);
      }
      return InteractionResultHolder.pass(stack);
    }

    // main hand equips the spike
    if (isFull(stack)) {
      if (!level.isClientSide) {
        MetalbornData.getData(player).equip(stack);
      }
      return InteractionResultHolder.consume(stack);
    }
    // if the spike is not full, main hand lets you fill it from yourself
    // though offhand must be empty to perform this technique, for the sake of dual wielding spikes
    if (stack.getCount() > 1 || !player.getOffhandItem().isEmpty() || getMetal(stack) == MetalId.NONE) {
      return InteractionResultHolder.pass(stack);
    }
    player.startUsingItem(hand);
    return InteractionResultHolder.consume(stack);
  }

  @Override
  public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
    if (hand == InteractionHand.OFF_HAND && Config.OFFHAND_SPIKE_ATTACK.get() && OffhandCooldownTracker.isAttackReady(player)) {
      if (!player.level().isClientSide && CombatHelper.attack(stack, player, target, target, hand)) {
        OffhandCooldownTracker.swingHand(player, hand, false);
        return InteractionResult.CONSUME;
      }
    }
    return InteractionResult.PASS;
  }

  @Override
  public int getUseDuration(ItemStack pStack) {
    return 32;
  }

  @Override
  public UseAnim getUseAnimation(ItemStack pStack) {
    return UseAnim.SPEAR;
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
    if (!level.isClientSide) {
      // if the metal matches, fully fill the spike
      MetalId spike = getMetal(stack);
      if (spike != MetalId.NONE) {
        MetalbornData data = MetalbornData.getData(entity);
        MetalId target = data.getFerringType();
        if (spike.equals(target)) {
          data.setFerringType(MetalId.NONE);
          CompoundTag tag = stack.getOrCreateTag();
          tag.putBoolean(TAG_FULL, true);
          tag.remove(TAG_AMOUNT);
        }
      }
      // hurt the target regardless of stabbing success
      entity.hurt(Registration.makeSource(level, Registration.MAKE_SPIKE), 10);
    }
    return stack;
  }
}

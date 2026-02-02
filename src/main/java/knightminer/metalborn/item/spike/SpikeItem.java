package knightminer.metalborn.item.spike;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Config;
import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.item.MetalItem;
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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.util.CombatHelper;
import slimeknights.mantle.util.OffhandCooldownTracker;

import java.util.List;
import java.util.function.Consumer;

/** Spike item that is always full. Subclasses handle fillable spikes, but base is used for soulbound spikes which are crafted from full spikes. */
public class SpikeItem extends Item implements MetalItem, Spike {
  // translation
  protected static final String KEY_CHARGE = Metalborn.key("item", "spike.charge");
  protected static final String KEY_STEALS = Metalborn.key("item", "spike.steals");
  public static final String KEY_TARGET = Metalborn.key("item", "spike.target");
  protected static final Component FULLY_CHARGED = Metalborn.component("item", "spike.charge.full").withStyle(ChatFormatting.GRAY);
  // weapon properties
  private static final float ATTACK_DAMAGE = 2;
  private static final float ATTACK_SPEED = 2;
  // NBT keys
  /** Tag marking a spike as full. Ensures datapack changes don't change a spike's fullness. */
  protected static final String TAG_FULL = "full";

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
  public boolean canUseMetal(MetalPower metal) {
    return metal != MetalPower.DEFAULT && metal.hemalurgyCharge() > 0 && !metal.feruchemy().isEmpty();
  }

  @Override
  public MetalId getMetal(ItemStack stack) {
    return MetalItem.getMetal(stack);
  }

  @Override
  public boolean isFull(ItemStack stack) {
    return true;
  }

  @Override
  public void addVariants(Consumer<ItemStack> consumer) {
    for (MetalPower power : MetalManager.INSTANCE.getSortedPowers()) {
      if (!power.feruchemy().isEmpty() && power.hemalurgyCharge() > 0) {
        consumer.accept(withMetal(power.id()));
      }
    }
  }

  @Override
  public void verifyTagAfterLoad(CompoundTag tag) {
    MetalItem.verifyTagAfterLoad(tag);
  }


  /* Display */

  @Override
  public boolean isFoil(ItemStack stack) {
    return isFull(stack);
  }

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
    return InteractionResultHolder.pass(stack);
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
}

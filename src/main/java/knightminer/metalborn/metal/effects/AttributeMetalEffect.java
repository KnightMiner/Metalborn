package knightminer.metalborn.metal.effects;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.List;
import java.util.UUID;

/** Metal effect which applies an attribute to the target */
public record AttributeMetalEffect(String unique, UUID uuid, Attribute attribute, Operation operation, float base, float multiplier) implements MetalEffect {
  public static RecordLoadable<AttributeMetalEffect> LOADER = RecordLoadable.create(
    new AttributeUniqueField<>("unique", AttributeMetalEffect::unique),
    Loadables.ATTRIBUTE.requiredField("attribute", AttributeMetalEffect::attribute),
    new EnumLoadable<>(Operation.class).requiredField("operation", AttributeMetalEffect::operation),
    FloatLoadable.ANY.defaultField("base", 0f, AttributeMetalEffect::base),
    FloatLoadable.ANY.requiredField("multiplier", AttributeMetalEffect::multiplier),
    AttributeMetalEffect::new);

  /** @apiNote use {@link #AttributeMetalEffect(String, Attribute, Operation, float, float)} */
  @Internal
  public AttributeMetalEffect {}

  public AttributeMetalEffect(String unique, Attribute attribute, Operation operation, float base, float multiplier) {
    this(unique, UUID.nameUUIDFromBytes(unique.getBytes()), attribute, operation, base, multiplier);
  }

  @Override
  public RecordLoadable<AttributeMetalEffect> getLoader() {
    return LOADER;
  }

  @Override
  public void onChange(MetalPower power, LivingEntity entity, int level, int previous) {
    if (level != previous) {
      return;
    }
    AttributeInstance instance = entity.getAttribute(this.attribute);
    if (instance == null) {
      Metalborn.LOG.warn("Entity {} does not support attribute {}", entity, Loadables.ATTRIBUTE.getString(attribute));
    } else {
      instance.removeModifier(uuid);
      if (level != 0) {
        instance.addTransientModifier(new AttributeModifier(uuid, unique, base + multiplier * level, operation));
      }
    }
  }

  @Override
  public void getTooltip(MetalPower power, LivingEntity entity, int level, List<Component> tooltip) {
    // this code is recreated from the ItemStack attribute tooltip logic
    double value = base + level * multiplier;
    if (operation == Operation.ADDITION) {
      // vanilla multiplies knockback resist by 10 for some odd reason
      if (attribute.equals(Attributes.KNOCKBACK_RESISTANCE)) {
        value *= 10;
      }
    } else {
      // display multiply as percentage
      value *= 100;
    }
    // build tooltip
    Component name = Component.translatable(attribute.getDescriptionId());
    if (value > 0.0D) {
      tooltip.add(Component.translatable("attribute.modifier.plus." + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(value), name)
        .withStyle(ChatFormatting.BLUE));
    } else if (value < 0.0D) {
      tooltip.add(Component.translatable("attribute.modifier.take." + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(value * -1), name)
        .withStyle(ChatFormatting.RED));
    }
  }
}

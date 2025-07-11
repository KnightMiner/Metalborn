package knightminer.metalborn.metal.effects.general;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.MetalPower;
import knightminer.metalborn.metal.effects.MetalEffect;
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
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Metal effect which applies an attribute to the target */
public record AttributeMetalEffect(
  String unique, UUID uuid,
  Attribute attribute, Operation operation,
  float base, float multiplier,
  boolean swapColors
) implements MetalEffect {
  private static final EnumLoadable<Operation> OPERATION = new EnumLoadable<>(Operation.class);
  public static RecordLoadable<AttributeMetalEffect> LOADER = RecordLoadable.create(
    new AttributeUniqueField<>("unique", AttributeMetalEffect::unique),
    Loadables.ATTRIBUTE.requiredField("attribute", AttributeMetalEffect::attribute),
    OPERATION.requiredField("operation", AttributeMetalEffect::operation),
    FloatLoadable.ANY.defaultField("base", 0f, AttributeMetalEffect::base),
    FloatLoadable.ANY.requiredField("multiplier", AttributeMetalEffect::multiplier),
    BooleanLoadable.INSTANCE.defaultField("swap_colors", false, false, AttributeMetalEffect::swapColors),
    AttributeMetalEffect::new);

  /** @apiNote use {@link #builder(Attribute, Operation)} */
  @Internal
  public AttributeMetalEffect {}

  /** Constructor if you need to set the unique key. Only needed if you add multiple effects for the same attribute */
  private AttributeMetalEffect(String unique, Attribute attribute, Operation operation, float base, float multiplier, boolean swapColors) {
    this(unique, UUID.nameUUIDFromBytes(unique.getBytes()), attribute, operation, base, multiplier, swapColors);
  }

  /** Creates a new builder instance */
  public static Builder builder(Attribute attribute, Operation operation) {
    return new Builder(attribute, operation);
  }

  /** Creates a new builder instance */
  public static Builder builder(Supplier<? extends Attribute> attribute, Operation operation) {
    return builder(attribute.get(), operation);
  }

  @Override
  public RecordLoadable<AttributeMetalEffect> getLoader() {
    return LOADER;
  }

  @Override
  public void onChange(MetalPower power, LivingEntity entity, int level, int previous) {
    if (level == previous) {
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
  public int onTap(MetalPower power, LivingEntity entity, int level) {
    return multiplier != 0 ? level : 1;
  }

  @Override
  public int onStore(MetalPower power, LivingEntity entity, int level) {
    return multiplier != 0 ? level : 1;
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
        .withStyle(swapColors ? ChatFormatting.RED : ChatFormatting.BLUE));
    } else if (value < 0.0D) {
      tooltip.add(Component.translatable("attribute.modifier.take." + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(value * -1), name)
        .withStyle(swapColors ? ChatFormatting.BLUE : ChatFormatting.RED));
    }
  }


  /* Builder */
  public static class Builder {
    private final Attribute attribute;
    private final Operation operation;
    private String unique = "";
    private boolean swapColors = false;

    private Builder(Attribute attribute, Operation operation) {
      this.attribute = attribute;
      this.operation = operation;
    }

    /** Sets the unique string for the attribute instance. Generally not needed unless adding multiple of the same attribute */
    public Builder unique(String unique) {
      this.unique = unique;
      return this;
    }

    /** If true, swaps the colors for negative and positive effects */
    public Builder swapColors() {
      this.swapColors = true;
      return this;
    }

    /** Builds with the given base and multiplier */
    public AttributeMetalEffect build(float base, float multiplier) {
      return new AttributeMetalEffect(unique, attribute, operation, base, multiplier, swapColors);
    }

    /** Builds with the given flat level */
    public AttributeMetalEffect flat(int value) {
      return build(value, 0);
    }

    /** Builds with the given value each level */
    public AttributeMetalEffect eachLevel(float value) {
      return build(0, value);
    }
  }
}

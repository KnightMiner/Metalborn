package knightminer.metalborn.metal.effects.general;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalPower;
import knightminer.metalborn.metal.effects.MetalEffect;
import knightminer.metalborn.metal.effects.value.EffectValue;
import knightminer.metalborn.metal.effects.value.LinearEffectValue;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.List;

/** Metal effect which applies an attribute to the target */
public record AttributeMetalEffect(ResourceLocation id, Holder<Attribute> attribute, Operation operation, EffectValue value) implements MetalEffect {
  private static final EnumLoadable<Operation> OPERATION = new EnumLoadable<>(Operation.class);
  public static RecordLoadable<AttributeMetalEffect> LOADER = RecordLoadable.create(
    new AttributeIdField<>("id", AttributeMetalEffect::id),
    Loadables.ATTRIBUTE.requiredField("attribute", AttributeMetalEffect::attribute),
    OPERATION.requiredField("operation", AttributeMetalEffect::operation),
    EffectValue.LOADER.requiredField("value", AttributeMetalEffect::value),
    AttributeMetalEffect::new);

  /** @apiNote use {@link #builder(Holder, Operation)} */
  @Internal
  public AttributeMetalEffect {}

  /** Creates a new builder instance */
  public static Builder builder(Holder<Attribute> attribute, Operation operation) {
    return new Builder(attribute, operation);
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
      instance.removeModifier(id);
      if (level != 0) {
        instance.addTransientModifier(new AttributeModifier(id, value.getValue(level), operation));
      }
    }
  }

  @Override
  public int onTap(MetalPower power, LivingEntity entity, int level) {
    return value.getEffectiveLevel(level);
  }

  @Override
  public int onStore(MetalPower power, LivingEntity entity, int level) {
    return -value.getEffectiveLevel(-level);
  }

  @Override
  public void getTooltip(MetalPower power, LivingEntity entity, int level, List<Component> tooltip) {
    tooltip.add(attribute.value().toComponent(new AttributeModifier(id, value.getValue(level), operation), TooltipFlag.NORMAL));
  }


  /* Builder */
  public static class Builder {
    private final Holder<Attribute> attribute;
    private final Operation operation;
    private ResourceLocation id = MetalId.NONE;

    private Builder(Holder<Attribute> attribute, Operation operation) {
      this.attribute = attribute;
      this.operation = operation;
    }

    /** Sets the unique ID for the attribute instance. Generally not needed unless adding multiple of the same attribute */
    public Builder id(ResourceLocation id) {
      this.id = id;
      return this;
    }

    /** Builds with the given base and multiplier */
    public AttributeMetalEffect build(EffectValue value) {
      return new AttributeMetalEffect(id, attribute, operation, value);
    }

    /** Builds with the given base and multiplier */
    public AttributeMetalEffect build(float base, float multiplier) {
      return build(new LinearEffectValue(base, multiplier));
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

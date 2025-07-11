package knightminer.metalborn.metal.effects.nesting;

import knightminer.metalborn.metal.MetalPower;
import knightminer.metalborn.metal.effects.MetalEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.List;

/** Metal effect which only starts applying once the level is larger than a lower bound */
public record OffsetMetalEffect(int offset, MetalEffect effect) implements MetalEffect {
  public static final RecordLoadable<OffsetMetalEffect> LOADER = RecordLoadable.create(
    IntLoadable.FROM_ONE.requiredField("amount", OffsetMetalEffect::offset),
    MetalEffect.REGISTRY.requiredField("offset", OffsetMetalEffect::effect),
    OffsetMetalEffect::new);

  @Override
  public RecordLoadable<OffsetMetalEffect> getLoader() {
    return LOADER;
  }

  /** Applies the offset to the target level */
  private int applyOffset(int level) {
    if (level > 0) {
      return level - offset;
    } else {
      return level + offset;
    }
  }

  @Override
  public void onChange(MetalPower power, LivingEntity entity, int level, int previous) {
    // if the new level is greater than the offset, apply the effect at the offset level
    if (Math.abs(level) > offset) {
      // if the previous isn't greater than the offset, nothing to apply
      if (Math.abs(previous) <= offset) {
        previous = 0;
      } else {
        // if
        previous = applyOffset(previous);
      }
      effect.onChange(power, entity, applyOffset(level), previous);
    // otherwise, if we were previously valid, mark it as removed
    } else if (Math.abs(previous) > offset) {
      effect.onChange(power, entity, 0, applyOffset(previous));
    }
    // if neither is valid, nothing needs to be done
  }

  @Override
  public int onTap(MetalPower power, LivingEntity entity, int level) {
    if (level > offset) {
      return effect.onTap(power, entity, level - offset) + offset;
    }
    return 0;
  }

  @Override
  public int onStore(MetalPower power, LivingEntity entity, int level) {
    if (level > offset) {
      return effect.onStore(power, entity, level - offset) + offset;
    }
    return 0;
  }

  @Override
  public void getTooltip(MetalPower power, LivingEntity entity, int level, List<Component> tooltip) {
    if (Math.abs(level) > offset) {
      effect.getTooltip(power, entity, applyOffset(level), tooltip);
    }
  }
}

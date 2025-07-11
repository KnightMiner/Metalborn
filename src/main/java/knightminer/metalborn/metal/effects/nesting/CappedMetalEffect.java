package knightminer.metalborn.metal.effects.nesting;

import knightminer.metalborn.metal.MetalPower;
import knightminer.metalborn.metal.effects.MetalEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.List;

/** Metal effect that won't go beyond a cap */
public record CappedMetalEffect(int cap, MetalEffect effect) implements MetalEffect {
  public static final RecordLoadable<CappedMetalEffect> LOADER = RecordLoadable.create(
    IntLoadable.FROM_ONE.requiredField("level", CappedMetalEffect::cap),
    MetalEffect.REGISTRY.requiredField("capped", CappedMetalEffect::effect),
    CappedMetalEffect::new);

  @Override
  public RecordLoadable<CappedMetalEffect> getLoader() {
    return LOADER;
  }

  /** Gets the cap for the given level */
  private int applyCap(int level) {
    // if within the cap, keep level the same
    if (Math.abs(level) <= cap) {
      return level;
    }
    // need to keep the sign from the cap
    if (level > 0) {
      return cap;
    } else {
      return -cap;
    }
  }

  @Override
  public void onChange(MetalPower power, LivingEntity entity, int level, int previous) {
    level = applyCap(level);
    previous = applyCap(previous);
    if (level != previous) {
      effect.onChange(power, entity, level, previous);
    }
  }

  @Override
  public int onTap(MetalPower power, LivingEntity entity, int level) {
    return effect.onTap(power, entity, Math.min(level, cap));
  }

  @Override
  public int onStore(MetalPower power, LivingEntity entity, int level) {
    return effect.onStore(power, entity, Math.min(level, cap));
  }

  @Override
  public void getTooltip(MetalPower power, LivingEntity entity, int level, List<Component> tooltip) {
    effect.getTooltip(power, entity, applyCap(level), tooltip);
  }
}

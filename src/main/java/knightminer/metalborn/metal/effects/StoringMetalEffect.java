package knightminer.metalborn.metal.effects;

import knightminer.metalborn.metal.MetalPower;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;

import java.util.List;

/** Effect that only runs when storing */
public record StoringMetalEffect(MetalEffect effect) implements MetalEffect {
  public static final RecordLoadable<StoringMetalEffect> LOADER = RecordLoadable.create(MetalEffect.REGISTRY.directField("storing_type", StoringMetalEffect::effect), StoringMetalEffect::new);

  @Override
  public void onChange(MetalPower power, LivingEntity entity, int level, int previous) {
    // if positive, call effect on change
    if (level < 0) {
      // if previous is positive, treat it as 0; we never send a positive value through
      if (previous > 0) {
        previous = 0;
      }
      effect.onChange(power, entity, level, previous);
    } else if (previous < 0) {
      // if it was previously active but is no longer, send a 0 to indicate removed
      effect.onChange(power, entity, 0, previous);
    }
  }

  @Override
  public int onTick(MetalPower power, LivingEntity entity, int level) {
    if (level < 0) {
      return effect.onTick(power, entity, level);
    }
    return 0;
  }

  @Override
  public void getTooltip(MetalPower power, LivingEntity entity, int level, List<Component> tooltip) {
    if (level < 0) {
      effect.getTooltip(power, entity, level, tooltip);
    }
  }

  @Override
  public RecordLoadable<? extends IHaveLoader> getLoader() {
    return LOADER;
  }
}

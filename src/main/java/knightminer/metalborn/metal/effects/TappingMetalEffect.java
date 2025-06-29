package knightminer.metalborn.metal.effects;

import knightminer.metalborn.metal.MetalPower;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;

import java.util.List;

/** Effect that only runs when tapping */
public record TappingMetalEffect(MetalEffect effect) implements MetalEffect {
  public static final RecordLoadable<TappingMetalEffect> LOADER = RecordLoadable.create(MetalEffect.REGISTRY.directField("tapping_type", TappingMetalEffect::effect), TappingMetalEffect::new);

  @Override
  public void onChange(MetalPower power, LivingEntity entity, int level, int previous) {
    // if positive, call effect on change
    if (level > 0) {
      // if previous is negative, treat it as 0; we never send a negative value through
      if (previous < 0) {
        previous = 0;
      }
      effect.onChange(power, entity, level, previous);
    } else if (previous > 0) {
      // if it was previously active but is no longer, send a 0 to indicate removed
      effect.onChange(power, entity, 0, previous);
    }
  }

  @Override
  public int onTap(MetalPower power, LivingEntity entity, int level) {
    return effect.onTap(power, entity, level);
  }

  @Override
  public int onStore(MetalPower power, LivingEntity entity, int level) {
    return 0;
  }

  @Override
  public void getTooltip(MetalPower power, LivingEntity entity, int level, List<Component> tooltip) {
    if (level > 0) {
      effect.getTooltip(power, entity, level, tooltip);
    }
  }

  @Override
  public RecordLoadable<? extends IHaveLoader> getLoader() {
    return LOADER;
  }
}

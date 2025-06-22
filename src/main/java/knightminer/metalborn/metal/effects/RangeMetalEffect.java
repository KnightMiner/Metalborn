package knightminer.metalborn.metal.effects;

import knightminer.metalborn.metal.MetalPower;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;

import java.util.List;

/**
 * Effect that limits the nested effect to the given range. Notably used to ensure an effect only runs when positive or negative.
 * @param effect  Effect to run if in range.
 * @param min     Min level to run effect.
 * @param max     Max level to run effect.
 */
public record RangeMetalEffect(MetalEffect effect, int min, int max) implements MetalEffect {
  public static final RecordLoadable<RangeMetalEffect> LOADER = RecordLoadable.create(
    MetalEffect.REGISTRY.directField("effect_type", RangeMetalEffect::effect),
    IntLoadable.ANY_SHORT.defaultField("min_level", (int)Short.MIN_VALUE, RangeMetalEffect::min),
    IntLoadable.ANY_SHORT.defaultField("max_level", (int)Short.MAX_VALUE, RangeMetalEffect::max),
    RangeMetalEffect::new);

  @Override
  public RecordLoadable<? extends IHaveLoader> getLoader() {
    return LOADER;
  }

  @Override
  public void onChange(MetalPower power, LivingEntity entity, int level, int previous) {
    boolean previousInRange = min <= previous && previous <= max;
    // if in range, call the effect on change
    if (min <= level && level <= max) {
      // if previous is not in range, mark the previous as 0, though skip the effect if both end up as 0
      if (!previousInRange) {
        if (level == 0) {
          return;
        }
        previous = 0;
      }
      onChange(power, entity, level, previous);
    } else if (previousInRange) {
      // if its not in range but was previously in range, send a 0 to indicate it was "removed"
      onChange(power, entity, 0, previous);
    }
  }

  @Override
  public int onTick(MetalPower power, LivingEntity entity, int level) {
    if (min <= level && level <= max) {
      return effect.onTick(power, entity, level);
    }
    return 0;
  }

  @Override
  public void getTooltip(MetalPower power, LivingEntity entity, int level, List<Component> tooltip) {
    if (min <= level && level <= max) {
      effect.getTooltip(power, entity, level, tooltip);
    }
  }
}

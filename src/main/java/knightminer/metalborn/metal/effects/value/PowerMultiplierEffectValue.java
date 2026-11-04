package knightminer.metalborn.metal.effects.value;

import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/** Produces linear scaling, adding {@code multiplier} each level. */
public record PowerMultiplierEffectValue(float base, float multiplier) implements EffectValue {
  public static final RecordLoadable<PowerMultiplierEffectValue> LOADER = RecordLoadable.create(
    FloatLoadable.FROM_ZERO.defaultField("base", 0f, false, PowerMultiplierEffectValue::base),
    FloatLoadable.ANY.defaultField("multiplier", 0f, false, PowerMultiplierEffectValue::multiplier),
    PowerMultiplierEffectValue::new);

  @Override
  public float getValue(int level) {
    return (float) (Math.pow(base, level * multiplier) - 1);
  }

  @Override
  public int getEffectiveLevel(int level) {
    return multiplier == 0 ? Integer.compare(level, 0) : level;
  }

  @Override
  public RecordLoadable<PowerMultiplierEffectValue> getLoader() {
    return LOADER;
  }
}

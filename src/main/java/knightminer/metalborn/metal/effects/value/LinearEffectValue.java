package knightminer.metalborn.metal.effects.value;

import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/** Produces linear scaling, adding {@code multiplier} each level. */
public record LinearEffectValue(float base, float multiplier) implements EffectValue {
  public static final RecordLoadable<LinearEffectValue> LOADER = RecordLoadable.create(
    FloatLoadable.ANY.defaultField("base", 0f, false, LinearEffectValue::base),
    FloatLoadable.ANY.defaultField("multiplier", 0f, false, LinearEffectValue::multiplier),
    LinearEffectValue::new);

  /** Creates an instance with a base value of 0 */
  public static LinearEffectValue eachLevel(float multiplier) {
    return new LinearEffectValue(0, multiplier);
  }

  @Override
  public float getValue(int level) {
    return base + (multiplier * level);
  }

  @Override
  public int getEffectiveLevel(int level) {
    return multiplier == 0 ? Integer.compare(level, 0) : level;
  }

  @Override
  public RecordLoadable<LinearEffectValue> getLoader() {
    return LOADER;
  }
}

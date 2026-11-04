package knightminer.metalborn.metal.effects.value;

import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.array.FloatArrayLoadable;
import slimeknights.mantle.data.loadable.field.DefaultingField;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.function.BiPredicate;

/** Produces specific values from a list, the list size determining the number of effective levels. */
public record ListEffectValue(float[] storing, float[] tapping) implements EffectValue {
  private static final Loadable<float[]> ARRAY = new FloatArrayLoadable(FloatLoadable.ANY, 0, 10);
  private static final float[] ZERO = new float[0];
  private static final BiPredicate<float[],float[]> SKIP = (defaultValue, value) -> value.length == 0;
  public static final RecordLoadable<ListEffectValue> LOADER = RecordLoadable.create(
    new DefaultingField<>(ARRAY, "storing", ZERO, SKIP, ListEffectValue::storing),
    new DefaultingField<>(ARRAY, "tapping", ZERO, SKIP, ListEffectValue::tapping),
    ListEffectValue::new);

  @Override
  public float getValue(int level) {
    if (level > 0) {
      return tapping[Math.min(level, tapping.length) - 1];
    } else if (level < 0) {
      return storing[Math.min(-level, storing.length) - 1];
    }
    return 0;
  }

  @Override
  public int getEffectiveLevel(int level) {
    if (level > 0) {
      return Math.min(level, tapping.length);
    } else if (level < 0) {
      return Math.max(level, -storing.length);
    }
    return 0;
  }

  @Override
  public RecordLoadable<ListEffectValue> getLoader() {
    return LOADER;
  }
}

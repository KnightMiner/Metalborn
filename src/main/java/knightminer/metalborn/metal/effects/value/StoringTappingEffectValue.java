package knightminer.metalborn.metal.effects.value;

import slimeknights.mantle.data.loadable.record.RecordLoadable;

/** Effect value that runs a different value for storing versus tapping. */
public record StoringTappingEffectValue(EffectValue storing, EffectValue tapping) implements EffectValue {
  public static final RecordLoadable<StoringTappingEffectValue> LOADER = RecordLoadable.create(
    EffectValue.LOADER.requiredField("storing", StoringTappingEffectValue::storing),
    EffectValue.LOADER.requiredField("tapping", StoringTappingEffectValue::tapping),
    StoringTappingEffectValue::new);

  @Override
  public float getValue(int level) {
    if (level > 0) {
      return tapping.getValue(level);
    } else if (level < 0) {
      return storing.getValue(level);
    }
    return 0;
  }

  @Override
  public int getEffectiveLevel(int level) {
    if (level > 0) {
      return storing.getEffectiveLevel(level);
    } else if (level < 0) {
      return tapping.getEffectiveLevel(level);
    }
    return 0;
  }

  @Override
  public RecordLoadable<StoringTappingEffectValue> getLoader() {
    return LOADER;
  }
}

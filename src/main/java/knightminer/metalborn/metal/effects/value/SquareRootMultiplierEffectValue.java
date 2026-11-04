package knightminer.metalborn.metal.effects.value;

import net.minecraft.util.Mth;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/** Effect value which just takes the square root of a nested value */
public record SquareRootMultiplierEffectValue(EffectValue nested) implements EffectValue {
  public static final RecordLoadable<SquareRootMultiplierEffectValue> LOADER = RecordLoadable.create(EffectValue.LOADER.directField("square_root_type", SquareRootMultiplierEffectValue::nested), SquareRootMultiplierEffectValue::new);

  @Override
  public float getValue(int level) {
    return Mth.sqrt(1 + nested.getValue(level)) - 1;
  }

  @Override
  public int getEffectiveLevel(int level) {
    return nested.getEffectiveLevel(level);
  }

  @Override
  public RecordLoadable<SquareRootMultiplierEffectValue> getLoader() {
    return LOADER;
  }
}

package knightminer.metalborn.metal.effects.value;

import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.mantle.data.registry.GenericLoaderRegistry;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;

/** Helper to get the value of an effect for a given level. Used in {@link knightminer.metalborn.metal.effects.general.AttributeMetalEffect} notably. */
public interface EffectValue extends IHaveLoader {
  /** Zero instance, always returning zero regardless of level. */
  EffectValue ZERO = SingletonLoader.singleton(loader -> new EffectValue() {
    @Override
    public float getValue(int level) {
      return 0;
    }

    @Override
    public int getEffectiveLevel(int level) {
      return 0;
    }

    @Override
    public RecordLoadable<? extends EffectValue> getLoader() {
      return loader;
    }
  });

  /** Loader registry */
  GenericLoaderRegistry<EffectValue> LOADER = new GenericLoaderRegistry<>("Effect value", ZERO, false);

  /**
   * Gets the value to apply at the given level
   * @param level  Level
   * @return  Value to apply
   */
  float getValue(int level);

  /** Gets the effective level after clamping, used for values that have a max range */
  default int getEffectiveLevel(int level) {
    return level;
  }

  @Override
  RecordLoadable<? extends EffectValue> getLoader();
}

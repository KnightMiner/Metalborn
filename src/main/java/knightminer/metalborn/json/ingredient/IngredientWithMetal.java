package knightminer.metalborn.json.ingredient;

import knightminer.metalborn.metal.MetalPower;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;

import java.util.function.Predicate;

/** Interface for {@link knightminer.metalborn.json.recipe.MetalShapedForgeRecipe} and {@link knightminer.metalborn.json.recipe.MetalShapelessForgeRecipe} to get the filter used by ingredients. */
public interface IngredientWithMetal {
  /** Gets the filter for this ingredient */
  MetalFilter getFilter();

  /** Filter on allowed metals */
  enum MetalFilter implements Predicate<MetalPower> {
    ANY {
      @Override
      public boolean test(MetalPower metalPower) {
        return true;
      }
    },
    NATURAL_FERRING {
      @Override
      public boolean test(MetalPower power) {
        return power.ferring();
      }
    },
    METALMIND {
      @Override
      public boolean test(MetalPower power) {
        return !power.feruchemy().isEmpty();
      }
    },
    SPIKE {
      @Override
      public boolean test(MetalPower power) {
        return power.hemalurgyCharge() > 0 && !power.feruchemy().isEmpty();
      }
    };

    public static final EnumLoadable<MetalFilter> LOADABLE = new EnumLoadable<>(MetalFilter.class);
  }
}

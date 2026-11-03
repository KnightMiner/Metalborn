package knightminer.metalborn.json.predicate;

import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.core.registries.BuiltInRegistries;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.PredicateRegistry;

import java.util.List;
import java.util.function.Predicate;

/** Predicate on {@link MetalPower}. Used in {@link knightminer.metalborn.json.ingredient.MetalItemIngredient} and {@link knightminer.metalborn.json.ingredient.MetalShapeIngredient} */
public interface MetalPredicate extends IJsonPredicate<MetalPower> {
  /** Predicate that matches all metals */
  MetalPredicate ANY = simple(metal -> true);
  /** Predicate that matches no metals */
  MetalPredicate NONE = simple(metal -> false);
  /** Loader for metal predicates */
  PredicateRegistry<MetalPower> LOADER = new PredicateRegistry<>("Metal Predicate", ANY, NONE);

  @Override
  RecordLoadable<? extends MetalPredicate> getLoader();

  @Override
  default IJsonPredicate<MetalPower> inverted() {
    return LOADER.invert(this);
  }


  /* Simple */

  /** Matches powers that have a natural ferring form */
  MetalPredicate NATURAL_FERRING = simple(MetalPower::ferring);
  /** Matches powers that can be made as a metalmind */
  MetalPredicate METALMIND = simple(metal -> !metal.feruchemy().isEmpty());
  /** Matches powers that have a spike */
  MetalPredicate SPIKE = simple(metal -> metal.hemalurgyCharge() > 0 && !metal.feruchemy().isEmpty());
  /** Matches powers that have a spike */
  MetalPredicate HAS_NUGGET = simple(metal -> BuiltInRegistries.ITEM.getTag(metal.nugget()).filter(tag -> tag.size() > 0).isPresent());

  /** Creates a simple predicate with no arguments */
  static MetalPredicate simple(Predicate<MetalPower> predicate) {
    return SingletonLoader.singleton(loader -> new MetalPredicate() {
      @Override
      public boolean matches(MetalPower metal) {
        return predicate.test(metal);
      }

      @Override
      public RecordLoadable<MetalPredicate> getLoader() {
        return loader;
      }
    });
  }


  /* Helpers */

  /** Creates a predicate for a set of metal IDs. */
  static MetalPredicate set(MetalId... metals) {
    return new MetalSetPredicate(metals);
  }

  /** Creates an and predicate */
  @SafeVarargs
  static IJsonPredicate<MetalPower> and(IJsonPredicate<MetalPower>... predicates) {
    return LOADER.and(List.of(predicates));
  }

  /** Creates an or predicate */
  @SafeVarargs
  static IJsonPredicate<MetalPower> or(IJsonPredicate<MetalPower>... predicates) {
    return LOADER.or(List.of(predicates));
  }

  /** Interface for {@link knightminer.metalborn.json.recipe.forge.MetalShapedForgeRecipe} and {@link knightminer.metalborn.json.recipe.forge.MetalShapelessForgeRecipe} to get the filter used by ingredients. */
  interface IngredientWithMetal {
    /** Field for creating loadables */
    LoadableField<IJsonPredicate<MetalPower>,IngredientWithMetal> FILTER_FIELD = LOADER.defaultField("filter", IngredientWithMetal::filter);

    /** Gets the filter for this ingredient */
    IJsonPredicate<MetalPower> filter();
  }
}

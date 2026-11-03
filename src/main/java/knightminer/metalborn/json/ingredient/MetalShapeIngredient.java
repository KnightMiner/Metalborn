package knightminer.metalborn.json.ingredient;

import knightminer.metalborn.core.Registration;
import knightminer.metalborn.json.predicate.MetalPredicate;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import knightminer.metalborn.metal.MetalShape;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.util.RegistryHelper;

import java.util.stream.Stream;

/**
 * Ingredient matching a metal ingot or nugget.
 * @see MetalItemIngredient
 */
public record MetalShapeIngredient(MetalShape shape, IJsonPredicate<MetalPower> filter) implements ICustomIngredient, MetalPredicate.IngredientWithMetal {
  public static final RecordLoadable<MetalShapeIngredient> LOADER = RecordLoadable.create(
    MetalShape.LOADABLE.requiredField("shape", MetalShapeIngredient::shape),
    FILTER_FIELD, MetalShapeIngredient::new);

  /** Creates an ingredient matching ingots */
  public static Ingredient ingot(IJsonPredicate<MetalPower> filter) {
    return new MetalShapeIngredient(MetalShape.INGOT, filter).toVanilla();
  }

  /** Creates an ingredient matching nuggets */
  public static Ingredient nugget(IJsonPredicate<MetalPower> filter) {
    return new MetalShapeIngredient(MetalShape.NUGGET, filter).toVanilla();
  }

  @Override
  public IngredientType<MetalShapeIngredient> getType() {
    return Registration.METAL_SHAPE_INGREDIENT.get();
  }

  /** Gets the metal matched by the given stack */
  public MetalPower getMetal(ItemStack stack) {
    if (shape == null || stack.is(shape.getTag())) {
      MetalPower power = MetalManager.INSTANCE.fromIngotOrNugget(stack.getItem());
      if (power != MetalPower.DEFAULT && filter.matches(power)) {
        return power;
      }
    }
    return MetalPower.DEFAULT;
  }

  @Override
  public boolean test(ItemStack stack) {
    MetalPower power = getMetal(stack);
    return power != MetalPower.DEFAULT && filter.matches(power);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public Stream<ItemStack> getItems() {
    return MetalManager.INSTANCE.getSortedPowers()
      .stream()
      .filter(filter::matches)
      .flatMap(power -> RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, power.tag(shape)))
      .map(ItemStack::new);
  }
}

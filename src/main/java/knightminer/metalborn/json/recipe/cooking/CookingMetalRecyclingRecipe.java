package knightminer.metalborn.json.recipe.cooking;

import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.json.recipe.MetalResult;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import slimeknights.mantle.data.loadable.field.LoadableField;

import java.util.function.Predicate;

/** Common logic between {@link SmeltingMetalRecyclingRecipe} and {@link BlastingMetalRecyclingRecipe} */
public interface CookingMetalRecyclingRecipe {
  LoadableField<MetalResult,CookingMetalRecyclingRecipe> RESULT_FIELD = MetalResult.LOADABLE.requiredField("result", CookingMetalRecyclingRecipe::getResult);

  /** Gets the recipe filter */
  Predicate<MetalPower> getFilter();

  /** Gets the recipe result */
  MetalResult getResult();

  /** Checks if the result is present */
  default boolean isPresent(SingleRecipeInput input) {
    MetalPower metal = MetalItem.getMetalPower(input.item());
    // must have a metal, that matches our filter, and has a value for the requested tag
    return metal.isPresent() && getFilter().test(metal) && getResult().isPresent(metal);
  }

  /** Gets the result for this recipe */
  default ItemStack assembleMetal(SingleRecipeInput input) {
    MetalPower metal = MetalItem.getMetalPower(input.item());
    if (metal.isPresent()) {
      return getResult().get(metal);
    }
    return ItemStack.EMPTY;
  }
}

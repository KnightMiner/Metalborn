package knightminer.metalborn.json.recipe.cooking;

import knightminer.metalborn.json.recipe.MetalResult;
import knightminer.metalborn.metal.MetalShape;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.NotImplementedException;
import slimeknights.mantle.recipe.cooking.CookingRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;

/** Builder for {@link SmeltingMetalRecyclingRecipe} and {@link CookingMetalRecyclingBuilder} */
public class CookingMetalRecyclingBuilder extends CookingRecipeBuilder<CookingMetalRecyclingBuilder> {
  private final MetalResult result;
  protected CookingMetalRecyclingBuilder(MetalResult result) {
    super(ItemOutput.EMPTY);
    this.result = result;
  }

  /** Creates a builder for the given result */
  public static CookingMetalRecyclingBuilder builder(MetalResult result) {
    return new CookingMetalRecyclingBuilder(result);
  }

  /** Creates a builder for the given result */
  public static CookingMetalRecyclingBuilder builder(MetalShape shape, int amount) {
    return builder(new MetalResult(shape, amount));
  }

  /** Saves the smelting recipe */
  @Override
  public CookingMetalRecyclingBuilder saveSmelting(RecipeOutput output, ResourceLocation id) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Ingredient must be set");
    }
    output.accept(id, new SmeltingMetalRecyclingRecipe(group, ingredient, result, experience, cookingTime), null);
    return this;
  }

  /** Saves the blasting recipe */
  @Override
  public CookingMetalRecyclingBuilder saveBlasting(RecipeOutput output, ResourceLocation id) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Ingredient must be set");
    }
    output.accept(id, new BlastingMetalRecyclingRecipe(group, ingredient, result, experience, cookingTime / 2), null);
    return this;
  }

  @Override
  public CookingMetalRecyclingBuilder saveSmoking(RecipeOutput output, ResourceLocation id) {
    throw new NotImplementedException("Only smelting and blasting recipes are supported");
  }

  @Override
  public CookingMetalRecyclingBuilder saveCampfire(RecipeOutput output, ResourceLocation id) {
    throw new NotImplementedException("Only smelting and blasting recipes are supported");
  }
}

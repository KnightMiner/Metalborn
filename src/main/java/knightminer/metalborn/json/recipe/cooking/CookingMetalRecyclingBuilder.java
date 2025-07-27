package knightminer.metalborn.json.recipe.cooking;

import knightminer.metalborn.json.recipe.MetalResult;
import knightminer.metalborn.metal.MetalShape;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.NotImplementedException;
import slimeknights.mantle.recipe.cooking.CookingRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.function.Consumer;

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
  public CookingMetalRecyclingBuilder saveSmelting(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Ingredient must be set");
    }
    consumer.accept(new LoadableFinishedRecipe<>(new SmeltingMetalRecyclingRecipe(id, group, ingredient, result, experience, cookingTime), SmeltingMetalRecyclingRecipe.LOADABLE, null));
    return this;
  }

  /** Saves the blasting recipe */
  @Override
  public CookingMetalRecyclingBuilder saveBlasting(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Ingredient must be set");
    }
    consumer.accept(new LoadableFinishedRecipe<>(new BlastingMetalRecyclingRecipe(id, group, ingredient, result, experience, cookingTime / 2), BlastingMetalRecyclingRecipe.LOADABLE, null));
    return this;
  }

  @Override
  public CookingMetalRecyclingBuilder saveSmoking(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    throw new NotImplementedException("Only smelting and blasting recipes are supported");
  }

  @Override
  public CookingMetalRecyclingBuilder saveCampfire(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    throw new NotImplementedException("Only smelting and blasting recipes are supported");
  }
}

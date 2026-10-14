package knightminer.metalborn.json.recipe.cooking;

import knightminer.metalborn.core.Registration;
import knightminer.metalborn.json.ingredient.IngredientWithMetal;
import knightminer.metalborn.json.ingredient.IngredientWithMetal.MetalFilter;
import knightminer.metalborn.json.recipe.MetalResult;
import knightminer.metalborn.json.recipe.forge.MetalShapedForgeRecipe;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.cooking.BlastingResultRecipe;
import slimeknights.mantle.recipe.cooking.CookingResultRecipe;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;

import java.util.function.Predicate;

/** Furnace recipe that allows recycling a metal item into ingots or nuggets of the right type in a blast furnace */
public class BlastingMetalRecyclingRecipe extends BlastingRecipe implements CookingMetalRecyclingRecipe {
  public static final RecordLoadable<BlastingMetalRecyclingRecipe> LOADABLE = RecordLoadable.create(
    LoadableRecipeSerializer.RECIPE_GROUP,
    Loadables.ITEM_INGREDIENT_NONEMPTY.requiredField("ingredient", r -> r.ingredient),
    RESULT_FIELD, CookingResultRecipe.EXPERIENCE_FIELD, BlastingResultRecipe.COOKING_TIME_FIELD,
    BlastingMetalRecyclingRecipe::new);

  private final MetalResult result;
  private MetalFilter filter;
  protected BlastingMetalRecyclingRecipe(String group, Ingredient input, MetalResult result, float experience, int cookingTime) {
    super(group, CookingBookCategory.MISC, input, result.getDisplay(), experience, cookingTime);
    this.result = result;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return Registration.BLASTING_METAL_RECYCLING.get();
  }

  @Override
  public boolean isSpecial() {
    return true;
  }

  @Override
  public MetalResult getResult() {
    return result;
  }

  @Override
  public Predicate<MetalPower> getFilter() {
    if (filter == null) {
      if (MetalShapedForgeRecipe.unwrap(ingredient).getCustomIngredient() instanceof IngredientWithMetal metal) {
        filter = metal.filter();
      } else {
        filter = MetalFilter.ANY;
      }
    }
    return filter;
  }


  @Override
  public boolean matches(SingleRecipeInput input, Level level) {
    return super.matches(input, level) && isPresent(input);
  }

  @Override
  public ItemStack assemble(SingleRecipeInput inv, HolderLookup.Provider lookup) {
    return assembleMetal(inv);
  }
}

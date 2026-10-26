package knightminer.metalborn.json.recipe.forge;

import com.mojang.datafixers.util.Function4;
import knightminer.metalborn.core.Registration;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.ArrayList;
import java.util.List;

/** Forge recipe matching a number of ingredients which can go in any slot */
public class ShapelessForgeRecipe extends AbstractForgeRecipe {
  public static final RecordLoadable<ShapelessForgeRecipe> LOADABLE = makeLoader(ShapelessForgeRecipe::new);

  final NonNullList<Ingredient> ingredients;
  private final boolean isSimple;
  protected ShapelessForgeRecipe(NonNullList<Ingredient> ingredients, ItemOutput result, float experience, int cookingTime) {
    super(result, experience, cookingTime);
    this.ingredients = new NonNullList<>(List.copyOf(ingredients), Ingredient.EMPTY);
    this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return Registration.SHAPELESS_FORGE.get();
  }

  @Override
  public NonNullList<Ingredient> getIngredients() {
    return ingredients;
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return ingredients.size() <= width * height;
  }

  @Override
  public boolean matches(CraftingInput input, Level level) {
    // if the count does not match, recipe will not match
    if (input.ingredientCount() != ingredients.size()) {
      return false;
    }
    // if the sizes are both 1, just check the first ingredient, saves some data structures
    if (input.size() == 1 && ingredients.size() == 1) {
      return ingredients.getFirst().test(input.getItem(0));
    }
    // simple matching lets us use the mojang helper, its faster
    if (isSimple) {
      return input.stackedContents().canCraft(this, null);
    } else {
      // if not simple, use the neo matcher for full NBT checks
      List<ItemStack> inputs = new ArrayList<>(input.ingredientCount());
      for (ItemStack stack : input.items()) {
        if (!stack.isEmpty()) {
          inputs.add(stack);
        }
      }
      return RecipeMatcher.findMatches(inputs, this.ingredients) != null;
    }
  }

  /** Makes a loader for this recipe */
  public static <T extends ShapelessForgeRecipe> RecordLoadable<T> makeLoader(Function4<NonNullList<Ingredient>,ItemOutput,Float,Integer,T> constructor) {
    return RecordLoadable.create(
      Loadables.ITEM_INGREDIENT_NONEMPTY.list(1).mapChecked(
        (list, error) -> {
          int max = ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth();
          if (list.size() > max) {
            throw error.create("Too many ingredients for shapeless forge recipe. The maximum is: " + max);
          }
          return new NonNullList<>(list, Ingredient.EMPTY);
        },
        (l, error) -> l
      ).requiredField("ingredients", r -> r.ingredients),
      RESULT_FIELD, EXPERIENCE_FIELD, TIME_FIELD,
      constructor);
  }
}

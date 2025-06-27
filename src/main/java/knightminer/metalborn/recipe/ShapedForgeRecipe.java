package knightminer.metalborn.recipe;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Function7;
import knightminer.metalborn.core.Registration;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.IShapedRecipe;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;

import java.util.Map;

/** Cross between {@link net.minecraft.world.item.crafting.ShapedRecipe} and {@link net.minecraft.world.item.crafting.AbstractCookingRecipe} */
public class ShapedForgeRecipe extends AbstractForgeRecipe implements IShapedRecipe<CraftingContainer> {
  static int MAX_WIDTH = 2;
  static int MAX_HEIGHT = 2;

  /** Sets the max size for a recipe. In case an addon wants to reuse our recipes for a larger forge */
  public static void setMaxSize(int width, int height) {
    if (MAX_WIDTH < width) MAX_WIDTH = width;
    if (MAX_HEIGHT < height) MAX_HEIGHT = height;
  }

  final int width;
  final int height;
  final NonNullList<Ingredient> grid;

  public ShapedForgeRecipe(ResourceLocation id, int width, int height, NonNullList<Ingredient> grid, ItemOutput result, float experience, int cookingTime) {
    super(id, result, experience, cookingTime);
    this.width = width;
    this.height = height;
    this.grid = grid;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return Registration.SHAPED_FORGE.get();
  }

  @Override
  public int getRecipeHeight() {
    return height;
  }

  @Override
  public int getRecipeWidth() {
    return width;
  }

  @Override
  public NonNullList<Ingredient> getIngredients() {
    return grid;
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return this.width <= width && this.height <= height;
  }

  /** Based on {@link net.minecraft.world.item.crafting.ShapedRecipe#matches(CraftingContainer, Level)} */
  @Override
  public boolean matches(CraftingContainer inv, Level pLevel) {
    // try offsetting the recipe if there is space
    for (int xOffset = 0; xOffset <= inv.getWidth() - this.width; xOffset++) {
      for (int yOffset = 0; yOffset <= inv.getHeight() - this.height; yOffset++) {
        // try matching both regular and mirrored
        if (this.matches(inv, xOffset, yOffset, true)) {
          return true;
        }
        if (this.matches(inv, xOffset, yOffset, false)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks if the region of a crafting inventory is match for the recipe.
   * Based on {@link net.minecraft.world.item.crafting.ShapedRecipe#matches(CraftingContainer, int, int, boolean)}, can't call because its an instance method.
   */
  private boolean matches(CraftingContainer inv, int xStart, int yStart, boolean mirrored) {
    // loop over items in the inventory
    for(int x = 0; x < inv.getWidth(); x++) {
      for(int y = 0; y < inv.getHeight(); y++) {
        // offset X and Y based on the passed start positions
        int localX = x - xStart;
        int localY = y - yStart;
        Ingredient ingredient = Ingredient.EMPTY;
        if (localX >= 0 && localY >= 0 && localX < this.width && localY < this.height) {
          if (mirrored) {
            ingredient = this.grid.get(this.width - localX - 1 + localY * this.width);
          } else {
            ingredient = this.grid.get(localX + localY * this.width);
          }
        }
        if (!ingredient.test(inv.getItem(x + y * inv.getWidth()))) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean isIncomplete() {
    NonNullList<Ingredient> ingredients = this.getIngredients();
    return ingredients.isEmpty() || ingredients.stream().filter(ingredient -> !ingredient.isEmpty()).anyMatch(ForgeHooks::hasNoElements);
  }

  /** Custom serializer for parsing the recipe differently */
  public static class Serializer<T extends ShapedForgeRecipe> extends LoadableRecipeSerializer<T> {
    private final Function7<ResourceLocation, Integer, Integer, NonNullList<Ingredient>, ItemOutput, Float, Integer, T> constructor;
    public Serializer(Function7<ResourceLocation,Integer,Integer,NonNullList<Ingredient>,ItemOutput,Float,Integer,T> constructor) {
      super(RecordLoadable.create(
        ContextKey.ID.requiredField(),
        IntLoadable.FROM_ONE.requiredField("width", r -> r.width),
        IntLoadable.FROM_ONE.requiredField("height", r -> r.height),
        IngredientLoadable.ALLOW_EMPTY.list(1).xmap((l, error) -> new NonNullList<>(l, null), (l, error) -> l).requiredField("ingredients", r -> r.grid),
        RESULT_FIELD, EXPERIENCE_FIELD, TIME_FIELD,
        constructor));
      this.constructor = constructor;
    }

    @Override
    public T fromJson(ResourceLocation id, JsonObject json) {
      Map<String, Ingredient> map = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
      String[] pattern = ShapedRecipe.shrink(ShapedRecipe.patternFromJson(GsonHelper.getAsJsonArray(json, "pattern")));
      int width = pattern[0].length();
      int height = pattern.length;
      NonNullList<Ingredient> grid = ShapedRecipe.dissolvePattern(pattern, map, width, height);
      return constructor.apply(id, width, height, grid, RESULT_FIELD.get(json), EXPERIENCE_FIELD.get(json), TIME_FIELD.get(json));
    }
  }
}

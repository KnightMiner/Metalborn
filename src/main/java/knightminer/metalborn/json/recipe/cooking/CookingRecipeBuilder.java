package knightminer.metalborn.json.recipe.cooking;

import knightminer.metalborn.json.recipe.forge.AbstractForgeRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.function.Consumer;

/** Builder for {@link SmeltingResultRecipe} and {@link BlastingResultRecipe} */
public class CookingRecipeBuilder<T extends CookingRecipeBuilder<T>> extends AbstractForgeRecipeBuilder<T> {
  protected Ingredient ingredient = Ingredient.EMPTY;
  protected CookingBookCategory category = CookingBookCategory.MISC;

  protected CookingRecipeBuilder(ItemOutput result) {
    super(result);
    cookingTime = 200;
  }

  /** Creates a new builder instance */
  public static CookingRecipeBuilder<?> builder(ItemOutput result) {
    return new CookingRecipeBuilder<>(result);
  }

  /** Creates a new builder instance */
  public static CookingRecipeBuilder<?> builder(ItemLike output, int amount) {
    return builder(ItemOutput.fromItem(output, amount));
  }

  /** Creates a new builder instance */
  public static CookingRecipeBuilder<?> builder(ItemLike output) {
    return builder(output, 1);
  }

  /** Creates a new builder instance for a tag with the given size */
  public static CookingRecipeBuilder<?> builder(TagKey<Item> result, int amount) {
    return builder(ItemOutput.fromTag(result, amount));
  }

  /** Creates a new builder instance for a tag with size of 1 */
  public static CookingRecipeBuilder<?> builder(TagKey<Item> result) {
    return builder(result, 1);
  }

  /** Sets the input ingredient */
  @SuppressWarnings("unchecked")
  public T requires(Ingredient ingredient) {
    this.ingredient = ingredient;
    return (T) this;
  }

  /** Sets the input ingredient */
  public T requires(ItemLike item) {
    return requires(Ingredient.of(item));
  }

  /** Sets the input ingredient */
  public T requires(TagKey<Item> tag) {
    return requires(Ingredient.of(tag));
  }

  /** Saves the blasting recipe */
  @SuppressWarnings("unchecked")
  public T saveBlasting(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Ingredient must be set");
    }
    consumer.accept(new LoadableFinishedRecipe<>(new BlastingResultRecipe(id, group, category, ingredient, result, experience, cookingTime / 2), BlastingResultRecipe.LOADABLE, null));
    return (T) this;
  }

  /** Saves the smelting recipe */
  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Ingredient must be set");
    }
    consumer.accept(new LoadableFinishedRecipe<>(new SmeltingResultRecipe(id, group, category, ingredient, result, experience, cookingTime), SmeltingResultRecipe.LOADABLE, null));
  }
}

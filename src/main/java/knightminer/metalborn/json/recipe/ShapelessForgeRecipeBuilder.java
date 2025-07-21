package knightminer.metalborn.json.recipe;

import knightminer.metalborn.json.ingredient.MetalShapeIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Builder for shapeless forge recipes */
public class ShapelessForgeRecipeBuilder extends AbstractForgeRecipeBuilder<ShapelessForgeRecipeBuilder> {
  private boolean metal = false;
  private final List<Ingredient> ingredients = new ArrayList<>();
  protected ShapelessForgeRecipeBuilder(ItemOutput result) {
    super(result);
  }

  /** Creates a new builder instance */
  public static ShapelessForgeRecipeBuilder shapeless(ItemOutput result) {
    return new ShapelessForgeRecipeBuilder(result);
  }

  /** Creates a new builder instance */
  public static ShapelessForgeRecipeBuilder shapeless(TagKey<Item> tag, int count) {
    return shapeless(ItemOutput.fromTag(tag, count));
  }

  /** Creates a new builder instance */
  public static ShapelessForgeRecipeBuilder shapeless(ItemLike result, int count) {
    return shapeless(ItemOutput.fromItem(result, count));
  }

  /** Creates a new builder instance */
  public static ShapelessForgeRecipeBuilder shapeless(ItemLike result) {
    return shapeless(result, 1);
  }


  /** Adds an ingredient multiple times. */
  public ShapelessForgeRecipeBuilder requires(Ingredient ingredient, int quantity) {
    for(int i = 0; i < quantity; ++i) {
      this.ingredients.add(ingredient);
    }
    return this;
  }

  /** Adds an ingredient to the recipe. */
  public ShapelessForgeRecipeBuilder requires(Ingredient ingredient) {
    return requires(ingredient, 1);
  }

  /** Adds an item multiple times. */
  public ShapelessForgeRecipeBuilder requires(ItemLike item, int quantity) {
    return requires(Ingredient.of(item), quantity);
  }

  /** Adds an item to the recipe. */
  public ShapelessForgeRecipeBuilder requires(ItemLike item) {
    return requires(item, 1);
  }

  /** Adds a tag multiple times. */
  public ShapelessForgeRecipeBuilder requires(TagKey<Item> tag, int quantity) {
    return requires(Ingredient.of(tag), quantity);
  }

  /** Adds a tag to the recipe. */
  public ShapelessForgeRecipeBuilder requires(TagKey<Item> tag) {
    return requires(tag, 1);
  }

  /** Makes this a metal recipe, setting the output metal based on input ingredients. Should have at least one {@link MetalShapeIngredient} for best results. */
  public ShapelessForgeRecipeBuilder metal() {
    this.metal = true;
    return this;
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = buildOptionalAdvancement(id, "forge");
    if (metal) {
      consumer.accept(new LoadableFinishedRecipe<>(new MetalShapelessForgeRecipe(id, result, new NonNullList<>(ingredients, null), experience, computeCookingTime()), MetalShapelessForgeRecipe.LOADABLE, advancementId));
    } else {
      consumer.accept(new LoadableFinishedRecipe<>(new ShapelessForgeRecipe(id, result, new NonNullList<>(ingredients, null), experience, computeCookingTime()), ShapelessForgeRecipe.LOADABLE, advancementId));
    }
  }
}

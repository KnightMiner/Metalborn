package knightminer.metalborn.json.recipe.forge;

import com.mojang.datafixers.util.Function4;
import knightminer.metalborn.json.ingredient.MetalShapeIngredient;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.ArrayList;
import java.util.List;

/** Builder for shapeless forge recipes */
public class ShapelessForgeRecipeBuilder extends AbstractForgeRecipeBuilder<ShapelessForgeRecipeBuilder> {
  private Function4<NonNullList<Ingredient>,ItemOutput,Float,Integer,ShapelessForgeRecipe> constructor = ShapelessForgeRecipe::new;
  private final List<Ingredient> ingredients = new ArrayList<>();
  protected ShapelessForgeRecipeBuilder(ItemOutput result) {
    super(result);
  }

  /** Creates a new builder instance */
  public static ShapelessForgeRecipeBuilder shapeless(ItemOutput result) {
    return new ShapelessForgeRecipeBuilder(result);
  }

  /** Creates a new builder instance */
  public static ShapelessForgeRecipeBuilder shapeless(ItemStack result) {
    return shapeless(ItemOutput.fromStack(result));
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
    this.constructor = MetalShapelessForgeRecipe::new;
    return this;
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    AdvancementHolder advancement = buildOptionalAdvancement(output, id, "forge");
    output.accept(id, constructor.apply(new NonNullList<>(ingredients, null), result, experience, computeCookingTime()), advancement);
  }
}

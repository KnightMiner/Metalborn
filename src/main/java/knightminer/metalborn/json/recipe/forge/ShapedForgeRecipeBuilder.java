package knightminer.metalborn.json.recipe.forge;

import com.mojang.datafixers.util.Function4;
import knightminer.metalborn.json.ingredient.MetalShapeIngredient;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Builder for shaped forge recipes. Based largely on {@link net.minecraft.data.recipes.ShapedRecipeBuilder} */
public class ShapedForgeRecipeBuilder extends AbstractForgeRecipeBuilder<ShapedForgeRecipeBuilder> {
  private Function4<ShapedRecipePattern,ItemOutput,Float,Integer,ShapedForgeRecipe> constructor = ShapedForgeRecipe::new;
  private final List<String> rows = new ArrayList<>();
  private final Map<Character, Ingredient> key = new LinkedHashMap<>();
  protected ShapedForgeRecipeBuilder(ItemOutput result) {
    super(result);
  }

  /** Creates a new builder instance */
  public static ShapedForgeRecipeBuilder shaped(ItemOutput result) {
    return new ShapedForgeRecipeBuilder(result);
  }

  /** Creates a new builder instance */
  public static ShapedForgeRecipeBuilder shaped(ItemStack result) {
    return shaped(ItemOutput.fromStack(result));
  }

  /** Creates a new builder instance */
  public static ShapedForgeRecipeBuilder shaped(ItemLike result, int count) {
    return shaped(ItemOutput.fromItem(result, count));
  }

  /** Creates a new builder instance */
  public static ShapedForgeRecipeBuilder shaped(ItemLike result) {
    return shaped(result, 1);
  }

  /** Adds a key to the recipe pattern. */
  public ShapedForgeRecipeBuilder define(Character symbol, TagKey<Item> tag) {
    return this.define(symbol, Ingredient.of(tag));
  }

  /** Adds a key to the recipe pattern. */
  public ShapedForgeRecipeBuilder define(Character symbol, ItemLike item) {
    return this.define(symbol, Ingredient.of(item));
  }

  /** Adds a key to the recipe pattern. */
  public ShapedForgeRecipeBuilder define(Character symbol, Ingredient ingredient) {
    if (this.key.containsKey(symbol)) {
      throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined!");
    } else if (symbol == ' ') {
      throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
    } else {
      this.key.put(symbol, ingredient);
      return this;
    }
  }

  /** Adds a new entry to the patterns for this recipe. */
  public ShapedForgeRecipeBuilder pattern(String pattern) {
    if (!this.rows.isEmpty() && pattern.length() != this.rows.getFirst().length()) {
      throw new IllegalArgumentException("Pattern must be the same width on every line!");
    } else {
      this.rows.add(pattern);
      return this;
    }
  }

  /** Makes this a metal recipe, setting the output metal based on input ingredients. Should have at least one {@link MetalShapeIngredient} for best results. */
  public ShapedForgeRecipeBuilder metal() {
    this.constructor = MetalShapedForgeRecipe::new;
    return this;
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    output.accept(id, constructor.apply(ShapedRecipePattern.of(key, rows), result, experience, computeCookingTime()), buildOptionalAdvancement(output, id, "forge"));
  }
}

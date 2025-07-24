package knightminer.metalborn.json.recipe;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.json.ingredient.MetalShapeIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.primitive.CharacterLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/** Builder for shaped forge recipes. Based largely on {@link net.minecraft.data.recipes.ShapedRecipeBuilder} */
public class ShapedForgeRecipeBuilder extends AbstractForgeRecipeBuilder<ShapedForgeRecipeBuilder> {
  private RecipeSerializer<? extends ShapedForgeRecipe> serializer = Registration.SHAPED_FORGE.get();
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
    if (!this.rows.isEmpty() && pattern.length() != this.rows.get(0).length()) {
      throw new IllegalArgumentException("Pattern must be the same width on every line!");
    } else {
      this.rows.add(pattern);
      return this;
    }
  }

  /** Makes this a metal recipe, setting the output metal based on input ingredients. Should have at least one {@link MetalShapeIngredient} for best results. */
  public ShapedForgeRecipeBuilder metal() {
    this.serializer = Registration.METAL_SHAPED_FORGE.get();
    return this;
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    ensureValid(id);
    ResourceLocation advancementId = buildOptionalAdvancement(id, "forge");
    consumer.accept(new Finished(id, advancementId));
  }

  /** Makes sure that this recipe is valid. Based on {@link ShapedRecipeBuilder#ensureValid(ResourceLocation)} */
  private void ensureValid(ResourceLocation id) {
    if (this.rows.isEmpty()) {
      throw new IllegalStateException("No pattern is defined for shaped recipe " + id + "!");
    } else {
      Set<Character> set = Sets.newHashSet(this.key.keySet());
      set.remove(' ');

      for(String s : this.rows) {
        for(int i = 0; i < s.length(); ++i) {
          char c0 = s.charAt(i);
          if (!this.key.containsKey(c0) && c0 != ' ') {
            throw new IllegalStateException("Pattern in recipe " + id + " uses undefined symbol '" + c0 + "'");
          }

          set.remove(c0);
        }
      }

      if (!set.isEmpty()) {
        throw new IllegalStateException("Ingredients are defined but not used in pattern for recipe " + id);
      } else if (this.rows.size() == 1 && this.rows.get(0).length() == 1) {
        throw new IllegalStateException("Shaped recipe " + id + " only takes in a single item - should it be a shapeless recipe instead?");
      }
    }
  }

  /** Finished recipe instance */
  private class Finished extends AbstractFinishedRecipe {
    private static final Loadable<List<String>> PATTERN_LOADABLE = StringLoadable.DEFAULT.list(1);
    private static final Loadable<Map<Character,Ingredient>> KEY_LOADABLE = CharacterLoadable.DEFAULT.mapWithValues(IngredientLoadable.DISALLOW_EMPTY);

    public Finished(ResourceLocation id, @Nullable ResourceLocation advancementId) {
      super(id, advancementId);
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      json.add("pattern", PATTERN_LOADABLE.serialize(rows));
      json.add("key", KEY_LOADABLE.serialize(key));
      json.add("result", ItemOutput.Loadable.REQUIRED_STACK.serialize(result));
      if (experience != 0) {
        json.addProperty("experience", experience);
      }
      json.addProperty("cooking_time", computeCookingTime());
    }

    @Override
    public RecipeSerializer<?> getType() {
      return serializer;
    }
  }
}

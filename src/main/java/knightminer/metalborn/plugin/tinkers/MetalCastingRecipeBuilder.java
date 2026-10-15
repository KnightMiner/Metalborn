package knightminer.metalborn.plugin.tinkers;

import com.google.gson.JsonObject;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.json.ingredient.IngredientWithMetal.MetalFilter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.FalseCondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.data.mock.MockRecipe;
import slimeknights.mantle.recipe.data.mock.MockRecipe.SimpleMockRecipe;
import slimeknights.mantle.recipe.data.mock.MockRecipeConsumer;
import slimeknights.mantle.recipe.helper.ItemOutput;

/** Builder for {@link MetalCastingRecipe}. Safe to use even if Tinkers' Construct is not a runtime dependency. */
public class MetalCastingRecipeBuilder extends AbstractRecipeBuilder<MetalCastingRecipeBuilder> {
  private final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<?>> serializer;
  private final ItemOutput result;
  private Ingredient cast = Ingredient.EMPTY;
  private MetalFilter filter = MetalFilter.ANY;
  private int amount;
  private boolean consumed = false;
  private boolean switchSlots = false;

  private MetalCastingRecipeBuilder(DeferredHolder<RecipeSerializer<?>,RecipeSerializer<?>> serializer, ItemOutput result) {
    this.serializer = serializer;
    this.result = result;
  }

  /** Creates a new builder for a basin recipe */
  public static MetalCastingRecipeBuilder basin(ItemOutput result) {
    return new MetalCastingRecipeBuilder(Registration.METAL_CASTING_BASIN, result);
  }

  /** Creates a new builder for a basin recipe */
  public static MetalCastingRecipeBuilder basin(ItemLike result) {
    return basin(ItemOutput.fromItem(result));
  }

  /** Creates a new builder for a table recipe */
  public static MetalCastingRecipeBuilder table(ItemOutput result) {
    return new MetalCastingRecipeBuilder(Registration.METAL_CASTING_TABLE, result);
  }

  /** Creates a new builder for a table recipe */
  public static MetalCastingRecipeBuilder table(ItemLike result) {
    return table(ItemOutput.fromItem(result));
  }

  /** Sets the filter for this recipe */
  public MetalCastingRecipeBuilder setFilter(MetalFilter filter) {
    this.filter = filter;
    return this;
  }

  /** Sets the fluid amount */
  public MetalCastingRecipeBuilder setAmount(int amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Set the cast to the given ingredient
   * @param cast      Ingredient
   * @param consumed  If true, cast is consumed
   * @return  Builder instance
   */
  public MetalCastingRecipeBuilder setCast(Ingredient cast, boolean consumed) {
    this.cast = cast;
    this.consumed = consumed;
    return this;
  }

  /**
   * Set output of recipe to be put into the input slot.
   * Mostly used for cast creation
   */
  public MetalCastingRecipeBuilder setSwitchSlots() {
    this.switchSlots = true;
    return this;
  }

  @Override
  public void save(RecipeOutput output) {
    save(output, Loadables.ITEM.getKey(result.get().getItem()));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    // TODO: can I safely call the constructor here?
    throw new UnsupportedOperationException();
  }

  /** Saves this recipe without requiring tinkers during datagen. */
  public void save(MockRecipeConsumer consumer) {
    save(consumer, Loadables.ITEM.getKey(result.get().getItem()));
  }

  /** Saves this recipe without requiring tinkers during datagen. */
  public void save(MockRecipeConsumer consumer, ResourceLocation id) {
    // FIXME: switch condition to TinkersMockRecipeBuilder.MOD_LOADED when tinkers is ported
    consumer.accept(new Finished(id), FalseCondition.INSTANCE);
    if (!criteria.isEmpty()) {
      consumer.advancement(id, "casting", criteria, FalseCondition.INSTANCE);
    }
  }

  /** Finished recipe instance */
  private class Finished implements SimpleMockRecipe {
    private final ResourceLocation id;
    public Finished(ResourceLocation id) {
      this.id = id;
    }

    @Override
    public ResourceLocation id() {
      return id;
    }

    @Override
    public ResourceLocation serializerId() {
      return serializer.getId();
    }

    @Override
    public void serializeRecipeData(JsonObject json, MockRecipe.Serializer serializer) {
      if (!group.isEmpty()) {
        json.addProperty("group", group);
      }
      if (cast != Ingredient.EMPTY) {
        json.add("cast", serializer.ingredient(cast));
      }
      if (filter != MetalFilter.ANY) {
        json.add("filter", MetalFilter.LOADABLE.serialize(filter));
      }
      json.addProperty("amount", amount);
      json.add("result", serializer.item(result));
      if (consumed) {
        json.addProperty("cast_consumed", consumed);
      }
      if (switchSlots) {
        json.addProperty("switch_slots", switchSlots);
      }
    }
  }
}

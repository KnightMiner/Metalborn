package knightminer.metalborn.plugin.tinkers;

import com.google.gson.JsonObject;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.json.ingredient.IngredientWithMetal.MetalFilter;
import knightminer.metalborn.json.ingredient.MetalItemIngredient;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.FalseCondition;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.data.mock.MockRecipe;
import slimeknights.mantle.recipe.data.mock.MockRecipe.SimpleMockRecipe;
import slimeknights.mantle.recipe.data.mock.MockRecipeConsumer;

/** Builder for {@link MetalMeltingRecipe}. Safe to use even if Tinkers' Construct is not a runtime dependency. */
public class MetalMeltingRecipeBuilder extends AbstractRecipeBuilder<MetalMeltingRecipeBuilder> {
  private final Ingredient ingredient;
  private final int amount;

  protected MetalMeltingRecipeBuilder(Ingredient ingredient, int amount) {
    this.ingredient = ingredient;
    this.amount = amount;
  }

  /** Creates a new builder instance */
  public static MetalMeltingRecipeBuilder melting(Ingredient ingredient, int amount) {
    return new MetalMeltingRecipeBuilder(ingredient, amount);
  }

  /** Creates a new builder for the given item and filter */
  private static MetalMeltingRecipeBuilder melting(ItemLike item, MetalFilter filter, int amount) {
    return melting(MetalItemIngredient.of(item, filter), amount);
  }

  @Override
  public void save(RecipeOutput output) {
    save(output, Loadables.ITEM.getKey(ingredient.getItems()[0].getItem()));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    // TODO: can I safely call the constructor here?
    throw new UnsupportedOperationException();
  }

  /** Saves this recipe without requiring tinkers during datagen. */
  public void save(MockRecipeConsumer consumer) {
    save(consumer, Loadables.ITEM.getKey(ingredient.getItems()[0].getItem()));
  }

  /** Saves this recipe without requiring tinkers during datagen. */
  public void save(MockRecipeConsumer consumer, ResourceLocation id) {
    // FIXME: switch condition to TinkersMockRecipeBuilder.MOD_LOADED when tinkers is ported
    consumer.accept(new Finished(id), FalseCondition.INSTANCE);
    if (!criteria.isEmpty()) {
      consumer.advancement(id, "melting", criteria, FalseCondition.INSTANCE);
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
      return Registration.METAL_MELTING.getId();
    }

    @Override
    public void serializeRecipeData(JsonObject json, MockRecipe.Serializer serializer) {
      json.add("ingredient", serializer.ingredient(ingredient));
      json.addProperty("amount", amount);
    }
  }
}

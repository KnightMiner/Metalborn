package knightminer.metalborn.plugin.tinkers;

import com.google.gson.JsonObject;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.json.ingredient.MetalIngredient.MetalFilter;
import knightminer.metalborn.json.ingredient.MetalItemIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;

import java.util.function.Consumer;

/** Builder for {@link MetalMeltingRecipe}. Safe to use even if Tinkers' Construct is not a runtime dependency. */
public class MetalMeltingRecipeBuilder extends AbstractRecipeBuilder<MetalMeltingRecipeBuilder> {
  private final Ingredient ingredient;
  private final int amount;
  private boolean optional = true;

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

  /** Disables the recipe condition requiring Tinkers' Construct to be present. Use if adding via a Tinkers' Construct addon. */
  public MetalMeltingRecipeBuilder alwaysPresent() {
    this.optional = false;
    return this;
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, Loadables.ITEM.getKey(ingredient.getItems()[0].getItem()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = this.buildOptionalAdvancement(id, "casting");
    // not using loadables to prevent class loading with the builder when Tinkers' Construct is absent
    consumer.accept(new Finished(id, advancementId));
  }

  private class Finished extends AbstractFinishedRecipe {
    public Finished(ResourceLocation id, @Nullable ResourceLocation advancementId) {
      super(id, advancementId);
    }

    @Override
    public JsonObject serializeRecipe() {
      JsonObject json = new JsonObject();
      json.addProperty("type", Registration.METAL_MELTING.getId().toString());
      if (optional) {
        json.add("conditions", TinkersMockRecipeBuilder.MOD_LOADED_CONDITION);
      }
      this.serializeRecipeData(json);
      return json;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      json.add("ingredient", ingredient.toJson());
      json.addProperty("amount", amount);
    }

    @Override
    public RecipeSerializer<?> getType() {
      return Registration.METAL_MELTING.get();
    }
  }
}

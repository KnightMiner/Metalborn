package knightminer.metalborn.plugin.tinkers;

import com.google.gson.JsonObject;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.json.ingredient.MetalIngredient.MetalFilter;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.function.Consumer;

/** Builder for {@link MetalCastingRecipe}. Safe to use even if Tinkers' Construct is not a runtime dependency. */
public class MetalCastingRecipeBuilder extends AbstractRecipeBuilder<MetalCastingRecipeBuilder> {
  private final RegistryObject<RecipeSerializer<?>> serializer;
  private final ItemOutput result;
  private boolean optional = true;
  private Ingredient cast = Ingredient.EMPTY;
  private MetalFilter filter = MetalFilter.ANY;
  private int amount;
  private boolean consumed = false;
  private boolean switchSlots = false;

  private MetalCastingRecipeBuilder(RegistryObject<RecipeSerializer<?>> serializer, ItemOutput result) {
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

  /** Disables the recipe condition requiring Tinkers' Construct to be present. Use if adding via a Tinkers' Construct addon. */
  public MetalCastingRecipeBuilder alwaysPresent() {
    this.optional = false;
    return this;
  }


  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, Loadables.ITEM.getKey(result.get().getItem()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = this.buildOptionalAdvancement(id, "casting");
    // not using loadables to prevent class loading with the builder when Tinkers' Construct is absent
    consumer.accept(new Finished(id, advancementId));
  }

  /** Finished recipe instance */
  private class Finished extends AbstractFinishedRecipe {
    public Finished(ResourceLocation id, @Nullable ResourceLocation advancementId) {
      super(id, advancementId);
    }

    @Override
    public JsonObject serializeRecipe() {
      JsonObject json = new JsonObject();
      json.addProperty("type", serializer.getId().toString());
      if (optional) {
        json.add("conditions", TinkersMockRecipeBuilder.MOD_LOADED_CONDITION);
      }
      this.serializeRecipeData(json);
      return json;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      if (!group.isEmpty()) {
        json.addProperty("group", group);
      }
      if (cast != Ingredient.EMPTY) {
        json.add("cast", cast.toJson());
      }
      if (filter != MetalFilter.ANY) {
        json.add("filter", MetalFilter.LOADABLE.serialize(filter));
      }
      json.addProperty("amount", amount);
      json.add("result", result.serialize(false));
      if (consumed) {
        json.addProperty("cast_consumed", consumed);
      }
      if (switchSlots) {
        json.addProperty("switchSlots", switchSlots);
      }
    }

    @Override
    public RecipeSerializer<?> getType() {
      return serializer.get();
    }
  }
}

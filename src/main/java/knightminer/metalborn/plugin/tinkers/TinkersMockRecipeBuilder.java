package knightminer.metalborn.plugin.tinkers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import knightminer.metalborn.data.MetalIds;
import knightminer.metalborn.data.tag.MetalbornTags;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.recipe.MetalIngredient.MetalFilter;
import knightminer.metalborn.recipe.MetalItemIngredient;
import knightminer.metalborn.util.CastItemObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.mantle.registration.object.IdAwareObject;

import java.util.function.Consumer;

import static knightminer.metalborn.Metalborn.TINKERS;

/** Helpers for datagenning Tinkers' Construct recipes without adding Tinkers' as a datagen dependency. */
public class TinkersMockRecipeBuilder {
  static final JsonArray MOD_LOADED_CONDITION = CraftingHelper.serialize(new ICondition[]{new ModLoadedCondition(TINKERS)});
  private static final Ingredient SAND_CASTS = Ingredient.of(MetalbornTags.Items.SAND_CASTS);
  private static final Ingredient RED_SAND_CASTS = Ingredient.of(MetalbornTags.Items.RED_SAND_CASTS);

  /** Creates all recipes for creating casts in Tinkers' Construct */
  public static <T extends ItemLike & IdAwareObject> void castRecipes(Consumer<FinishedRecipe> consumer, T item, CastItemObject cast, MetalFilter filter, int cost, String folder) {
    ResourceLocation id = cast.getId();
    ResourceLocation root = id.withPrefix(folder);
    ResourceLocation casts = root.withSuffix("/cast/");
    ItemOutput sandCast = ItemOutput.fromItem(cast.getSand());
    ItemOutput redSandCast = ItemOutput.fromItem(cast.getRedSand());

    // part builder - makes sand casts and copper metalminds
    consumer.accept(new ItemPartRecipe(root.withSuffix("/part_builder"), location("copper"), id, Ingredient.EMPTY, cost, ItemOutput.fromStack(MetalItem.setMetal(new ItemStack(item), MetalIds.copper))));
    consumer.accept(new ItemPartRecipe(casts.withSuffix("sand_builder"),           id, SAND_CASTS,     sandCast));
    consumer.accept(new ItemPartRecipe(casts.withSuffix("red_sand_builder"),       id, RED_SAND_CASTS, redSandCast));
    consumer.accept(new ItemPartRecipe(casts.withSuffix("sand_builder_block"),     id, Ingredient.of(Blocks.SAND),     ItemOutput.fromItem(cast.getSand(), 4)));
    consumer.accept(new ItemPartRecipe(casts.withSuffix("red_sand_builder_block"), id, Ingredient.of(Blocks.RED_SAND), ItemOutput.fromItem(cast.getRedSand(), 4)));

    // molding - makes sand casts in the casting table
    Ingredient itemIngredient = MetalItemIngredient.of(item, filter);
    consumer.accept(new MoldingRecipe(casts.withSuffix("sand_molding"),     SAND_CASTS,     itemIngredient, sandCast));
    consumer.accept(new MoldingRecipe(casts.withSuffix("red_sand_molding"), RED_SAND_CASTS, itemIngredient, redSandCast));

    // make gold casts from casting
    consumer.accept(new GoldCastRecipe(casts.withSuffix("gold_casting"), itemIngredient, ItemOutput.fromItem(cast.get())));
  }

  /** Creates a tinkers namespace tag */
  private static ResourceLocation location(String name) {
    return new ResourceLocation(TINKERS, name);
  }

  /** Common logic for a mock recipe */
  private interface MockRecipe extends FinishedRecipe {
    @Override
    default RecipeSerializer<?> getType() {
      throw new UnsupportedOperationException();
    }

    /** Gets the recipe type ID */
    String getTypeId();

    @Override
    default JsonObject serializeRecipe() {
      JsonObject json = new JsonObject();
      json.addProperty("type", TINKERS + ':' + getTypeId());
      json.add("conditions", MOD_LOADED_CONDITION);
      this.serializeRecipeData(json);
      return json;
    }

    @Nullable
    @Override
    default JsonObject serializeAdvancement() {
      return null;
    }

    @Override
    default @Nullable ResourceLocation getAdvancementId() {
      return null;
    }
  }

  /** Finished recipe for an item part recipe */
  private record ItemPartRecipe(ResourceLocation getId, @Nullable ResourceLocation material, ResourceLocation pattern, Ingredient patternItem, int cost, ItemOutput result) implements MockRecipe {
    public ItemPartRecipe(ResourceLocation id, ResourceLocation pattern, Ingredient patternItem, ItemOutput result) {
      this(id, null, pattern, patternItem, 0, result);
    }

    @Override
    public String  getTypeId() {
      return "item_part_builder";
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      if (material != null) {
        json.addProperty("material", material.toString());
        json.addProperty("cost", cost);
      }
      json.addProperty("pattern", pattern.toString());
      if (patternItem != Ingredient.EMPTY) {
        json.add("pattern_item", patternItem.toJson());
      }
      json.add("result", result.serialize(true));
    }
  }

  /** Creates a casting table molding recipe */
  private record MoldingRecipe(ResourceLocation getId, Ingredient material, Ingredient pattern, ItemOutput result) implements MockRecipe {
    @Override
    public String getTypeId() {
      return "molding_table";
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      json.add("material", material.toJson());
      json.add("pattern", pattern.toJson());
      json.add("result", result.serialize(false));
    }
  }

  private record GoldCastRecipe(ResourceLocation getId, Ingredient cast, ItemOutput result) implements MockRecipe {
    private static final JsonElement GOLD_INGOT = ((FluidIngredient)FluidIngredient.of(FluidTags.create(location("molten_gold")), 90)).serialize();    private static final int COOLING_TIME = 57;

    @Override
    public String getTypeId() {
      return "casting_table";
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      json.add("cast", cast.toJson());
      json.addProperty("cast_consumed", true);

      json.add("fluid", GOLD_INGOT);
      json.addProperty("cooling_time", COOLING_TIME);

      json.add("result", result.serialize(false));
      json.addProperty("switch_slots", true);
    }
  }
}

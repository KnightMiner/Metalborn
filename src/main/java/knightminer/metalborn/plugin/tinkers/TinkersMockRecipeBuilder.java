package knightminer.metalborn.plugin.tinkers;

import com.google.gson.JsonArray;
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
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.mantle.registration.object.IdAwareObject;
import slimeknights.tconstruct.library.recipe.FluidValues;

import java.util.List;
import java.util.function.Consumer;

import static knightminer.metalborn.Metalborn.TINKERS;

/** Helpers for datagenning Tinkers' Construct recipes without adding Tinkers' as a datagen dependency. */
public class TinkersMockRecipeBuilder {
  /** JSON for the conditions for a recipe dependant on tinkers */
  static final JsonArray MOD_LOADED_CONDITION = CraftingHelper.serialize(new ICondition[]{new ModLoadedCondition(TINKERS)});
  /** Input for the sand cast part builder recipe */
  private static final Ingredient SAND_CASTS = Ingredient.of(MetalbornTags.Items.SAND_CASTS);
  /** Input for the red sand cast part builder recipe */
  private static final Ingredient RED_SAND_CASTS = Ingredient.of(MetalbornTags.Items.RED_SAND_CASTS);
  /** Input for casting a gold cast */
  private static final FluidIngredient GOLD_INGOT = FluidIngredient.of(FluidTags.create(Mantle.commonResource("molten_gold")), 90);
  /** Time for casting a gold cast */
  private static final int GOLD_INGOT_TIME = 57;
  /** Material ID for copper, the one part builder metalmind recipe */
  private static final ResourceLocation COPPER = new ResourceLocation(TINKERS, "copper");

  /** Creates all recipes for creating casts in Tinkers' Construct */
  public static void castRecipes(Consumer<FinishedRecipe> consumer, ItemLike metalItem, List<ItemLike> otherItems, CastItemObject cast, MetalFilter filter, int cost, String folder) {
    ResourceLocation id = cast.getId();
    ResourceLocation root = id.withPrefix(folder);
    ResourceLocation casts = root.withSuffix("/cast/");
    ItemOutput sandCast = ItemOutput.fromItem(cast.getSand());
    ItemOutput redSandCast = ItemOutput.fromItem(cast.getRedSand());

    // part builder - makes sand casts and copper metalminds
    consumer.accept(new ItemPartRecipe(root.withSuffix("/part_builder"), COPPER, id, Ingredient.EMPTY, cost, ItemOutput.fromStack(MetalItem.setMetal(new ItemStack(metalItem), MetalIds.copper))));
    consumer.accept(new ItemPartRecipe(casts.withSuffix("sand_builder"),           id, SAND_CASTS,     sandCast));
    consumer.accept(new ItemPartRecipe(casts.withSuffix("red_sand_builder"),       id, RED_SAND_CASTS, redSandCast));
    consumer.accept(new ItemPartRecipe(casts.withSuffix("sand_builder_block"),     id, Ingredient.of(Blocks.SAND),     ItemOutput.fromItem(cast.getSand(), 4)));
    consumer.accept(new ItemPartRecipe(casts.withSuffix("red_sand_builder_block"), id, Ingredient.of(Blocks.RED_SAND), ItemOutput.fromItem(cast.getRedSand(), 4)));

    // molding - makes sand casts in the casting table
    Ingredient itemIngredient = MetalItemIngredient.of(metalItem, filter);
    if (!otherItems.isEmpty()) {
      itemIngredient = CompoundIngredient.of(itemIngredient, Ingredient.of(otherItems.toArray(new ItemLike[0])));
    }
    consumer.accept(new MoldingRecipe(casts.withSuffix("sand_molding"),     SAND_CASTS,     itemIngredient, sandCast));
    consumer.accept(new MoldingRecipe(casts.withSuffix("red_sand_molding"), RED_SAND_CASTS, itemIngredient, redSandCast));

    // make gold casts from casting
    consumer.accept(new CastingTableRecipe(casts.withSuffix("gold_casting"), itemIngredient, true, true, GOLD_INGOT, GOLD_INGOT_TIME, ItemOutput.fromItem(cast.get())));
  }

  /** Creates recipes for melting and casting the given item */
  public static <T extends ItemLike & IdAwareObject> void meltingCasting(Consumer<FinishedRecipe> consumer, T item, CastItemObject cast, TagKey<Fluid> fluid, int amount, int temperature, String prefix) {
    ResourceLocation root = item.getId().withPath(prefix);
    consumer.accept(new MeltingRecipe(root.withSuffix("melting"), Ingredient.of(item), FluidOutput.fromTag(fluid, amount), temperature));
    int time = calcTimeForAmount(temperature, amount);
    consumer.accept(new CastingTableRecipe(root.withSuffix("casting_gold_cast"), Ingredient.of(cast.getMultiUseTag()),  false, false, FluidIngredient.of(fluid, amount), time, ItemOutput.fromItem(item)));
    consumer.accept(new CastingTableRecipe(root.withSuffix("casting_sand_cast"), Ingredient.of(cast.getSingleUseTag()), true,  false, FluidIngredient.of(fluid, amount), time, ItemOutput.fromItem(item)));
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

  /**
   * Calculates the temperature for a recipe based on the given temperature and factor
   * @param temperature  Required melting temperature
   * @param amount       Amount of relevant fluid
   * @return  Time for the recipe in celsius
   */
  private static int calcTimeForAmount(int temperature, int amount) {
    // base formula is temp^(.585), which will produce a time in 1/5th second increments
    return (int)Math.round((Math.pow(temperature + 300, 0.585f) * (float) Math.sqrt(amount / (float) FluidValues.INGOT)));
  }

  /** Creates a melting recipe with the given properties */
  private record MeltingRecipe(ResourceLocation getId, Ingredient ingredient, FluidOutput result, int temperature, int time) implements MockRecipe {
    public MeltingRecipe(ResourceLocation getId, Ingredient ingredient, FluidOutput result, int temperature) {
      this(getId, ingredient, result, temperature, calcTimeForAmount(temperature, result.getAmount()));
    }

    @Override
    public String getTypeId() {
      return "melting";
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      json.add("ingredient", ingredient.toJson());
      json.add("result", FluidOutput.Loadable.REQUIRED.serialize(result));
      json.addProperty("temperature", temperature);
      json.addProperty("time", time);
    }
  }

  /** Creates a casting table recipe with the given properties */
  private record CastingTableRecipe(ResourceLocation getId, Ingredient cast, boolean castConsumed, boolean switchSlots, FluidIngredient fluid, int time, ItemOutput result) implements MockRecipe {
    @Override
    public String getTypeId() {
      return "casting_table";
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      json.add("cast", cast.toJson());
      json.addProperty("cast_consumed", castConsumed);

      json.add("fluid", fluid.serialize());
      json.addProperty("cooling_time", time);

      json.add("result", result.serialize(false));
      json.addProperty("switch_slots", switchSlots);
    }
  }
}

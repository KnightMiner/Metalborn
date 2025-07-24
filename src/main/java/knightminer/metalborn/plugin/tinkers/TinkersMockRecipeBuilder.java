package knightminer.metalborn.plugin.tinkers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.data.MetalIds;
import knightminer.metalborn.data.tag.MetalbornTags;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.json.ingredient.IngredientWithMetal.MetalFilter;
import knightminer.metalborn.json.ingredient.MetalItemIngredient;
import knightminer.metalborn.util.CastItemObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.TrueCondition;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.mantle.recipe.data.ConsumerWrapperBuilder;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.mantle.registration.object.IdAwareObject;
import slimeknights.tconstruct.library.recipe.FluidValues;

import java.util.List;
import java.util.function.Consumer;

import static knightminer.metalborn.Metalborn.TINKERS;
import static slimeknights.mantle.Mantle.commonResource;

/** Helpers for datagenning Tinkers' Construct recipes without adding Tinkers' as a datagen dependency. */
public class TinkersMockRecipeBuilder {
  /** JSON for the conditions for a recipe dependant on tinkers */
  static final JsonArray MOD_LOADED_CONDITION = CraftingHelper.serialize(new ICondition[]{new ModLoadedCondition(TINKERS)});
  /** Input for the sand cast part builder recipe */
  private static final Ingredient SAND_CASTS = Ingredient.of(MetalbornTags.Items.SAND_CASTS);
  /** Input for the red sand cast part builder recipe */
  private static final Ingredient RED_SAND_CASTS = Ingredient.of(MetalbornTags.Items.RED_SAND_CASTS);
  /** Input for casting a gold cast */
  private static final FluidIngredient GOLD_INGOT = FluidIngredient.of(FluidTags.create(commonResource("molten_gold")), 90);
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
    consumer.accept(new CastingTableRecipe(casts.withSuffix("gold_casting"), itemIngredient, true, true, GOLD_INGOT, GOLD_INGOT_TIME, ItemOutput.fromItem(cast.get()), false));
  }

  /** Creates recipe to cast the given item */
  public static <T extends ItemLike & IdAwareObject> void casting(Consumer<FinishedRecipe> consumer, T item, boolean copyMetal, Ingredient cast, TagKey<Fluid> fluid, int amount, int temperature, String path) {
    int time = calcTimeForAmount(temperature, amount);
    consumer.accept(new CastingTableRecipe(item.getId().withPath(path), cast, true, false, FluidIngredient.of(fluid, amount), time, ItemOutput.fromItem(item), copyMetal));
  }

  /** Creates recipes for melting and casting the given item */
  public static <T extends ItemLike & IdAwareObject> void meltingCasting(Consumer<FinishedRecipe> consumer, T item, CastItemObject cast, TagKey<Fluid> fluid, int amount, int temperature, String prefix) {
    ResourceLocation root = item.getId().withPath(prefix);
    consumer.accept(new MeltingRecipe(root.withSuffix("melting"), Ingredient.of(item), FluidOutput.fromTag(fluid, amount), temperature));
    int time = calcTimeForAmount(temperature, amount);
    consumer.accept(new CastingTableRecipe(root.withSuffix("casting_gold_cast"), Ingredient.of(cast.getMultiUseTag()),  false, false, FluidIngredient.of(fluid, amount), time, ItemOutput.fromItem(item), false));
    consumer.accept(new CastingTableRecipe(root.withSuffix("casting_sand_cast"), Ingredient.of(cast.getSingleUseTag()), true,  false, FluidIngredient.of(fluid, amount), time, ItemOutput.fromItem(item), false));
  }

  /** Creates recipes for melting and casting the given identity metal item */
  public static <T extends ItemLike & IdAwareObject> void identityMeltingCasting(Consumer<FinishedRecipe> consumer, T item, CastItemObject cast, int aluminum, int quartz, String prefix) {
    TagKey<Fluid> aluminumTag = FluidTags.create(commonResource("molten_aluminum"));
    int aluminumTemperature = 425;
    TagKey<Fluid> quartzTag = FluidTags.create(new ResourceLocation(TINKERS, "molten_quartz"));
    int quartzTemperature = 637;

    // all recipes we plan to add will be conditioned on tinkers
    consumer = ConsumerWrapperBuilder.wrap().addCondition(new ModLoadedCondition(TINKERS)).build(consumer);
    addConditions = false;
    ResourceLocation root = item.getId().withPath(prefix);

    // if aluminum ingots are present, we use the aluminum recipes
    ICondition condition = new TagFilledCondition<>(ItemTags.create(commonResource("ingots/aluminum")));

    // melting
    Ingredient metalItem = Ingredient.of(item);
    ConditionalRecipe.builder()
      .addCondition(condition)
      .addRecipe(new MeltingRecipe(COPPER, metalItem, FluidOutput.fromTag(aluminumTag, aluminum), aluminumTemperature))
      .addCondition(TrueCondition.INSTANCE)
      .addRecipe(new MeltingRecipe(COPPER, metalItem, FluidOutput.fromTag(quartzTag, quartz), quartzTemperature))
      .build(consumer, root.withSuffix("melting"));

    // casting
    int aluminumTime = calcTimeForAmount(aluminumTemperature, aluminum);
    int quartzTime = calcTimeForAmount(quartzTemperature, quartz);
    FluidIngredient aluminumIngredient = FluidIngredient.of(aluminumTag, aluminum);
    FluidIngredient quartzIngredient = FluidIngredient.of(quartzTag, quartz);
    ItemOutput result = ItemOutput.fromItem(item);
    // gold cast
    Ingredient goldCast = Ingredient.of(cast.getMultiUseTag());
    ConditionalRecipe.builder()
      .addCondition(condition)
      .addRecipe(new CastingTableRecipe(COPPER, goldCast,false, false, aluminumIngredient, aluminumTime, result, false))
      .addCondition(TrueCondition.INSTANCE)
      .addRecipe(new CastingTableRecipe(COPPER, goldCast,false, false, quartzIngredient, quartzTime, result, false))
      .build(consumer, root.withSuffix("casting_gold_cast"));
    // sand cast
    Ingredient sandCast = Ingredient.of(cast.getSingleUseTag());
    ConditionalRecipe.builder()
      .addCondition(condition)
      .addRecipe(new CastingTableRecipe(COPPER, sandCast, true, false, aluminumIngredient, aluminumTime, result, false))
      .addCondition(TrueCondition.INSTANCE)
      .addRecipe(new CastingTableRecipe(COPPER, sandCast, true, false, quartzIngredient, quartzTime, result, false))
      .build(consumer, root.withSuffix("casting_sand_cast"));

    addConditions = true;
  }

  // simplest way to prevent the condition for the condition recipe, which will add it another way
  private static boolean addConditions = true;

  /** Common logic for a mock recipe */
  private interface MockRecipe extends FinishedRecipe {
    @Override
    default RecipeSerializer<?> getType() {
      throw new UnsupportedOperationException();
    }

    /** Gets the recipe type ID */
    String getTypeId();

    /** Gets the recipe type location */
    default String getTypeLocation() {
      return TINKERS + ':' + getTypeId();
    }

    @Override
    default JsonObject serializeRecipe() {
      JsonObject json = new JsonObject();
      json.addProperty("type", getTypeLocation());
      if (addConditions) {
        json.add("conditions", MOD_LOADED_CONDITION);
      }
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
  private record CastingTableRecipe(ResourceLocation getId, Ingredient cast, boolean castConsumed, boolean switchSlots, FluidIngredient fluid, int time, ItemOutput result, boolean copyMetal) implements MockRecipe {
    @Override
    public String getTypeId() {
      return "casting_table";
    }

    @Override
    public String getTypeLocation() {
      if (copyMetal) {
        return Registration.COPY_METAL_TABLE.getId().toString();
      }
      return MockRecipe.super.getTypeLocation();
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

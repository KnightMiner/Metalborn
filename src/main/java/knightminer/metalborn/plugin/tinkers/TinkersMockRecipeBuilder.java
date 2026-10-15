package knightminer.metalborn.plugin.tinkers;

import com.google.gson.JsonObject;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.data.MetalIds;
import knightminer.metalborn.data.tag.MetalbornTags;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.json.ingredient.IngredientWithMetal.MetalFilter;
import knightminer.metalborn.json.ingredient.MetalItemIngredient;
import knightminer.metalborn.util.CastItemObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.mantle.recipe.data.mock.MockRecipe.SimpleMockRecipe;
import slimeknights.mantle.recipe.data.mock.MockRecipeConsumer;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.library.recipe.FluidValues;

import java.util.List;

import static knightminer.metalborn.Metalborn.TINKERS;
import static slimeknights.mantle.Mantle.commonResource;

/** Helpers for datagenning Tinkers' Construct recipes without adding Tinkers' as a datagen dependency. */
public class TinkersMockRecipeBuilder {
  /** Condition for a recipe dependent on tinkers */
  static final ICondition MOD_LOADED = new ModLoadedCondition(TINKERS);
  /** Input for the sand cast part builder recipe */
  private static final Ingredient SAND_CASTS = Ingredient.of(MetalbornTags.Items.SAND_CASTS);
  /** Input for the red sand cast part builder recipe */
  private static final Ingredient RED_SAND_CASTS = Ingredient.of(MetalbornTags.Items.RED_SAND_CASTS);
  /** Input for casting a gold cast */
  private static final SizedFluidIngredient GOLD_INGOT = SizedFluidIngredient.of(FluidTags.create(commonResource("molten_gold")), 90);
  /** Time for casting a gold cast */
  private static final int GOLD_INGOT_TIME = 57;
  /** Material ID for copper, the one part builder metalmind recipe */
  private static final ResourceLocation COPPER = ResourceLocation.fromNamespaceAndPath(TINKERS, "copper");
  /** Title key for sand cast creation */
  private static final ResourceLocation SAND_CAST_TITLE = ResourceLocation.fromNamespaceAndPath(TINKERS, "sand_cast");

  /** Creates all recipes for creating casts in Tinkers' Construct */
  public static void castRecipes(MockRecipeConsumer consumer, ItemLike metalItem, List<ItemLike> otherItems, CastItemObject cast, MetalFilter filter, int cost, String folder) {
    ResourceLocation id = cast.getId();
    ResourceLocation root = id.withPrefix(folder);
    ResourceLocation casts = root.withSuffix("/cast/");
    ItemOutput sandCast = ItemOutput.fromItem(cast.getSand());
    ItemOutput redSandCast = ItemOutput.fromItem(cast.getRedSand());

    // part builder - makes sand casts and copper metalminds
    consumer.accept(new ItemPartRecipe(root.withSuffix("/part_builder"), COPPER, id, Ingredient.EMPTY, cost, ItemOutput.fromStack(MetalItem.setMetal(new ItemStack(metalItem), MetalIds.copper))));
    consumer.accept(new ItemPartRecipe(casts.withSuffix("sand_builder"),           SAND_CAST_TITLE, id, SAND_CASTS,     sandCast));
    consumer.accept(new ItemPartRecipe(casts.withSuffix("red_sand_builder"),       SAND_CAST_TITLE, id, RED_SAND_CASTS, redSandCast));
    consumer.accept(new ItemPartRecipe(casts.withSuffix("sand_builder_block"),     SAND_CAST_TITLE, id, Ingredient.of(Blocks.SAND),     ItemOutput.fromItem(cast.getSand(), 4)));
    consumer.accept(new ItemPartRecipe(casts.withSuffix("red_sand_builder_block"), SAND_CAST_TITLE, id, Ingredient.of(Blocks.RED_SAND), ItemOutput.fromItem(cast.getRedSand(), 4)));

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
  public static void casting(MockRecipeConsumer consumer, DeferredItem<?> item, boolean copyMetal, Ingredient cast, TagKey<Fluid> fluid, int amount, int temperature, String path) {
    int time = calcTimeForAmount(temperature, amount);
    consumer.accept(new CastingTableRecipe(item.getId().withPath(path), cast, true, false, SizedFluidIngredient.of(fluid, amount), time, ItemOutput.fromItem(item), copyMetal));
  }

  /** Creates recipes for melting and casting the given item */
  public static void meltingCasting(MockRecipeConsumer consumer, DeferredItem<?> item, CastItemObject cast, TagKey<Fluid> fluid, int amount, int temperature, String prefix) {
    ResourceLocation root = item.getId().withPath(prefix);
    consumer.accept(new MeltingRecipe(root.withSuffix("melting"), Ingredient.of(item), FluidOutput.fromTag(fluid, amount), temperature));
    int time = calcTimeForAmount(temperature, amount);
    consumer.accept(new CastingTableRecipe(root.withSuffix("casting_gold_cast"), Ingredient.of(cast.getMultiUseTag()),  false, false, SizedFluidIngredient.of(fluid, amount), time, ItemOutput.fromItem(item), false));
    consumer.accept(new CastingTableRecipe(root.withSuffix("casting_sand_cast"), Ingredient.of(cast.getSingleUseTag()), true,  false, SizedFluidIngredient.of(fluid, amount), time, ItemOutput.fromItem(item), false));
  }

  /** Creates recipes for melting and casting the given identity metal item */
  public static void identityMeltingCasting(MockRecipeConsumer consumer, DeferredItem<?> item, CastItemObject cast, int aluminum, int quartz, String prefix) {
    TagKey<Fluid> aluminumTag = FluidTags.create(commonResource("molten_aluminum"));
    int aluminumTemperature = 425;
    TagKey<Fluid> quartzTag = FluidTags.create(ResourceLocation.fromNamespaceAndPath(TINKERS, "molten_quartz"));
    int quartzTemperature = 637;

    // all recipes we plan to add will be conditioned on tinkers
    ResourceLocation root = item.getId().withPath(prefix);

    // if aluminum ingots are present, we use the aluminum recipes
    ICondition condition = new TagFilledCondition<>(ItemTags.create(commonResource("ingots/aluminum")));
    // if not, use quartz recipes
    ICondition notCondition = new NotCondition(condition);

    // melting
    Ingredient metalItem = Ingredient.of(item);
    consumer.accept(new MeltingRecipe(root.withSuffix("melting_aluminum"), metalItem, FluidOutput.fromTag(aluminumTag, aluminum), aluminumTemperature), MOD_LOADED, condition);
    consumer.accept(new MeltingRecipe(root.withSuffix("melting_quartz"),   metalItem, FluidOutput.fromTag(quartzTag, quartz),     quartzTemperature),   MOD_LOADED, notCondition);

    // casting
    int aluminumTime = calcTimeForAmount(aluminumTemperature, aluminum);
    int quartzTime = calcTimeForAmount(quartzTemperature, quartz);
    SizedFluidIngredient aluminumIngredient = SizedFluidIngredient.of(aluminumTag, aluminum);
    SizedFluidIngredient quartzIngredient = SizedFluidIngredient.of(quartzTag, quartz);
    ItemOutput result = ItemOutput.fromItem(item);
    // gold cast
    Ingredient goldCast = Ingredient.of(cast.getMultiUseTag());
    consumer.accept(new CastingTableRecipe(root.withSuffix("casting_gold_cast_aluminum"), goldCast,false, false, aluminumIngredient, aluminumTime, result, false), MOD_LOADED, condition);
    consumer.accept(new CastingTableRecipe(root.withSuffix("casting_gold_cast_quartz"),   goldCast,false, false, quartzIngredient,   quartzTime,   result, false), MOD_LOADED, notCondition);
    // sand cast
    Ingredient sandCast = Ingredient.of(cast.getSingleUseTag());
    consumer.accept(new CastingTableRecipe(root.withSuffix("casting_sand_cast_aluminum"), sandCast, true, false, aluminumIngredient, aluminumTime, result, false), MOD_LOADED, condition);
    consumer.accept(new CastingTableRecipe(root.withSuffix("casting_sand_cast_quartz"),   sandCast, true, false, quartzIngredient,   quartzTime,   result, false), MOD_LOADED, notCondition);
  }

  /** Common logic for a mock recipe */
  private interface MockTinkersRecipe extends SimpleMockRecipe {
    /** Gets the recipe type ID */
    String serializerName();

    @Override
    default ResourceLocation serializerId() {
      return ResourceLocation.fromNamespaceAndPath(TINKERS, serializerName());
    }

    @Override
    default JsonObject serializeRecipe(Serializer serializer) {
      JsonObject json = SimpleMockRecipe.super.serializeRecipe(serializer);
      serializer.addConditions(json, MOD_LOADED);
      return json;
    }
  }

  /** Finished recipe for an item part recipe */
  private record ItemPartRecipe(ResourceLocation id, @Nullable ResourceLocation materialOrTitle, ResourceLocation pattern, Ingredient patternItem, int cost, ItemOutput result) implements MockTinkersRecipe {
    public ItemPartRecipe(ResourceLocation id, ResourceLocation title, ResourceLocation pattern, Ingredient patternItem, ItemOutput result) {
      this(id, title, pattern, patternItem, 0, result);
    }

    @Override
    public String serializerName() {
      return "item_part_builder";
    }

    @Override
    public void serializeRecipeData(JsonObject json, Serializer serializer) {
      if (materialOrTitle != null) {
        if (cost > 0) {
          json.addProperty("material", materialOrTitle.toString());
          json.addProperty("cost", cost);
        } else {
          json.addProperty("title_key", materialOrTitle.toString());
        }
      }
      json.addProperty("pattern", pattern.toString());
      if (patternItem != Ingredient.EMPTY) {
        json.add("pattern_item", serializer.ingredient(patternItem));
      }
      json.add("result", serializer.stack(result));
    }
  }

  /** Creates a casting table molding recipe */
  private record MoldingRecipe(ResourceLocation id, Ingredient material, Ingredient pattern, ItemOutput result) implements MockTinkersRecipe {
    @Override
    public String serializerName() {
      return "molding_table";
    }

    @Override
    public void serializeRecipeData(JsonObject json, Serializer serializer) {
      json.add("material", serializer.serialize(Ingredient.CODEC_NONEMPTY, material));
      json.add("pattern", serializer.serialize(Ingredient.CODEC_NONEMPTY, pattern));
      json.add("result", serializer.item(result));
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
  private record MeltingRecipe(ResourceLocation id, Ingredient ingredient, FluidOutput result, int temperature, int time) implements MockTinkersRecipe {
    public MeltingRecipe(ResourceLocation getId, Ingredient ingredient, FluidOutput result, int temperature) {
      this(getId, ingredient, result, temperature, calcTimeForAmount(temperature, result.getAmount()));
    }

    @Override
    public String serializerName() {
      return "melting";
    }

    @Override
    public void serializeRecipeData(JsonObject json, Serializer serializer) {
      json.add("ingredient", serializer.ingredient(ingredient));
      json.add("result", serializer.fluid(result));
      json.addProperty("temperature", temperature);
      json.addProperty("time", time);
    }
  }

  /** Creates a casting table recipe with the given properties */
  private record CastingTableRecipe(ResourceLocation id, Ingredient cast, boolean castConsumed, boolean switchSlots, SizedFluidIngredient fluid, int time, ItemOutput result, boolean copyMetal) implements MockTinkersRecipe {
    @Override
    public String serializerName() {
      return "casting_table";
    }

    @Override
    public ResourceLocation serializerId() {
      if (copyMetal) {
        return Registration.COPY_METAL_TABLE.getId();
      }
      return MockTinkersRecipe.super.serializerId();
    }

    @Override
    public void serializeRecipeData(JsonObject json, Serializer serializer) {
      json.add("cast", serializer.ingredient(cast));
      json.addProperty("cast_consumed", castConsumed);

      json.add("fluid", serializer.fluidIngredient(fluid));
      json.addProperty("cooling_time", time);

      json.add("result", serializer.item(result));
      json.addProperty("switch_slots", switchSlots);
    }
  }
}

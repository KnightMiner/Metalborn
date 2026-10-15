package knightminer.metalborn.data;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.data.tag.MetalbornTags;
import knightminer.metalborn.item.SatchelItem.SatchelType;
import knightminer.metalborn.json.ingredient.FillableIngredient;
import knightminer.metalborn.json.ingredient.FillableIngredient.FillState;
import knightminer.metalborn.json.ingredient.IngredientWithMetal.MetalFilter;
import knightminer.metalborn.json.ingredient.MetalItemIngredient;
import knightminer.metalborn.json.ingredient.MetalShapeIngredient;
import knightminer.metalborn.json.recipe.cooking.CookingMetalRecyclingBuilder;
import knightminer.metalborn.json.recipe.forge.ShapedForgeRecipeBuilder;
import knightminer.metalborn.json.recipe.forge.ShapelessForgeRecipeBuilder;
import knightminer.metalborn.metal.MetalShape;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.mantle.recipe.cooking.CookingRecipeBuilder;
import slimeknights.mantle.recipe.data.ICommonRecipeHelper;

import java.util.concurrent.CompletableFuture;

import static slimeknights.mantle.Mantle.commonResource;

/** Adds all metalborn crafting recipes */
public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider implements ICommonRecipeHelper {
  public RecipeProvider(PackOutput packOutput, CompletableFuture<Provider> registries) {
    super(packOutput, registries);
  }

  @Override
  public String getModId() {
    return Metalborn.MOD_ID;
  }

  @Override
  protected void buildRecipes(RecipeOutput output) {
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.METALLIC_ARTS)
      .requires(Items.BOOK)
      .requires(Registration.TIN.getIngotTag())
      .unlockedBy("has_tin", has(Registration.TIN.getIngotTag()))
      .save(output, location("metallic_arts"));

    String metalFolder = "metal/";
    metalCrafting(output, Registration.TIN, metalFolder);
    metalCrafting(output, Registration.PEWTER, metalFolder);
    metalCrafting(output, Registration.STEEL, metalFolder);
    metalCrafting(output, Registration.BRONZE, metalFolder);
    metalCrafting(output, Registration.ROSE_GOLD, metalFolder);
    metalCrafting(output, Registration.NICROSIL, metalFolder);
    packingRecipe(output, RecipeCategory.MISC, "ingot", Items.COPPER_INGOT, "nugget", Registration.COPPER_NUGGET, MetalbornTags.Items.COPPER_NUGGETS, metalFolder);
    packingRecipe(output, RecipeCategory.MISC, "scrap", Items.NETHERITE_SCRAP, "shard", Registration.NETHERITE_SHARD, MetalbornTags.Items.NETHERITE_SCRAP_NUGGETS, metalFolder);
    packingRecipe(output, RecipeCategory.MISC, "ingot", Items.NETHERITE_INGOT, "nugget", Registration.NETHERITE_NUGGET, MetalbornTags.Items.NETHERITE_NUGGETS, metalFolder);

    // tin ore
    CookingRecipeBuilder.builder(Registration.TIN.getIngot())
      .experience(0.7f)
      .requires(Ingredient.of(Registration.RAW_TIN, Registration.TIN_ORE, Registration.DEEPSLATE_TIN_ORE))
      .unlockedBy("has_raw", has(Registration.RAW_TIN))
      .unlockedBy("has_ore", has(Registration.TIN_ORE))
      .unlockedBy("has_deepslate", has(Registration.DEEPSLATE_TIN_ORE))
      .saveSmelting(output, location(metalFolder + "tin_ingot_smelting"))
      .saveBlasting(output, location(metalFolder + "tin_ingot_blasting"));
    packingRecipe(output, RecipeCategory.MISC, "raw_block", Registration.RAW_TIN_BLOCK, "raw", Registration.RAW_TIN, MetalbornTags.Items.RAW_TIN, metalFolder);

    // ferring nuggets
    ShapelessForgeRecipeBuilder.shapeless(Registration.RANDOM_FERRING)
      .requires(Registration.NICROSIL.getNuggetTag())
      .requires(FillableIngredient.filled(MetalItemIngredient.of(Registration.SPIKE, MetalFilter.SPIKE)))
      .cookingRate(2)
      .experience(3f)
      .save(output);
    ShapelessForgeRecipeBuilder.shapeless(Registration.CHANGE_FERRING)
      .requires(MetalbornTags.Items.NETHERITE_NUGGETS)
      .requires(FillableIngredient.filled(MetalItemIngredient.of(Registration.SPIKE, MetalFilter.SPIKE)))
      .metal()
      .cookingRate(2)
      .experience(3f)
      .save(output);

    // forge
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Registration.FORGE)
      .define('#', Blocks.SMOOTH_SANDSTONE)
      .define('X', Blocks.FURNACE)
      .define('I', Registration.TIN.getIngotTag())
      .pattern("III").pattern("IXI").pattern("###")
      .unlockedBy("has_tin", has(Registration.TIN.getIngotTag()))
      .save(output, location("forge"));

    // satchels
    ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Registration.SATCHEL.get(SatchelType.BRONZE))
      .define('I', Registration.BRONZE.getIngotTag())
      .define('s', Tags.Items.STRINGS)
      .define('c', Tags.Items.GEMS_QUARTZ)
      .pattern("sIs").pattern("IcI").pattern("III")
      .unlockedBy("has_ingot", has(Registration.BRONZE.getIngotTag()))
      .save(output, location("bronze_satchel"));
    ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Registration.SATCHEL.get(SatchelType.PEWTER))
      .define('I', Registration.PEWTER.getIngotTag())
      .define('s', Tags.Items.STRINGS)
      .define('c', Tags.Items.GEMS_DIAMOND)
      .pattern("sIs").pattern("IcI").pattern("III")
      .unlockedBy("has_ingot", has(Registration.PEWTER.getIngotTag()))
      .save(output, location("pewter_satchel"));
    ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Registration.SATCHEL.get(SatchelType.STEEL))
      .define('I', Registration.STEEL.getIngotTag())
      .define('s', Tags.Items.STRINGS)
      .define('c', Tags.Items.INGOTS_NETHERITE)
      .pattern("sIs").pattern("IcI").pattern("III")
      .unlockedBy("has_ingot", has(Registration.STEEL.getIngotTag()))
      .save(output, location("steel_satchel"));
    ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Registration.SATCHEL.get(SatchelType.NICROSIL))
      .define('I', Registration.NICROSIL.getIngotTag())
      .define('s', Tags.Items.STRINGS)
      .define('c', Items.ECHO_SHARD)
      .pattern("sIs").pattern("IcI").pattern("III")
      .unlockedBy("has_ingot", has(Registration.NICROSIL.getIngotTag()))
      .save(output, location("nicrosil_satchel"));

    // metal items
    // rings
    ShapedForgeRecipeBuilder.shaped(Registration.RING)
      .pattern("##").pattern("##")
      .define('#', MetalShapeIngredient.nugget(MetalFilter.METALMIND))
      .metal()
      .cookingRate(1)
      .experience(0.5f)
      .save(output, location("ring/metal"));
    ShapedForgeRecipeBuilder.shaped(Registration.INVESTITURE_RING)
      .pattern("##").pattern("##")
      .define('#', Registration.NICROSIL.getNuggetTag())
      .cookingRate(1)
      .experience(0.5f)
      .save(output, location("ring/nicrosil"));
    // identity uses quartz if no aluminum
    TagKey<Item> aluminumNugget = ItemTags.create(commonResource("nuggets/aluminum"));
    ICondition hasAluminumNugget = new TagFilledCondition<>(aluminumNugget);
    ShapedForgeRecipeBuilder.shaped(Registration.IDENTITY_RING)
      .pattern("##").pattern("##")
      .define('#', aluminumNugget)
      .cookingRate(1)
      .experience(0.5f)
      .save(output.withConditions(hasAluminumNugget), location("ring/identity_aluminum"));
    ShapelessForgeRecipeBuilder.shapeless(Registration.IDENTITY_RING)
      // if we gave 2, this would make the ring cost 1/8 instead of 1/9
      // ultimately decided on just 1, making it 1/4, as we get better recycling. Means it's a bit more expensive, but identity is niche
      .requires(Tags.Items.GEMS_QUARTZ)
      .cookingRate(1)
      .experience(1.0f)
      .save(output.withConditions(new NotCondition(hasAluminumNugget)), location("ring/identity_quartz"));

    // bracers
    ShapedForgeRecipeBuilder.shaped(Registration.BRACER)
      .pattern("##").pattern("##")
      .define('#', MetalShapeIngredient.ingot(MetalFilter.METALMIND))
      .metal()
      .cookingRate(4)
      .experience(2f)
      .save(output, location("bracer/metal"));
    ShapedForgeRecipeBuilder.shaped(Registration.INVESTITURE_BRACER)
      .pattern("##").pattern("##")
      .define('#', Registration.NICROSIL.getIngotTag())
      .cookingRate(4)
      .experience(2f)
      .save(output, location("bracer/nicrosil"));
    // identity uses quartz if no aluminum
    ICondition hasAluminumIngot = ingotCondition("aluminum");
    ShapedForgeRecipeBuilder.shaped(Registration.IDENTITY_BRACER)
      .pattern("##").pattern("##")
      .define('#', ingot("aluminum"))
      .cookingRate(4)
      .experience(2f)
      .save(output.withConditions(hasAluminumIngot), location("bracer/identity_aluminum"));
    ShapedForgeRecipeBuilder.shaped(Registration.IDENTITY_BRACER)
      .pattern("##").pattern("##")
      .define('#', Tags.Items.GEMS_QUARTZ)
      .cookingRate(4)
      .experience(2f)
      .save(output.withConditions(new NotCondition(hasAluminumIngot)), location("bracer/identity_quartz"));
    // spikes
    ShapedForgeRecipeBuilder.shaped(Registration.SPIKE)
      .pattern(" #").pattern("# ")
      .define('#', MetalShapeIngredient.ingot(MetalFilter.SPIKE))
      .metal()
      .cookingRate(4) // you get 4 spikes per fuel usage, but you really want hoppers since they are unstackable
      .experience(1f)
      .save(output, location("spike/metal"));
    ShapedForgeRecipeBuilder.shaped(Registration.INVESTITURE_SPIKE)
      .pattern(" #").pattern("# ")
      .define('#', Registration.NICROSIL.getIngotTag())
      .cookingRate(4)
      .experience(1f)
      .save(output, location("spike/nicrosil"));

    // unsealed is crafted from a full nicrosil metalmind and a matching empty metalmind
    ShapelessForgeRecipeBuilder.shapeless(Registration.UNSEALED_RING)
      .requires(MetalbornTags.Items.NETHERITE_NUGGETS)
      .requires(FillableIngredient.of(FillState.EMPTY, MetalItemIngredient.of(Registration.RING, MetalFilter.METALMIND)))
      .requires(FillableIngredient.of(FillState.UNSEALED, MetalItemIngredient.of(Registration.INVESTITURE_RING, MetalFilter.METALMIND)))
      .metal()
      .cookingRate(4)
      .experience(0.5f)
      .save(output, location("ring/unsealed"));
    // soulbound requires a full identity ring
    ShapelessForgeRecipeBuilder.shapeless(Registration.SOULBOUND_RING)
      .requires(Items.ENDER_PEARL)
      .requires(FillableIngredient.of(FillState.EMPTY, MetalItemIngredient.of(Registration.RING, MetalFilter.METALMIND)))
      .requires(FillableIngredient.of(FillState.FILLED, Ingredient.of(Registration.IDENTITY_RING)))
      .metal()
      .cookingRate(4)
      .experience(0.5f)
      .save(output, location("ring/soulbound"));
    ShapelessForgeRecipeBuilder.shapeless(Registration.SOULBOUND_IDENTITY_RING)
      .requires(Items.ENDER_PEARL)
      .requires(FillableIngredient.of(FillState.EMPTY, Ingredient.of(Registration.IDENTITY_RING)))
      .requires(FillableIngredient.of(FillState.FILLED, Ingredient.of(Registration.IDENTITY_RING)))
      .cookingRate(4)
      .experience(0.5f)
      .save(output, location("ring/soulbound_identity"));
    ShapelessForgeRecipeBuilder.shapeless(Registration.SOULBOUND_INVESTITURE_RING)
      .requires(Items.ENDER_PEARL)
      .requires(FillableIngredient.of(FillState.EMPTY, Ingredient.of(Registration.INVESTITURE_RING)))
      .requires(FillableIngredient.of(FillState.FILLED, Ingredient.of(Registration.IDENTITY_RING)))
      .cookingRate(4)
      .experience(0.5f)
      .save(output, location("ring/soulbound_investiture"));
    // soulbound spikes require full spikes of both variants, plus some echo
    ShapelessForgeRecipeBuilder.shapeless(Registration.SOULBOUND_SPIKE)
      .requires(Items.ECHO_SHARD)
      .requires(FillableIngredient.of(FillState.FILLED, MetalItemIngredient.of(Registration.SPIKE, MetalFilter.SPIKE)))
      .requires(FillableIngredient.of(FillState.FILLED, MetalItemIngredient.of(Registration.INVESTITURE_SPIKE, MetalFilter.SPIKE)))
      .metal()
      .cookingRate(4)
      .experience(0.5f)
      .save(output, location("spike/soulbound"));

    // alloys
    String alloyFolder = "alloy/";
    // base mod
    // use lead instead of iron if its present, as thats closer to the real recipe for pewter
    ICondition hasLeadIngot = ingotLikeCondition("lead");
    ShapelessForgeRecipeBuilder.shapeless(Registration.PEWTER.getIngotTag(), 4)
      .requires(ingotLike("tin"), 3)
      .requires(ingotLike("lead"), 1)
      .save(output.withConditions(hasLeadIngot), location(alloyFolder + "pewter_from_lead"));
    ShapelessForgeRecipeBuilder.shapeless(Registration.PEWTER.getIngotTag(), 4)
      .requires(ingotLike("tin"), 3)
      .requires(ingotLike("iron"), 1)
      .save(output.withConditions(new NotCondition(hasLeadIngot)), location(alloyFolder + "pewter_from_iron"));
    ShapelessForgeRecipeBuilder.shapeless(Registration.STEEL.getIngotTag(), 1)
      .requires(ingotLike("iron"), 1)
      .requires(Items.BLAZE_POWDER, 1)
      .requires(Blocks.SAND, 1)
      .cookingRate(4)
      .save(output, location(alloyFolder + "steel"));
    ShapelessForgeRecipeBuilder.shapeless(Registration.BRONZE.getIngotTag(), 4)
      .requires(ingotLike("copper"), 3)
      .requires(ingotLike("tin"), 1)
      .experience(0.7f)
      .save(output, location(alloyFolder + "bronze"));
    ShapelessForgeRecipeBuilder.shapeless(Registration.ROSE_GOLD.getIngotTag(), 2)
      .requires(ingotLike("gold"), 1)
      .requires(ingotLike("copper"), 1)
      .save(output, location(alloyFolder + "rose_gold"));
    // netherite is normally 4 scrap + 4 gold = 1 ingot
    // this recipe makes it effectively 3 scrap + 3 gold = 1 ingot, a 1 scrap discount!
    ShapelessForgeRecipeBuilder.shapeless(MetalbornTags.Items.NETHERITE_NUGGETS, 3)
      .requires(ingotLike("gold"), 1)
      .requires(ingotLike("netherite_scrap"), 1)
      .cookingRate(2)
      .save(output, location(alloyFolder + "netherite"));

    // nicrosil alloying
    // if available, use nickel in the recipe, though craft it without nickel if missing
    ICondition hasChromiumIngot = ingotLikeCondition("chromium");
    ICondition hasNickelIngot = ingotLikeCondition("nickel");
    ICondition noChromiumIngot = new NotCondition(hasChromiumIngot);
    ICondition noNickelIngot = new NotCondition(hasNickelIngot);
    // nickel + chromium - proper recipe
    ShapelessForgeRecipeBuilder.shapeless(Registration.NICROSIL.getIngotTag(), 4)
      .requires(ingotLike("nickel"), 2)
      .requires(ingotLike("chromium"), 1)
      .requires(ingotLike("quartz"), 1)
      .save(output.withConditions(hasChromiumIngot, hasNickelIngot), location(alloyFolder + "nicrosil/chromium_nickel"));
    // no chromium? replace it with emerald (trace chromium)
    ShapelessForgeRecipeBuilder.shapeless(Registration.NICROSIL.getIngotTag(), 4)
      .requires(ingotLike("nickel"), 2)
      .requires(ingotLike("emerald"), 1)
      .requires(ingotLike("quartz"), 1)
      .save(output.withConditions(noChromiumIngot, hasNickelIngot), location(alloyFolder + "nicrosil/emerald_nickel"));
    // no nickel but chromium? use the allomancy recipe
    ShapelessForgeRecipeBuilder.shapeless(Registration.NICROSIL.getIngotTag(), 4)
      .requires(ingotLike("chromium"), 2)
      .requires(ingotLike("iron"), 1)
      .requires(ingotLike("quartz"), 1)
      .save(output.withConditions(hasChromiumIngot, noNickelIngot), location(alloyFolder + "nicrosil/chromium_iron"));
    // no nickel or chromium? sub for tin and emerald
    ShapelessForgeRecipeBuilder.shapeless(Registration.NICROSIL.getIngotTag(), 4)
      .requires(ingotLike("tin"), 2)
      .requires(ingotLike("emerald"), 1)
      .requires(ingotLike("quartz"), 1)
      .save(output.withConditions(noChromiumIngot, noNickelIngot), location(alloyFolder + "nicrosil/emerald_tin"));

    // general compat
    ShapelessForgeRecipeBuilder.shapeless(ingot("brass"), 2)
      .requires(ingotLike("copper"), 1)
      .requires(ingotLike("zinc"), 1)
      .experience(1.0f)
      .save(output.withConditions(ingotCondition("brass"), ingotLikeCondition("zinc")), location(alloyFolder + "brass"));
    ShapelessForgeRecipeBuilder.shapeless(ingot("constantan"), 2)
      .requires(ingotLike("copper"), 1)
      .requires(ingotLike("nickel"), 1)
      .experience(0.7f)
      .save(output.withConditions(ingotCondition("constantan"), ingotLikeCondition("nickel")), location(alloyFolder + "constantan"));
    ShapelessForgeRecipeBuilder.shapeless(ingot("electrum"), 2)
      .requires(ingotLike("gold"), 1)
      .requires(ingotLike("silver"), 1)
      .save(output.withConditions(ingotCondition("electrum"), ingotLikeCondition("silver")), location(alloyFolder + "electrum"));
    ShapelessForgeRecipeBuilder.shapeless(ingot("invar"), 3)
      .requires(ingotLike("iron"), 2)
      .requires(ingotLike("nickel"), 1)
      .save(output.withConditions(ingotCondition("invar"), ingotLikeCondition("nickel")), location(alloyFolder + "invar"));
    ShapelessForgeRecipeBuilder.shapeless(ingot("duralumin"), 3)
      .requires(ingotLike("aluminum"), 3)
      .requires(ingotLike("copper"), 1)
      .save(output.withConditions(ingotCondition("duralumin"), ingotLikeCondition("aluminum")), location(alloyFolder + "duralumin"));
    ShapelessForgeRecipeBuilder.shapeless(ingot("bendalloy"), 4)
      .requires(ingotLike("lead"), 2)
      .requires(ingotLike("tin"), 1)
      .requires(ingotLike("cadmium"), 1)
      .save(output.withConditions(ingotCondition("bendalloy"), ingotLikeCondition("lead"), ingotLikeCondition("cadmium")), location(alloyFolder + "bendalloy"));

    // tinkers compat
    ShapelessForgeRecipeBuilder.shapeless(ingot("amethyst_bronze"), 1)
      .requires(ingotLike("copper"), 1)
      .requires(Items.AMETHYST_SHARD, 1)
      .experience(0.7f)
      .save(output.withConditions(ingotCondition("amethyst_bronze")), location(alloyFolder + "amethyst_bronze"));
    ShapelessForgeRecipeBuilder.shapeless(ingot("hepatizon"), 2)
      .requires(ingotLike("copper"), 2)
      .requires(ingotLike("cobalt"), 1)
      .requires(MetalbornTags.Items.QUARTZ_LIKE, 1)
      .experience(2f)
      .save(output.withConditions(ingotCondition("hepatizon"), ingotLikeCondition("cobalt")), location(alloyFolder + "hepatizon"));
    ShapelessForgeRecipeBuilder.shapeless(ingot("manyullyn"), 4)
      .requires(ingotLike("cobalt"), 3)
      .requires(MetalbornTags.Items.SCRAP_LIKE, 1)
      .experience(2f)
      .save(output.withConditions(ingotCondition("manyullyn"), ingotLikeCondition("cobalt")), location(alloyFolder + "manyullyn"));
    // TODO: consider slime metals


    // recycling - all done in the furnace and blast furnace
    String recyclingFolder = "recycling/";
    recycle(output, Registration.BRACER, MetalFilter.METALMIND, MetalShape.INGOT,  4, 800, recyclingFolder + "bracer/metal");
    recycle(output, Registration.RING,   MetalFilter.METALMIND, MetalShape.NUGGET, 4, 200, recyclingFolder + "ring/metal");
    recycle(output, Registration.SPIKE,  MetalFilter.SPIKE,     MetalShape.INGOT,  2, 400, recyclingFolder + "spike/metal");
    recycle(output, Registration.INVESTITURE_BRACER, Registration.NICROSIL.getIngotTag(),  4, 800, recyclingFolder + "bracer/nicrosil");
    recycle(output, Registration.INVESTITURE_RING,   Registration.NICROSIL.getNuggetTag(), 4, 200, recyclingFolder + "ring/nicrosil");
    recycle(output, Registration.INVESTITURE_SPIKE,  Registration.NICROSIL.getIngotTag(),  2, 400, recyclingFolder + "spike/nicrosil");

    // identity is a pain as it has two possibilities, so recycle into the one present
    recycleIdentity(output, Registration.IDENTITY_BRACER, ingot("aluminum"), 4, recyclingFolder + "bracer/identity");
    recycleIdentity(output, Registration.IDENTITY_RING,   aluminumNugget,    1, recyclingFolder + "ring/identity");
  }


  /** Makes an ingredient matching items that smelt into 1 ingot of material, or are 1 ingot of material */
  private static Ingredient ingotLike(String name) {
    return Ingredient.of(ItemTags.create(Metalborn.resource("ingot_like/" + name)));
  }

  /** Creates a condition for a recipe requiring the given tag be filled */
  private static ICondition tagCondition(ResourceLocation tag) {
    return new TagFilledCondition<>(ItemTags.create(tag));
  }

  /** Creates an output for the given ingot */
  private static TagKey<Item> ingot(String name) {
    return ItemTags.create(commonResource("ingots/" + name));
  }

  /** Creates a condition for the given ingot to be present */
  private ICondition ingotCondition(String name) {
    return tagCondition("ingots/" + name);
  }

  /** Creates a condition for the given ingot like input to be present */
  private ICondition ingotLikeCondition(String name) {
    return tagCondition(Metalborn.resource("ingot_like/" + name));
  }


  /** Creates a metal item recycling recipe pair for the given inputs */
  private void recycle(RecipeOutput output, ItemLike item, MetalFilter filter, MetalShape shape, int amount, int cookingTime, String prefix) {
    // special recipe, so no advancements
    CookingMetalRecyclingBuilder.builder(shape, amount)
      .requires(MetalItemIngredient.of(item, filter))
      .experience(0.5f)
      .cookingTime(cookingTime)
      .saveSmelting(output, location(prefix + "_smelting"))
      .saveBlasting(output, location(prefix + "_blasting"));
  }

  /** Creates item recycling recipe pair for the given inputs */
  private void recycle(RecipeOutput output, ItemLike item, TagKey<Item> result, int amount, int cookingTime, String prefix) {
    CookingRecipeBuilder.builder(result, amount)
      .requires(item)
      .experience(0.5f)
      .cookingTime(cookingTime)
      .unlockedBy("has_item", has(item))
      .saveSmelting(output, location(prefix + "_smelting"))
      .saveBlasting(output, location(prefix + "_blasting"));
  }

  /** Builds recipes for recycling identity metalminds */
  private void recycleIdentity(RecipeOutput output, ItemLike item, TagKey<Item> aluminum, int quartz, String name) {
    ICondition condition = new TagFilledCondition<>(aluminum);
    RecipeOutput hasAluminum = output.withConditions(condition);
    RecipeOutput noAluminum = output.withConditions(new NotCondition(condition));
    CookingRecipeBuilder.builder(Items.QUARTZ, quartz)
      .requires(item)
      .experience(0.5f)
      .cookingTime(200 * quartz)
      .saveSmelting(noAluminum, location(name + "_smelting_quartz"))
      .saveBlasting(noAluminum, location(name + "_blasting_quartz"));
    CookingRecipeBuilder.builder(aluminum, 4)
      .requires(item)
      .experience(0.5f)
      .cookingTime(200 * quartz)
      .saveSmelting(hasAluminum, location(name + "_smelting_aluminum"))
      .saveBlasting(hasAluminum, location(name + "_blasting_aluminum"));
  }
}

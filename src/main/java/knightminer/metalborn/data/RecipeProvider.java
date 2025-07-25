package knightminer.metalborn.data;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.data.tag.MetalbornTags;
import knightminer.metalborn.json.ingredient.FillableIngredient;
import knightminer.metalborn.json.ingredient.FillableIngredient.FillState;
import knightminer.metalborn.json.ingredient.IngredientWithMetal.MetalFilter;
import knightminer.metalborn.json.ingredient.MetalItemIngredient;
import knightminer.metalborn.json.ingredient.MetalShapeIngredient;
import knightminer.metalborn.json.recipe.forge.ShapedForgeRecipeBuilder;
import knightminer.metalborn.json.recipe.forge.ShapelessForgeRecipeBuilder;
import knightminer.metalborn.plugin.tinkers.MetalCastingRecipeBuilder;
import knightminer.metalborn.plugin.tinkers.MetalMeltingRecipeBuilder;
import knightminer.metalborn.plugin.tinkers.TinkersMockRecipeBuilder;
import knightminer.metalborn.util.CastItemObject;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.TrueCondition;
import slimeknights.mantle.recipe.condition.TagEmptyCondition;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.mantle.recipe.data.ICommonRecipeHelper;
import slimeknights.mantle.registration.object.IdAwareObject;

import java.util.List;
import java.util.function.Consumer;

import static slimeknights.mantle.Mantle.commonResource;

/** Adds all metalborn crafting recipes */
public class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider implements ICommonRecipeHelper {
  public RecipeProvider(PackOutput packOutput) {
    super(packOutput);
  }

  @Override
  public String getModId() {
    return Metalborn.MOD_ID;
  }

  @Override
  protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.METALLIC_ARTS)
      .requires(Items.BOOK)
      .requires(Registration.TIN.getIngotTag())
      .unlockedBy("has_tin", has(Registration.TIN.getIngotTag()))
      .save(consumer, location("metallic_arts"));

    String metalFolder = "metal/";
    metalCrafting(consumer, Registration.TIN, metalFolder);
    metalCrafting(consumer, Registration.PEWTER, metalFolder);
    metalCrafting(consumer, Registration.STEEL, metalFolder);
    metalCrafting(consumer, Registration.BRONZE, metalFolder);
    metalCrafting(consumer, Registration.ROSE_GOLD, metalFolder);
    metalCrafting(consumer, Registration.NICROSIL, metalFolder);
    packingRecipe(consumer, RecipeCategory.MISC, "ingot", Items.COPPER_INGOT, "nugget", Registration.COPPER_NUGGET, MetalbornTags.Items.COPPER_NUGGETS, metalFolder);
    packingRecipe(consumer, RecipeCategory.MISC, "ingot", Items.NETHERITE_INGOT, "nugget", Registration.NETHERITE_NUGGET, MetalbornTags.Items.NETHERITE_NUGGETS, metalFolder);

    // tin ore
    Ingredient tinOre = Ingredient.of(Registration.RAW_TIN, Registration.TIN_ORE, Registration.DEEPSLATE_TIN_ORE);
    for (RecipeSerializer<? extends AbstractCookingRecipe> serializer : List.of(RecipeSerializer.SMELTING_RECIPE, RecipeSerializer.BLASTING_RECIPE)) {
      int time = serializer == RecipeSerializer.SMELTING_RECIPE ? 200 : 100;
      String name = serializer == RecipeSerializer.SMELTING_RECIPE ? "smelting" : "blasting";
      SimpleCookingRecipeBuilder.generic(tinOre, RecipeCategory.MISC, Registration.TIN.getIngot(), 0.7f, time, serializer)
        .unlockedBy("has_raw", has(Registration.RAW_TIN))
        .unlockedBy("has_ore", has(Registration.TIN_ORE))
        .unlockedBy("has_deepslate", has(Registration.DEEPSLATE_TIN_ORE))
        .save(consumer, location(metalFolder + "tin_ingot_" + name));
    }
    packingRecipe(consumer, RecipeCategory.MISC, "raw_block", Registration.RAW_TIN_BLOCK, "raw", Registration.RAW_TIN, MetalbornTags.Items.RAW_TIN, metalFolder);

    // ferring nuggets
    ShapelessForgeRecipeBuilder.shapeless(Registration.RANDOM_FERRING)
      .requires(Registration.NICROSIL.getNuggetTag())
      .requires(FillableIngredient.filled(MetalItemIngredient.of(Registration.SPIKE, MetalFilter.SPIKE)))
      .cookingRate(2)
      .experience(3f)
      .save(consumer);
    ShapelessForgeRecipeBuilder.shapeless(Registration.CHANGE_FERRING)
      .requires(MetalbornTags.Items.NETHERITE_NUGGETS)
      .requires(FillableIngredient.filled(MetalItemIngredient.of(Registration.SPIKE, MetalFilter.SPIKE)))
      .metal()
      .cookingRate(2)
      .experience(3f)
      .save(consumer);

    // forge
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Registration.FORGE)
      .define('#', Blocks.SMOOTH_SANDSTONE)
      .define('X', Blocks.FURNACE)
      .define('I', Registration.TIN.getIngotTag())
      .pattern("III").pattern("IXI").pattern("###")
      .unlockedBy("has_tin", has(Registration.TIN.getIngotTag()))
      .save(consumer, location("forge"));

    // metal items
    // rings
    ShapedForgeRecipeBuilder.shaped(Registration.RING)
      .pattern("##").pattern("##")
      .define('#', MetalShapeIngredient.nugget(MetalFilter.METALMIND))
      .metal()
      .cookingRate(1)
      .experience(0.5f)
      .save(consumer, location("ring/metal"));
    ShapedForgeRecipeBuilder.shaped(Registration.INVESTITURE_RING)
      .pattern("##").pattern("##")
      .define('#', Registration.NICROSIL.getNuggetTag())
      .cookingRate(1)
      .experience(0.5f)
      .save(consumer, location("ring/nicrosil"));
    // identity uses quartz if no aluminum
    TagKey<Item> aluminumNugget = ItemTags.create(commonResource("nuggets/aluminum"));
    ConditionalRecipe.builder()
      .addCondition(new TagFilledCondition<>(aluminumNugget))
      .addRecipe(ShapedForgeRecipeBuilder.shaped(Registration.IDENTITY_RING)
          .pattern("##").pattern("##")
          .define('#', aluminumNugget)
          .cookingRate(1)
          .experience(0.5f)::save)
      .addCondition(TrueCondition.INSTANCE)
      .addRecipe(ShapelessForgeRecipeBuilder.shapeless(Registration.IDENTITY_RING, 2)
        .requires(Tags.Items.GEMS_QUARTZ) // 1 quartz by itself for 2 rings is close enough to 4 nuggets, 1/8 instead of 1/9
        .cookingRate(2)
        .experience(1.0f)::save)
        .build(consumer, location("ring/identity"));
    // if no duralumin is registered, nugget version of alloying makes a ring
    ICondition aluminumNuggetCondition = new TagFilledCondition<>(aluminumNugget);
    ShapelessForgeRecipeBuilder.shapeless(Registration.RING.get().withMetal(MetalIds.duralumin))
      .requires(aluminumNugget, 3)
      .requires(MetalbornTags.Items.COPPER_NUGGETS, 1)
      .cookingRate(1)
      .experience(0.5f)
      .save(withCondition(consumer, aluminumNuggetCondition, new TagEmptyCondition<>(ItemTags.create(commonResource("nuggets/duralumin")))), location("ring/duralumin"));
    // bracers
    ShapedForgeRecipeBuilder.shaped(Registration.BRACER)
      .pattern("##").pattern("##")
      .define('#', MetalShapeIngredient.ingot(MetalFilter.METALMIND))
      .metal()
      .cookingRate(4)
      .experience(2f)
      .save(consumer, location("bracer/metal"));
    ShapedForgeRecipeBuilder.shaped(Registration.INVESTITURE_BRACER)
      .pattern("##").pattern("##")
      .define('#', Registration.NICROSIL.getIngotTag())
      .cookingRate(4)
      .experience(2f)
      .save(consumer, location("bracer/nicrosil"));
    // identity uses quartz if no aluminum
    ConditionalRecipe.builder()
      .addCondition(ingotCondition("aluminum"))
      .addRecipe(ShapedForgeRecipeBuilder.shaped(Registration.IDENTITY_BRACER)
        .pattern("##").pattern("##")
        .define('#', ingot("aluminum"))
        .cookingRate(4)
        .experience(2f)::save)
      .addCondition(TrueCondition.INSTANCE)
      .addRecipe(ShapedForgeRecipeBuilder.shaped(Registration.IDENTITY_BRACER)
        .pattern("##").pattern("##")
        .define('#', Tags.Items.GEMS_QUARTZ)
        .cookingRate(4)
        .experience(2f)::save)
      .build(consumer, location("bracer/identity"));
    // if no duralumin is registered, its alloying recipe makes bracers
    ICondition noDuralumin = new TagEmptyCondition<>(ingot("duralumin"));
    ShapelessForgeRecipeBuilder.shapeless(Registration.BRACER.get().withMetal(MetalIds.duralumin))
      .requires(ingot("aluminum"), 3)
      .requires(Tags.Items.INGOTS_COPPER, 1)
      .cookingRate(4)
      .experience(2f)
      .save(withCondition(consumer, ingotCondition("aluminum"), noDuralumin), location("bracer/duralumin"));
    // spikes
    ShapedForgeRecipeBuilder.shaped(Registration.SPIKE)
      .pattern(" #").pattern("# ")
      .define('#', MetalShapeIngredient.ingot(MetalFilter.SPIKE))
      .metal()
      .cookingRate(4) // you get 4 spikes per fuel usage, but you really want hoppers since they are unstackable
      .experience(1f)
      .save(consumer, location("spike/metal"));
    ShapedForgeRecipeBuilder.shaped(Registration.INVESTITURE_SPIKE)
      .pattern(" #").pattern("# ")
      .define('#', Registration.NICROSIL.getIngotTag())
      .cookingRate(4)
      .experience(1f)
      .save(consumer, location("spike/nicrosil"));
    // if no duralumin is registered, create its spike with a close enough 1:1 recipe
    ShapedForgeRecipeBuilder.shaped(Registration.SPIKE.get().withMetal(MetalIds.duralumin))
      .pattern(" a").pattern("c ")
      .define('c', Tags.Items.INGOTS_COPPER)
      .define('a', ingot("aluminum"))
      .cookingRate(4)
      .experience(1f)
      .save(withCondition(consumer, ingotCondition("aluminum"), noDuralumin), location("spike/duralumin"));

    // unsealed is crafted from a full nicrosil metalmind and a matching empty metalmind
    ShapelessForgeRecipeBuilder.shapeless(Registration.UNSEALED_RING)
      .requires(MetalbornTags.Items.NETHERITE_NUGGETS)
      .requires(FillableIngredient.of(FillState.EMPTY, MetalItemIngredient.of(Registration.RING, MetalFilter.METALMIND)))
      .requires(FillableIngredient.of(FillState.UNSEALED, MetalItemIngredient.of(Registration.INVESTITURE_RING, MetalFilter.METALMIND)))
      .metal()
      .cookingRate(4)
      .experience(0.5f)
      .save(consumer, location("ring/unsealed"));

    // alloys
    String alloyFolder = "alloy/";
    // base mod
    // use lead instead of iron if its present, as thats closer to the real recipe for pewter
    ConditionalRecipe.builder()
      .addCondition(ingotLikeCondition("lead"))
      .addRecipe(ShapelessForgeRecipeBuilder.shapeless(Registration.PEWTER.getIngotTag(), 3)
        .requires(ingotLike("tin"), 2)
        .requires(ingotLike("lead"), 1)::save)
      .addCondition(TrueCondition.INSTANCE)
      .addRecipe(ShapelessForgeRecipeBuilder.shapeless(Registration.PEWTER.getIngotTag(), 4)
        .requires(ingotLike("tin"), 3)
        .requires(ingotLike("iron"), 1)::save)
      .build(consumer, location(alloyFolder + "pewter"));
    ShapelessForgeRecipeBuilder.shapeless(Registration.STEEL.getIngotTag(), 1)
      .requires(ingotLike("iron"), 1)
      .requires(Items.BLAZE_POWDER, 1)
      .requires(Blocks.SAND, 1)
      .cookingRate(4)
      .save(consumer, location(alloyFolder + "steel"));
    ShapelessForgeRecipeBuilder.shapeless(Registration.BRONZE.getIngotTag(), 4)
      .requires(ingotLike("copper"), 3)
      .requires(ingotLike("tin"), 1)
      .experience(0.7f)
      .save(consumer, location(alloyFolder + "bronze"));
    ShapelessForgeRecipeBuilder.shapeless(Registration.ROSE_GOLD.getIngotTag(), 2)
      .requires(ingotLike("gold"), 1)
      .requires(ingotLike("copper"), 1)
      .save(consumer, location(alloyFolder + "rose_gold"));
    // if available, use nickel in the recipe, though craft it without nickel if missing
    ConditionalRecipe.builder()
      .addCondition(ingotLikeCondition("nickel"))
      .addRecipe(ShapelessForgeRecipeBuilder.shapeless(Registration.NICROSIL.getIngotTag(), 4)
        .requires(ingotLike("nickel"), 2)
        .requires(ingotLike("tin"), 1)
        .requires(ingotLike("quartz"), 1)::save)
      .addCondition(TrueCondition.INSTANCE)
      .addRecipe(ShapelessForgeRecipeBuilder.shapeless(Registration.NICROSIL.getIngotTag(), 2)
        .requires(ingotLike("tin"), 1)
        .requires(ingotLike("quartz"), 1)::save)
      .build(consumer, location(alloyFolder + "nicrosil"));
    // netherite is normally 4 scrap + 4 gold = 1 ingot
    // this recipe makes it effectively 3 scrap + 3 gold = 1 ingot, a 1 scrap discount!
    ShapelessForgeRecipeBuilder.shapeless(MetalbornTags.Items.NETHERITE_NUGGETS, 3)
      .requires(ingotLike("gold"), 1)
      .requires(ingotLike("netherite_scrap"), 1)
      .cookingRate(2)
      .save(consumer, location(alloyFolder + "netherite"));

    // general compat
    ShapelessForgeRecipeBuilder.shapeless(ingot("brass"), 2)
      .requires(ingotLike("copper"), 1)
      .requires(ingotLike("zinc"), 1)
      .experience(1.0f)
      .save(withCondition(consumer, ingotCondition("brass"), ingotLikeCondition("zinc")), location(alloyFolder + "brass"));
    ShapelessForgeRecipeBuilder.shapeless(ingot("constantan"), 2)
      .requires(ingotLike("copper"), 1)
      .requires(ingotLike("nickel"), 1)
      .experience(0.7f)
      .save(withCondition(consumer, ingotCondition("constantan"), ingotLikeCondition("nickel")), location(alloyFolder + "constantan"));
    ShapelessForgeRecipeBuilder.shapeless(ingot("electrum"), 2)
      .requires(ingotLike("gold"), 1)
      .requires(ingotLike("silver"), 1)
      .save(withCondition(consumer, ingotCondition("electrum"), ingotLikeCondition("silver")), location(alloyFolder + "electrum"));
    ShapelessForgeRecipeBuilder.shapeless(ingot("invar"), 3)
      .requires(ingotLike("iron"), 2)
      .requires(ingotLike("nickel"), 1)
      .save(withCondition(consumer, ingotCondition("invar"), ingotLikeCondition("nickel")), location(alloyFolder + "invar"));
    ShapelessForgeRecipeBuilder.shapeless(ingot("duralumin"), 3)
      .requires(ingotLike("aluminum"), 3)
      .requires(ingotLike("copper"), 1)
      .save(withCondition(consumer, ingotCondition("duralumin"), ingotLikeCondition("aluminum")), location(alloyFolder + "duralumin"));

    // tinkers compat
    ShapelessForgeRecipeBuilder.shapeless(ingot("amethyst_bronze"), 1)
      .requires(ingotLike("copper"), 1)
      .requires(Items.AMETHYST_SHARD, 1)
      .experience(0.7f)
      .save(withCondition(consumer, ingotCondition("amethyst_bronze")), location(alloyFolder + "amethyst_bronze"));
    ShapelessForgeRecipeBuilder.shapeless(ingot("hepatizon"), 2)
      .requires(ingotLike("copper"), 2)
      .requires(ingotLike("cobalt"), 1)
      .requires(MetalbornTags.Items.QUARTZ_LIKE, 1)
      .experience(2f)
      .save(withCondition(consumer, ingotCondition("hepatizon"), ingotLikeCondition("cobalt")), location(alloyFolder + "hepatizon"));
    ShapelessForgeRecipeBuilder.shapeless(ingot("manyullyn"), 4)
      .requires(ingotLike("cobalt"), 3)
      .requires(MetalbornTags.Items.SCRAP_LIKE, 1)
      .experience(2f)
      .save(withCondition(consumer, ingotCondition("manyullyn"), ingotLikeCondition("cobalt")), location(alloyFolder + "manyullyn"));
    // TODO: consider slime metals


    // Tinkers' Construct melting and casting
    String tinkersFolder = "tinkers/";

    // casting
    int ingot = 90;
    int nugget = 10;
    // standard metal items
    metalMeltingCasting(consumer, Registration.RING,   List.of(Registration.INVESTITURE_RING,   Registration.IDENTITY_RING),   Registration.RING_CAST,   MetalFilter.METALMIND, nugget * 4, tinkersFolder);
    metalMeltingCasting(consumer, Registration.BRACER, List.of(Registration.INVESTITURE_BRACER, Registration.IDENTITY_BRACER), Registration.BRACER_CAST, MetalFilter.METALMIND, ingot * 4,  tinkersFolder);
    metalMeltingCasting(consumer, Registration.SPIKE,  List.of(Registration.INVESTITURE_SPIKE),                                Registration.SPIKE_CAST,  MetalFilter.SPIKE,     ingot * 2,  tinkersFolder);
    // special metalminds
    TagKey<Fluid> nicrosil = FluidTags.create(commonResource("molten_nicrosil"));
    int nicrosilTemperature = 1100;
    TinkersMockRecipeBuilder.meltingCasting(consumer, Registration.INVESTITURE_RING,   Registration.RING_CAST,   nicrosil, nugget * 4, nicrosilTemperature, tinkersFolder + "ring/investiture/");
    TinkersMockRecipeBuilder.meltingCasting(consumer, Registration.INVESTITURE_BRACER, Registration.BRACER_CAST, nicrosil, ingot * 4,  nicrosilTemperature, tinkersFolder + "bracer/investiture/");
    TinkersMockRecipeBuilder.meltingCasting(consumer, Registration.INVESTITURE_SPIKE,  Registration.SPIKE_CAST,  nicrosil, ingot * 2,  nicrosilTemperature, tinkersFolder + "spike/investiture/");
    // identity may be quartz and may be aluminum based on loaded content
    TinkersMockRecipeBuilder.identityMeltingCasting(consumer, Registration.IDENTITY_RING,   Registration.RING_CAST,   nugget * 4, 400, tinkersFolder + "ring/identity/");
    TinkersMockRecipeBuilder.identityMeltingCasting(consumer, Registration.IDENTITY_BRACER, Registration.BRACER_CAST, ingot * 4,   50, tinkersFolder + "bracer/identity/");

    // nuggets to change ferring type
    TagKey<Fluid> netherite = FluidTags.create(commonResource("molten_netherite"));
    int netheriteTemperature = 1250;
    TinkersMockRecipeBuilder.casting(consumer, Registration.RANDOM_FERRING, false, FillableIngredient.filled(MetalItemIngredient.of(Registration.SPIKE, MetalFilter.SPIKE)), nicrosil,  nugget, nicrosilTemperature,  tinkersFolder + "random_ferring_casting");
    TinkersMockRecipeBuilder.casting(consumer, Registration.CHANGE_FERRING, true,  FillableIngredient.filled(MetalItemIngredient.of(Registration.SPIKE, MetalFilter.SPIKE)), netherite, nugget, netheriteTemperature, tinkersFolder + "change_ferring_casting");
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

  /** Adds a recipe for casting a metal object, like a metalmind */
  private <T extends ItemLike & IdAwareObject> void metalMeltingCasting(Consumer<FinishedRecipe> consumer, T result, List<ItemLike> otherItems, CastItemObject cast, MetalFilter filter, int amount, String folder) {
    // casting
    MetalCastingRecipeBuilder.table(result)
      .setCast(Ingredient.of(cast.getMultiUseTag()), false)
      .setFilter(filter)
      .setAmount(amount)
      .save(consumer, wrap(result, folder, "/casting_gold_cast"));
    MetalCastingRecipeBuilder.table(result)
      .setCast(Ingredient.of(cast.getSingleUseTag()), true)
      .setFilter(filter)
      .setAmount(amount)
      .save(consumer, wrap(result, folder, "/casting_sand_cast"));

    // melting
    MetalMeltingRecipeBuilder.melting(MetalItemIngredient.of(result, filter), amount).save(consumer, wrap(result, folder, "/melting"));

    // cast creation
    TinkersMockRecipeBuilder.castRecipes(consumer, result, otherItems, cast, filter, Math.max(1, amount / 90), folder);
  }
}

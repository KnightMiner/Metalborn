package knightminer.metalborn.data;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.data.tag.MetalbornTags;
import knightminer.metalborn.recipe.ShapelessForgeRecipeBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.TrueCondition;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.mantle.recipe.data.ICommonRecipeHelper;

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
    String metalFolder = "metal/";
    metalCrafting(consumer, Registration.TIN, metalFolder);
    metalCrafting(consumer, Registration.PEWTER, metalFolder);
    metalCrafting(consumer, Registration.STEEL, metalFolder);
    metalCrafting(consumer, Registration.BRONZE, metalFolder);
    metalCrafting(consumer, Registration.ROSE_GOLD, metalFolder);
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

    // alloys
    String alloyFolder = "alloy/";
    // base mod
    // use lead instead of iron if its present, as thats closer to the real recipe for pewter
    ConditionalRecipe.builder()
      .addCondition(ingotLikeCondition("lead"))
      .addRecipe(ShapelessForgeRecipeBuilder.shapeless(Registration.PEWTER.getIngotTag(), 4)
        .requires(ingotLike("tin"), 3)
        .requires(ingotLike("lead"), 1)::save)
      .addCondition(TrueCondition.INSTANCE)
      .addRecipe(ShapelessForgeRecipeBuilder.shapeless(Registration.PEWTER.getIngotTag(), 4)
        .requires(ingotLike("tin"), 3)
        .requires(ingotLike("iron"), 1)::save)
      .build(consumer, location(alloyFolder + "pewter"));
    ShapelessForgeRecipeBuilder.shapeless(Registration.STEEL.getIngotTag(), 1)
      .requires(Items.CHARCOAL, 3)
      .requires(ingotLike("iron"), 1)
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

    // tinkers compat
    ShapelessForgeRecipeBuilder.shapeless(ingot("amethyst_bronze"), 1)
      .requires(ingotLike("copper"), 1)
      .requires(Items.AMETHYST_SHARD, 1)
      .experience(0.7f)
      .save(withCondition(consumer, ingotCondition("amethyst_bronze")), location(alloyFolder + "amethyst_bronze"));
    ShapelessForgeRecipeBuilder.shapeless(ingot("hepatizon"), 2)
      .requires(ingotLike("copper"), 2)
      .requires(ingotLike("cobalt"), 1)
      .requires(Items.QUARTZ, 1)
      .experience(2f)
      .save(withCondition(consumer, ingotCondition("hepatizon"), ingotLikeCondition("cobalt")), location(alloyFolder + "hepatizon"));
    ShapelessForgeRecipeBuilder.shapeless(ingot("manyullyn"), 4)
      .requires(ingotLike("cobalt"), 3)
      .requires(ingotLike("netherite_scrap"), 1)
      .experience(2f)
      .save(withCondition(consumer, ingotCondition("manyullyn"), ingotLikeCondition("cobalt")), location(alloyFolder + "manyullyn"));
    // TODO: consider slime metals
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
}

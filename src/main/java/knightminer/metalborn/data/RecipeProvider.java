package knightminer.metalborn.data;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.data.tag.MetalbornTags;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.recipe.data.ICommonRecipeHelper;

import java.util.List;
import java.util.function.Consumer;

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
  }
}

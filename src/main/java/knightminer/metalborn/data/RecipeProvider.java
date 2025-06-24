package knightminer.metalborn.data;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.data.tag.MetalbornTags;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Items;
import slimeknights.mantle.recipe.data.ICommonRecipeHelper;

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
  }
}

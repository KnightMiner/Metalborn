package knightminer.metalborn.recipe;

import knightminer.metalborn.core.Registration;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

/** Base interface for forge recipes */
public interface ForgeRecipe extends Recipe<CraftingContainer> {
  int DEFAULT_COOKING_TIME = 100;
  int[] NO_LINKS = new int[0];

  @Override
  default RecipeType<?> getType() {
    return Registration.FORGE_RECIPE.get();
  }

  /** Gets the experience granted on taking this result */
  float getExperience();

  /** Gets the cooking duration for this recipe */
  int getCookingTime();

  /** Gets an animated list of the result item */
  List<ItemStack> getResult();

  /** Gets a list of slots linked to the input slot */
  default int[] getLinkedInputs() {
    return NO_LINKS;
  }

  // silences log errors about missing recipe book
  @Override
  default boolean isSpecial() {
    // TODO: look into custom recipe book support
    return true;
  }
}

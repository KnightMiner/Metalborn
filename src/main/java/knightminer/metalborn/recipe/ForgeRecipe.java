package knightminer.metalborn.recipe;

import knightminer.metalborn.core.Registration;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

/** Base interface for forge recipes */
public interface ForgeRecipe extends Recipe<CraftingContainer> {
  @Override
  default RecipeType<?> getType() {
    return Registration.FORGE_RECIPE.get();
  }

  /** Gets the experience granted on taking this result */
  float getExperience();

  /** Gets the cooking duration for this recipe */
  int getCookingTime();
}

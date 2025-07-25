package knightminer.metalborn.json.recipe.forge;

import net.minecraft.data.recipes.FinishedRecipe;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.function.Consumer;

import static knightminer.metalborn.json.recipe.forge.ForgeRecipe.DEFAULT_COOKING_TIME;

/** Shared logic for Forge recipe builders */
public abstract class AbstractForgeRecipeBuilder<T extends AbstractForgeRecipeBuilder<T>> extends AbstractRecipeBuilder<T> {
  protected final ItemOutput result;
  protected float experience = 1.0f;
  private int cookingTime = 0;

  protected AbstractForgeRecipeBuilder(ItemOutput result) {
    this.result = result;
  }

  /** Casts this builder to the T builder type */
  @SuppressWarnings("unchecked")
  private T self() {
    return (T) this;
  }

  /** Sets the experience for this recipe */
  public T experience(float experience) {
    this.experience = experience;
    return self();
  }

  /** Sets the cooking time for this recipe */
  public T cookingTime(int cookingTime) {
    this.cookingTime = cookingTime;
    return self();
  }

  /** Sets the cooking time for this recipe to the default times the given rate*/
  public T cookingRate(float rate) {
    return cookingTime((int) (DEFAULT_COOKING_TIME * rate));
  }

  /** Computes the default cooking time */
  protected int computeCookingTime() {
    if (cookingTime == 0) {
      return DEFAULT_COOKING_TIME * result.getCount();
    }
    return cookingTime;
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, Loadables.ITEM.getKey(result.get().getItem()));
  }
}

package knightminer.metalborn.recipe;

import net.minecraft.data.recipes.FinishedRecipe;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.function.Consumer;

import static knightminer.metalborn.recipe.AbstractForgeRecipe.DEFAULT_COOKING_TIME;

/** Shared logic for Forge recipe builders */
public abstract class AbstractForgeRecipeBuilder<T extends AbstractForgeRecipeBuilder<T>> extends AbstractRecipeBuilder<T> {
  protected final ItemOutput result;
  protected float experience = 0f;
  protected int cookingTime = DEFAULT_COOKING_TIME;

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

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, Loadables.ITEM.getKey(result.get().getItem()));
  }
}

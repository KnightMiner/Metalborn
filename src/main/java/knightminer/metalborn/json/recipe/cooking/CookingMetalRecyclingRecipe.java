package knightminer.metalborn.json.recipe.cooking;

import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.json.recipe.MetalResult;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.loadable.field.LoadableField;

import java.util.function.Predicate;

/** Common logic between {@link SmeltingMetalRecyclingRecipe} and {@link BlastingMetalRecyclingRecipe} */
public interface CookingMetalRecyclingRecipe {
  LoadableField<MetalResult,CookingMetalRecyclingRecipe> RESULT_FIELD = MetalResult.LOADABLE.requiredField("result", CookingMetalRecyclingRecipe::getResult);

  /** Gets the recipe filter */
  Predicate<MetalPower> getFilter();

  /** Gets the recipe result */
  MetalResult getResult();

  /** Checks if the result is present */
  default boolean isPresent(Container inv) {
    MetalPower metal = MetalManager.INSTANCE.get(MetalItem.getMetal(inv.getItem(0)));
    // must have a metal, that matches our filter, and has a value for the requested tag
    return metal != MetalPower.DEFAULT && getFilter().test(metal) && getResult().isPresent(metal);
  }

  /** Gets the result for this recipe */
  default ItemStack assembleMetal(Container inv) {
    MetalPower metal = MetalManager.INSTANCE.get(MetalItem.getMetal(inv.getItem(0)));
    if (metal != MetalPower.DEFAULT) {
      return getResult().get(metal);
    }
    return ItemStack.EMPTY;
  }
}

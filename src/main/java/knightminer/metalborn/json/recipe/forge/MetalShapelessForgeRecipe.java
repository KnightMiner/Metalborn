package knightminer.metalborn.json.recipe.forge;

import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.json.recipe.forge.MetalShapedForgeRecipe.JEIInfo;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.List;
import java.util.function.Predicate;

/** Forge recipe which sets a metal to the result */
public class MetalShapelessForgeRecipe extends ShapelessForgeRecipe {
  /** Loadable instance to create the serializer */
  public static final RecordLoadable<MetalShapelessForgeRecipe> LOADABLE = makeLoader(MetalShapelessForgeRecipe::new);

  private Predicate<MetalPower> metalFilter;
  protected MetalShapelessForgeRecipe(NonNullList<Ingredient> ingredients, ItemOutput result, float experience, int cookingTime) {
    super(ingredients, result, experience, cookingTime);
  }

  /** Finds the first metal ingredient in the input list */
  private Predicate<MetalPower> getMetalFilter() {
    if (metalFilter == null) {
      metalFilter = MetalShapedForgeRecipe.getMetalFilter(getIngredients());
    }
    return metalFilter;
  }

  @Override
  public boolean matches(CraftingInput input, Level level) {
    return super.matches(input, level) && MetalShapedForgeRecipe.findMetal(input, getMetalFilter()) != MetalPower.DEFAULT;
  }

  @Override
  public ItemStack assemble(CraftingInput input, HolderLookup.Provider lookup) {
    return MetalItem.setMetal(super.assemble(input, lookup), MetalShapedForgeRecipe.findMetal(input, getMetalFilter()).id());
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return Registration.METAL_SHAPELESS_FORGE.get();
  }


  /* JEI */
  private int[] linkedInputs;
  private List<ItemStack> displayResults;

  /** Setup the JEI display */
  private void setupJEI() {
    if (displayResults == null || linkedInputs == null) {
      JEIInfo info = MetalShapedForgeRecipe.getJEIInfo(getIngredients(), result.get());
      displayResults = info.displayResults();
      linkedInputs = info.linkedInputs();
    }
  }

  @Override
  public List<ItemStack> getResult() {
    setupJEI();
    return displayResults;
  }

  @Override
  public int[] getLinkedInputs() {
    setupJEI();
    return linkedInputs;
  }
}

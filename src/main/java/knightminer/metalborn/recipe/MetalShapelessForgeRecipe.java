package knightminer.metalborn.recipe;

import knightminer.metalborn.core.Registration;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalPower;
import knightminer.metalborn.recipe.MetalShapedForgeRecipe.JEIInfo;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
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
  protected MetalShapelessForgeRecipe(ResourceLocation id, ItemOutput result, NonNullList<Ingredient> ingredients, float experience, int cookingTime) {
    super(id, result, ingredients, experience, cookingTime);
  }

  /** Finds the first metal ingredient in the input list */
  private Predicate<MetalPower> getMetalFilter() {
    if (metalFilter == null) {
      metalFilter = MetalShapedForgeRecipe.getMetalFilter(getIngredients());
    }
    return metalFilter;
  }

  @Override
  public boolean matches(CraftingContainer inv, Level level) {
    return super.matches(inv, level) && MetalShapedForgeRecipe.findMetal(inv, getMetalFilter()) != MetalId.NONE;
  }

  @Override
  public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
    return MetalShapedForgeRecipe.setMetal(super.assemble(inv, registryAccess), MetalShapedForgeRecipe.findMetal(inv, getMetalFilter()));
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

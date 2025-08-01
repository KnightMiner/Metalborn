package knightminer.metalborn.json.recipe.forge;

import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.json.ingredient.FillableIngredient;
import knightminer.metalborn.json.ingredient.IngredientWithMetal;
import knightminer.metalborn.json.ingredient.IngredientWithMetal.MetalFilter;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Forge recipe which sets a metal to the result */
public class MetalShapedForgeRecipe extends ShapedForgeRecipe {
  private Predicate<MetalPower> metalFilter;
  public MetalShapedForgeRecipe(ResourceLocation id, int width, int height, NonNullList<Ingredient> grid, ItemOutput result, float experience, int cookingTime) {
    super(id, width, height, grid, result, experience, cookingTime);
  }

  /** Fetches the ingredient outside the fillable ingredient */
  public static Ingredient unwrap(Ingredient ingredient) {
    if (ingredient instanceof FillableIngredient fillable) {
      return fillable.getInner();
    }
    return ingredient;
  }

  /** Finds the first metal ingredient in the input list */
  static Predicate<MetalPower> getMetalFilter(List<Ingredient> ingredients) {
    Set<MetalFilter> filters = ingredients.stream().flatMap(ingredient -> {
      if (unwrap(ingredient) instanceof IngredientWithMetal metal) {
        return Stream.of(metal.getFilter());
      }
      return Stream.empty();
    }).collect(Collectors.toSet());

    // combine filter list into a single filter
    if (filters.isEmpty()) {
      return MetalFilter.ANY;
    } else if (filters.size() == 1) {
      return filters.iterator().next();
    } else {
      return power -> {
        for (MetalFilter filter : filters) {
          if (filter.test(power)) {
            return true;
          }
        }
        return false;
      };
    }
  }

  /** Finds the first metal ingredient in the input list */
  private Predicate<MetalPower> getMetalFilter() {
    if (metalFilter == null) {
      metalFilter = getMetalFilter(getIngredients());
    }
    return metalFilter;
  }

  /** Gets the metal power from the given stack */
  private static MetalPower getMetal(ItemStack stack) {
    // map metal items to the metal in NBT
    if (stack.getItem() instanceof MetalItem) {
      return MetalManager.INSTANCE.get(MetalItem.getMetal(stack));
    } else {
      // other items try to do a tag match on ingot or nugget
      return MetalManager.INSTANCE.fromIngotOrNugget(stack.getItem());
    }
  }

  /** Finds the metal in the given grid */
  static MetalPower findMetal(CraftingContainer inventory, Predicate<MetalPower> metal) {
    // ensure same metal is in all slots
    MetalPower first = null;
    for (int i = 0; i < inventory.getContainerSize(); i++) {
      ItemStack stack = inventory.getItem(i);
      if (!stack.isEmpty()) {
        // if we found a metal, work with it
        MetalPower power = getMetal(stack);
        if (power != MetalPower.DEFAULT && metal.test(power)) {
          // first match is set
          if (first == null) {
            first = power;
          } else if (first != power) {
            return MetalPower.DEFAULT;
          }
        }
      }
    }
    return Objects.requireNonNullElse(first, MetalPower.DEFAULT);
  }

  @Override
  public boolean matches(CraftingContainer inv, Level level) {
    return super.matches(inv, level) && findMetal(inv, getMetalFilter()) != MetalPower.DEFAULT;
  }

  @Override
  public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
    return MetalItem.setMetal(super.assemble(inv, registryAccess), findMetal(inv, getMetalFilter()).id());
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return Registration.METAL_SHAPED_FORGE.get();
  }


  /* JEI */
  private int[] linkedInputs;
  private List<ItemStack> displayResults;

  record JEIInfo(int[] linkedInputs, List<ItemStack> displayResults) {}

  /** Gets the JEI info for the given inputs */
  static JEIInfo getJEIInfo(List<Ingredient> ingredients, ItemStack result) {
    List<Integer> indices = new ArrayList<>();
    ItemStack[] inputExample = null;
    // search for metal ingredients
    for (int i = 0; i < ingredients.size(); i++) {
      Ingredient ingredient = ingredients.get(i);
      if (unwrap(ingredient) instanceof IngredientWithMetal) {
        // if we have not yet found an ingredient, use this one for our input stacks
        if (inputExample == null) {
          indices.add(i);
          inputExample = ingredient.getItems();
          // for the focus link to work, we need all ingredients to have the same size
          // so skip any that don't match the first size
        } else if (ingredient.getItems().length == inputExample.length) {
          indices.add(i);
        }
      }
    }
    // mark linked slots
    int[] linkedInputs = indices.isEmpty() ? NO_LINKS : indices.stream().mapToInt(i -> i).toArray();

    // build result
    // if we found an ingredient, match that in the result. Otherwise, just pull all values from the registry
    List<ItemStack> displayResults;
    if (inputExample == null) {
      displayResults = MetalManager.INSTANCE.getSortedPowers().stream()
        .map(power -> MetalItem.setMetal(result.copy(), power.id())).toList();
    } else {
      displayResults = Arrays.stream(inputExample)
        .map(stack -> MetalItem.setMetal(result.copy(), getMetal(stack).id()))
        .toList();
    }
    return new JEIInfo(linkedInputs, displayResults);
  }

  /** Setup the JEI display */
  private void setupJEI() {
    if (displayResults == null || linkedInputs == null) {
      JEIInfo info = getJEIInfo(getIngredients(), result.get());
      displayResults = info.displayResults;
      linkedInputs = info.linkedInputs;
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

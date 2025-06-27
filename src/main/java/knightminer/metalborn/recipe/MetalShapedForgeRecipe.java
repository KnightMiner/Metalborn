package knightminer.metalborn.recipe;

import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import knightminer.metalborn.recipe.MetalIngredient.MetalFilter;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import slimeknights.mantle.recipe.helper.ItemOutput;

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

  /** Finds the first metal ingredient in the input list */
  static Predicate<MetalPower> getMetalFilter(List<Ingredient> ingredients) {
    Set<MetalFilter> filters = ingredients.stream().flatMap(ingredient -> {
      if (ingredient instanceof MetalIngredient metal) {
        return Stream.of(metal.filter);
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

  /** Finds the metal in the given grid */
  static MetalId findMetal(CraftingContainer inventory, Predicate<MetalPower> metal) {
    // ensure same metal is in all slots
    MetalId firstId = null;
    for (int i = 0; i < inventory.getContainerSize(); i++) {
      ItemStack stack = inventory.getItem(i);
      if (!stack.isEmpty()) {
        MetalPower power = MetalManager.INSTANCE.fromIngotOrNugget(stack.getItem());
        if (power != MetalPower.DEFAULT && metal.test(power)) {
          // first match is set
          if (firstId == null) {
            firstId = power.id();
          } else if (!firstId.equals(power.id())) {
            return MetalId.NONE;
          }
        }
      }
    }
    return Objects.requireNonNullElse(firstId, MetalId.NONE);
  }

  @Override
  public boolean matches(CraftingContainer inv, Level level) {
    return super.matches(inv, level) && findMetal(inv, getMetalFilter()) != MetalId.NONE;
  }

  @Override
  public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
    ItemStack stack = super.assemble(inv, registryAccess);
    MetalId metal = findMetal(inv, getMetalFilter());
    if (metal != MetalId.NONE) {
      stack.getOrCreateTag().putString(MetalItem.TAG_METAL, metal.toString());
    }
    return stack;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return Registration.METAL_SHAPED_FORGE.get();
  }
}

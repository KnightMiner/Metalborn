package knightminer.metalborn.plugin.tinkers;

import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.json.ingredient.IngredientWithMetal.MetalFilter;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.IMultiRecipe;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.library.recipe.casting.AbstractCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.DisplayCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.ICastingContainer;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Casting recipe for casting metal items, such as metalminds or spikes.
 * @see slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingRecipe
 */
public class MetalCastingRecipe extends AbstractCastingRecipe implements IMultiRecipe<IDisplayableCastingRecipe> {
  public static final RecordLoadable<MetalCastingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPED_SERIALIZER.requiredField(),
    ContextKey.ID.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP,
    CAST_FIELD,
    MetalFilter.LOADABLE.defaultField("filter", MetalFilter.ANY, r -> r.filter),
    IntLoadable.FROM_ONE.requiredField("amount", r -> r.amount),
    ItemOutput.Loadable.REQUIRED_ITEM.requiredField("result", r -> r.result),
    CAST_CONSUMED_FIELD, SWITCH_SLOTS_FIELD,
    MetalCastingRecipe::new);

  /** Recipe serialzier to distinguish basins from tables */
  private final TypeAwareRecipeSerializer<?> serializer;
  /** Filter on metal powers allowed */
  private final MetalFilter filter;
  /** Amount of fluid used in millibuckets, always the same as we only work with metals on the same standard */
  private final int amount;
  /** Result item from this recipe */
  private final ItemOutput result;

  protected MetalCastingRecipe(TypeAwareRecipeSerializer<?> serializer, ResourceLocation id, String group, Ingredient cast, MetalFilter filter, int amount, ItemOutput result, boolean consumed, boolean switchSlots) {
    super(serializer.getType(), id, group, cast, consumed, switchSlots);
    this.serializer = serializer;
    this.filter = filter;
    this.amount = amount;
    this.result = result;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return serializer;
  }

  @Override
  public boolean matches(ICastingContainer inv, Level pLevel) {
    ItemStack cast = inv.getStack();
    // cast must match
    if (!this.getCast().test(cast)) {
      return false;
    }
    // fluid must match the filter
    MetalPower power = MetalManager.INSTANCE.fromFluid(inv.getFluid());
    if (power == MetalPower.DEFAULT || !filter.test(power)) {
      return false;
    }
    // if cast is a metal item, then metal must match existing metal
    if (cast.getItem() instanceof MetalItem) {
      MetalId metal = MetalItem.getMetal(cast);
      return metal.equals(power.id());
    }
    return true;
  }

  @Override
  public int getFluidAmount(ICastingContainer inv) {
    return amount;
  }

  @Override
  public int getCoolingTime(ICastingContainer inv) {
    MetalPower power = MetalManager.INSTANCE.fromFluid(inv.getFluid());
    return power == MetalPower.DEFAULT ? 1 : ICastingRecipe.calcCoolingTime(power.temperature(), amount);
  }

  @Override
  public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
    return result.get();
  }

  @Override
  public ItemStack assemble(ICastingContainer inv, RegistryAccess access) {
    return MetalItem.setMetal(result.copy(), MetalManager.INSTANCE.fromFluid(inv.getFluid()).id());
  }


  /* JEI */
  protected List<IDisplayableCastingRecipe> multiRecipes = null;

  /** Adds the metal to the list of stacks */
  private static List<ItemStack> withMetal(List<ItemStack> stacks, MetalId metal) {
    // first, find a stack that is a metal item; if none we don't need a new list
    for (ItemStack stack : stacks) {
      if (stack.getItem() instanceof MetalItem) {
        // we now know we have a stack, so copy each over
        return stacks.stream().map(s -> s.getItem() instanceof MetalItem ? MetalItem.setMetal(s.copy(), metal) : s).toList();
      }
    }
    return stacks;
  }

  @Override
  public List<IDisplayableCastingRecipe> getRecipes(RegistryAccess access) {
    if (multiRecipes == null) {
      // if its a metal ingredient, display each stack as a separate recipe
      Ingredient cast = getCast();
      List<ItemStack> castItems = Arrays.asList(cast.getItems());
      multiRecipes = MetalManager.INSTANCE.getSortedPowers().stream()
        .filter(power -> power.temperature() > 0 && filter.test(power))
        .map(power -> {
          List<FluidStack> fluids = ((FluidIngredient)FluidIngredient.of(power.fluid(), amount)).getFluids();
          return new DisplayCastingRecipe(
            getId(), getType(), withMetal(castItems, power.id()), fluids, MetalItem.setMetal(result.copy(), power.id()),
            ICastingRecipe.calcCoolingTime(power.temperature(), amount), isConsumed());
        }).collect(Collectors.toList());
    }
    return multiRecipes;
  }
}

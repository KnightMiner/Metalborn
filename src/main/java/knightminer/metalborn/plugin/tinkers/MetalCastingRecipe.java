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
import slimeknights.mantle.recipe.helper.TagPreference;
import slimeknights.mantle.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.library.recipe.casting.AbstractCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.ICastingContainer;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;

import java.util.ArrayList;
import java.util.List;

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

  @Override
  public List<IDisplayableCastingRecipe> getRecipes(RegistryAccess access) {
    if (multiRecipes == null) {
      List<ItemStack> results = new ArrayList<>();
      List<FluidStack> fluids = new ArrayList<>();
      int maxTime = 0;
      for (MetalPower power : MetalManager.INSTANCE.getSortedPowers()) {
        if (power.temperature() > 0 && filter.test(power) && TagPreference.getPreference(power.fluid()).isPresent()) {
          // add all new fluids to the animation
          List<FluidStack> newFluids = ((FluidIngredient)FluidIngredient.of(power.fluid(), amount)).getFluids();
          fluids.addAll(newFluids);
          // add a copy of the result per fluid
          ItemStack newResult = MetalItem.setMetal(result.copy(), power.id());
          for (int i = 0; i < newFluids.size(); i++) {
            results.add(newResult);
          }
          int newTime = ICastingRecipe.calcCoolingTime(power.temperature(), amount);
          if (newTime > maxTime) {
            maxTime = newTime;
          }
        }
      }
      // if we got anything, make the display recipe
      if (!results.isEmpty()) {
        multiRecipes = List.of(new DisplayRecipe(getId(), List.of(getCast().getItems()), isConsumed(), List.copyOf(fluids), List.copyOf(results), maxTime));
      } else {
        multiRecipes = List.of();
      }
    }
    return multiRecipes;
  }

  /** Display recipe implementation - handles dynamic cooling time from the fluid */
  private record DisplayRecipe(ResourceLocation getRecipeId, List<ItemStack> getCastItems, boolean isConsumed, List<FluidStack> getFluids, List<ItemStack> getOutputs, int getCoolingTime) implements IDisplayableCastingRecipe {
    @Override
    public boolean hasCast() {
      return !getCastItems.isEmpty();
    }

    @Override
    public boolean linkCastToOutput() {
      return false;
    }

    @Override
    public boolean linkFluidsToOutput() {
      return true;
    }

    @Deprecated
    @Override
    public ItemStack getOutput() {
      return getOutputs.get(0);
    }

    @Override
    public boolean isCoolingTimeDynamic() {
      return true;
    }

    @Override
    public int getCoolingTime(FluidStack fluid) {
      int temperature = MetalManager.INSTANCE.fromFluid(fluid.getFluid()).temperature();
      if (temperature == 0) {
        return getCoolingTime;
      }
      return ICastingRecipe.calcCoolingTime(temperature, fluid.getAmount());
    }
  }
}

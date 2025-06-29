package knightminer.metalborn.plugin.tinkers;

import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.MetalItem;
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
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.IMultiRecipe;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.TagPreference;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer;
import slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/** Recipe for melting a metal item into the proper fluid based on its NBT */
public class MetalMeltingRecipe implements IMeltingRecipe, IMultiRecipe<MeltingRecipe> {
  public static final RecordLoadable<MetalMeltingRecipe> LOADER = RecordLoadable.create(
    ContextKey.ID.requiredField(),
    IngredientLoadable.DISALLOW_EMPTY.requiredField("ingredient", r -> r.ingredient),
    IntLoadable.FROM_ONE.requiredField("amount", r -> r.amount),
    MetalMeltingRecipe::new);

  private final ResourceLocation id;
  private final Ingredient ingredient;
  private final int amount;
  protected MetalMeltingRecipe(ResourceLocation id, Ingredient ingredient, int amount) {
    this.id = id;
    this.ingredient = ingredient;
    this.amount = amount;
  }

  @Override
  public ResourceLocation getId() {
    return id;
  }

  @Override
  public boolean matches(IMeltingContainer inv, Level pLevel) {
    // must match the cast
    ItemStack stack = inv.getStack();
    if (!ingredient.test(stack)) {
      return false;
    }
    // must have a metal
    MetalId metal = MetalItem.getMetal(stack);
    if (metal == MetalId.NONE) {
      return false;
    }
    // metal power must have a temperature (i.e. its meltable) and have a fluid
    MetalPower power = MetalManager.INSTANCE.get(metal);
    return power.temperature() > 0 && TagPreference.getPreference(power.fluid()).isPresent();
  }

  @Override
  public int getTemperature(IMeltingContainer inv) {
    return MetalManager.INSTANCE.get(MetalItem.getMetal(inv.getStack())).temperature();
  }

  @Override
  public int getTime(IMeltingContainer inv) {
    return IMeltingRecipe.calcTimeForAmount(getTemperature(inv), amount);
  }

  @Override
  public FluidStack getOutput(IMeltingContainer inv) {
    return TagPreference.getPreference(MetalManager.INSTANCE.get(MetalItem.getMetal(inv.getStack())).fluid())
      .map(value -> new FluidStack(value, amount))
      .orElse(FluidStack.EMPTY);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return Registration.METAL_MELTING.get();
  }


  /* JEI display */
  private List<MeltingRecipe> multiRecipes = null;

  @Override
  public List<MeltingRecipe> getRecipes(RegistryAccess access) {
    if (multiRecipes == null) {
      // working under the assumption we only have 1 item per metal
      multiRecipes = Arrays.stream(ingredient.getItems())
        .flatMap(stack -> {
          MetalPower power = MetalManager.INSTANCE.get(MetalItem.getMetal(stack));
          int temperature = power.temperature();
          if (temperature > 0) {
            return Stream.of(new MeltingRecipe(
              id, "", Ingredient.of(stack), FluidOutput.fromTag(power.fluid(), amount), temperature,
              IMeltingRecipe.calcTimeForAmount(temperature, amount), List.of(), false));
          }
          return Stream.empty();
        }).toList();
    }
    return multiRecipes;
  }
}

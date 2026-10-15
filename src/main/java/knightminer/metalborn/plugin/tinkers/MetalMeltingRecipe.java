package knightminer.metalborn.plugin.tinkers;

/* FIXME: update when Tinkers' ports
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MultiNamedRecipe;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.TagPreference;
import slimeknights.tconstruct.library.recipe.melting.DisplayMeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.IDisplayableMeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer;
import slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/** Recipe for melting a metal item into the proper fluid based on its NBT *//*
public class MetalMeltingRecipe implements IMeltingRecipe, MultiNamedRecipe<IDisplayableMeltingRecipe> {
  public static final RecordLoadable<MetalMeltingRecipe> LOADER = RecordLoadable.create(
    Loadables.ITEM_INGREDIENT_NONEMPTY.requiredField("ingredient", r -> r.ingredient),
    IntLoadable.FROM_ONE.requiredField("amount", r -> r.amount),
    MetalMeltingRecipe::new);

  private final Ingredient ingredient;
  private final int amount;
  protected MetalMeltingRecipe(Ingredient ingredient, int amount) {
    this.ingredient = ingredient;
    this.amount = amount;
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


  /* JEI display *//*
  private List<NamedRecipe<IDisplayableMeltingRecipe>> multiRecipes = null;

  @Override
  public List<NamedRecipe<IDisplayableMeltingRecipe>> getRecipes(ResourceLocation id, HolderLookup.Provider provider) {
    if (multiRecipes == null) {
      // working under the assumption we only have 1 item per metal
      multiRecipes = Arrays.stream(ingredient.getItems())
        .flatMap(stack -> {
          MetalPower power = MetalManager.INSTANCE.get(MetalItem.getMetal(stack));
          int temperature = power.temperature();
          if (temperature > 0) {
            return Stream.of(NamedRecipe.of(id, DisplayMeltingRecipe.id(id)
              .input(stack)
              .output(FluidOutput.fromTag(power.fluid(), amount))
              .temperature(temperature)
              .time(IMeltingRecipe.calcTimeForAmount(temperature, amount))
              .build()));
          }
          return Stream.empty();
        }).toList();
    }
    return multiRecipes;
  }
}
*/

package knightminer.metalborn.plugin.jei;

import com.mojang.datafixers.util.Function7;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.block.ForgeInventory;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.Fillable;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.item.SpikeItem;
import knightminer.metalborn.json.recipe.MetalResult;
import knightminer.metalborn.json.recipe.cooking.CookingMetalRecyclingRecipe;
import knightminer.metalborn.json.recipe.forge.ForgeRecipe;
import knightminer.metalborn.menu.ForgeMenu;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.recipe.helper.RecipeHelper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/** Plugin adding any relevant things to JEI */
@JeiPlugin
public class JEIPlugin implements IModPlugin {
  private static final ResourceLocation ID = Metalborn.resource("jei_plugin");

  @Override
  public ResourceLocation getPluginUid() {
    return ID;
  }

  @Override
  public void registerItemSubtypes(ISubtypeRegistration registration) {
    IIngredientSubtypeInterpreter<ItemStack> metal = (stack, context) -> {
      MetalId id = MetalItem.getMetal(stack);
      return id == MetalId.NONE ? IIngredientSubtypeInterpreter.NONE : id.toString();
    };

    registration.registerSubtypeInterpreter(Registration.CHANGE_FERRING.asItem(), metal);
    registration.registerSubtypeInterpreter(Registration.BRACER.asItem(), metal);
    registration.registerSubtypeInterpreter(Registration.RING.asItem(), metal);
    registration.registerSubtypeInterpreter(Registration.UNSEALED_RING.asItem(), metal);
    registration.registerSubtypeInterpreter(Registration.SPIKE.asItem(), (stack, context) -> {
      MetalId id = MetalItem.getMetal(stack);
      if (id == MetalId.NONE) {
        return IIngredientSubtypeInterpreter.NONE;
      }
      String type = id.toString();
      if (((SpikeItem)stack.getItem()).isFull(stack)) {
        type += ",full";
      }
      return type;
    });

    // only set variant on investiture items if full
    IIngredientSubtypeInterpreter<ItemStack> investiture = (stack, context) -> {
      if (((Fillable)stack.getItem()).isFull(stack)) {
        return MetalItem.getMetal(stack).toString();
      }
      return IIngredientSubtypeInterpreter.NONE;
    };
    registration.registerSubtypeInterpreter(Registration.INVESTITURE_RING.asItem(), investiture);
    registration.registerSubtypeInterpreter(Registration.INVESTITURE_BRACER.asItem(), investiture);
    registration.registerSubtypeInterpreter(Registration.INVESTITURE_SPIKE.asItem(), investiture);
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    registration.addRecipeCatalyst(Registration.FORGE, ForgeRecipeCategory.TYPE);
  }

  @Override
  public void registerCategories(IRecipeCategoryRegistration registration) {
    registration.addRecipeCategories(new ForgeRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
  }

  @Override
  public void registerRecipes(IRecipeRegistration registration) {
    Level level = SafeClientAccess.getLevel();
    if (level != null) {
      RecipeManager manager = level.getRecipeManager();
      registration.addRecipes(ForgeRecipeCategory.TYPE, RecipeHelper.getJEIRecipes(level.registryAccess(), manager, Registration.FORGE_RECIPE.get(), ForgeRecipe.class));
      registration.addRecipes(RecipeTypes.SMELTING, getRecycleRecipes(manager, RecipeType.SMELTING, SmeltingRecipe::new));
      registration.addRecipes(RecipeTypes.BLASTING, getRecycleRecipes(manager, RecipeType.BLASTING, BlastingRecipe::new));
    }
  }

  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(ForgeMenu.class, Registration.FORGE_MENU.get(), ForgeRecipeCategory.TYPE, ForgeInventory.GRID_START, ForgeInventory.GRID_SIZE, ForgeInventory.SIZE, 36);
  }

  /** Generates recycling recipes to show in JEI */
  private static <T extends AbstractCookingRecipe> List<T> getRecycleRecipes(RecipeManager manager, RecipeType<T> type, Function7<ResourceLocation,String, CookingBookCategory, Ingredient,ItemStack,Float,Integer,T> constructor) {
    return manager.getAllRecipesFor(type).stream()
      .flatMap(recipe -> {
        if (recipe instanceof CookingMetalRecyclingRecipe metal) {
          return Arrays.stream(recipe.getIngredients().get(0).getItems())
            .flatMap(input -> {
              MetalPower power = MetalManager.INSTANCE.get(MetalItem.getMetal(input));
              MetalResult result = metal.getResult();
              if (power != MetalPower.DEFAULT && result.isPresent(power)) {
                return Stream.of(constructor.apply(recipe.getId(), recipe.getGroup(), recipe.category(), Ingredient.of(input), result.get(power), recipe.getExperience(), recipe.getCookingTime()));
              }
              return Stream.empty();
            });
        }
        return Stream.empty();
      }).toList();
  }
}

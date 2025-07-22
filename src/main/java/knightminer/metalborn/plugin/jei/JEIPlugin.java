package knightminer.metalborn.plugin.jei;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.block.ForgeInventory;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.Fillable;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.item.SpikeItem;
import knightminer.metalborn.json.recipe.ForgeRecipe;
import knightminer.metalborn.menu.ForgeMenu;
import knightminer.metalborn.metal.MetalId;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.recipe.helper.RecipeHelper;

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
      registration.addRecipes(ForgeRecipeCategory.TYPE, RecipeHelper.getJEIRecipes(level.registryAccess(), level.getRecipeManager(), Registration.FORGE_RECIPE.get(), ForgeRecipe.class));
    }
  }

  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(ForgeMenu.class, Registration.FORGE_MENU.get(), ForgeRecipeCategory.TYPE, ForgeInventory.GRID_START, ForgeInventory.GRID_SIZE, ForgeInventory.SIZE, 36);
  }
}

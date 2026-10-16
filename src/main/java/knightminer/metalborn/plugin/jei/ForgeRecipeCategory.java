package knightminer.metalborn.plugin.jei;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.json.recipe.forge.ForgeRecipe;
import knightminer.metalborn.json.recipe.forge.ShapedForgeRecipe;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.plugin.jei.NamedRecipeCategory;
import slimeknights.mantle.recipe.NamedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** Recipe category for the forge */
public class ForgeRecipeCategory extends NamedRecipeCategory<ForgeRecipe> {
  private static final int GRID_WIDTH = 2;
  private static final int GRID_HEIGHT = 2;

  @SuppressWarnings({"unchecked", "RedundantCast"})
  public static final RecipeType<NamedRecipe<ForgeRecipe>> TYPE = RecipeType.create(Metalborn.MOD_ID, "forge", (Class<? extends NamedRecipe<ForgeRecipe>>)(Class<?>) NamedRecipe.class);
  private static final Component TITLE = Metalborn.component("jei", "forge");

  public ForgeRecipeCategory(IGuiHelper guiHelper) {
    super(TYPE, TITLE, guiHelper.createDrawableItemLike(Registration.FORGE), 122, 46);
  }

  /** Based on {@link mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension} for vanilla recipes */
  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, NamedRecipe<ForgeRecipe> holder, IFocusGroup focuses) {
    ForgeRecipe recipe = holder.recipe();
    List<List<ItemStack>> inputs = new ArrayList<>();
    for (Ingredient ingredient : recipe.getIngredients()) {
      List<ItemStack> items = List.of(ingredient.getItems());
      inputs.add(items);
    }
    IRecipeSlotBuilder output = createAndSetOutputs(builder, recipe.getResult());

    // find recipe size
    int width;
    int height;
    if (recipe instanceof ShapedForgeRecipe shaped) {
      width = shaped.getWidth();
      height = shaped.getHeight();
    } else {
      width = 0;
      height = 0;
      builder.setShapeless(60, 31);
    }

    // set input slots
    List<IRecipeSlotBuilder> inputSlots = createInputs(builder);
    setInputs(inputSlots, inputs, width, height);

    // setup focus links if requested
    int[] focusLinks = recipe.getLinkedInputs();
    if (focusLinks.length > 0) {
      builder.createFocusLink(Stream.concat(
        Stream.of(output),
        IntStream.of(focusLinks).mapToObj(i -> inputSlots.get(getCraftingIndex(i, width, height)))
      ).toArray(IIngredientAcceptor[]::new));
    }

    // fuel slot
    builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 1, 24)
      .setStandardSlotBackground();
  }

  /** Based on {@link mezz.jei.api.gui.ingredient.ICraftingGridHelper#createAndSetInputs(IRecipeLayoutBuilder, List, int, int)} */
  private static List<IRecipeSlotBuilder> createInputs(IRecipeLayoutBuilder builder) {
    // changed: don't call setShapeless, will do it in the parent

    // begin createAndSetInputs
    // changed: slot offset and grid size
    List<IRecipeSlotBuilder> inputSlots = new ArrayList<>();
    for (int y = 0; y < GRID_HEIGHT; y++) {
      for (int x = 0; x < GRID_WIDTH; x++) {
        IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, x * 18 + 23, y * 18 + 6) // 1)
          .setStandardSlotBackground();
        inputSlots.add(slot);
      }
    }

    return inputSlots;
  }

  /** Based on {@link mezz.jei.api.gui.ingredient.ICraftingGridHelper#setInputs(List, IIngredientType, List, int, int)} */
  private static void setInputs(List<IRecipeSlotBuilder> slotBuilders, List<List<ItemStack>> inputs, int width, int height) {
    // changed: shapeless only goes up to 2x2 instead of 3x3
    if (width <= 0 || height <= 0) {
      width = height = (inputs.size() > 1 ? 2 : 1);
    }
    if (slotBuilders.size() < width * height) {
      throw new IllegalArgumentException(String.format("There are not enough slots (%s) to hold a recipe of this size. (%sx%s)", slotBuilders.size(), width, height));
    }

    // set ingredients
    for (int i = 0; i < inputs.size(); i++) {
      // changed: using our helper for crafting index
      int index = getCraftingIndex(i, width, height);
      IRecipeSlotBuilder slot = slotBuilders.get(index);

      List<ItemStack> ingredients = inputs.get(i);
      if (ingredients != null) {
        slot.addItemStacks(ingredients);
      }
    }
  }

  /** Based on {@link mezz.jei.api.gui.ingredient.ICraftingGridHelper#createAndSetOutputs(IRecipeLayoutBuilder, List)} */
  private static IRecipeSlotBuilder createAndSetOutputs(IRecipeLayoutBuilder builder, List<ItemStack> result) {
    return builder.addOutputSlot(101, 15) //10)
      .setOutputSlotBackground()
      .addItemStacks(result);
  }

  /** Gets the index to set a slot given the grid size */
  private static int getCraftingIndex(int i, int width, int height) {
    int index;
    if (width == 1) {
      if (height > 1) {
        index = i * GRID_WIDTH;
      } else {
        index = 0;
      }
    } else {
      index = i;
    }
    return index;
  }


  /* Furnace display */

  @Override
  public void createRecipeExtras(IRecipeExtrasBuilder builder, NamedRecipe<ForgeRecipe> holder, IFocusGroup focuses) {
    ForgeRecipe recipe = holder.recipe();
    int cookTime = recipe.getCookingTime();
    if (cookTime <= 0) {
      cookTime = ForgeRecipe.DEFAULT_COOKING_TIME;
    }
    builder.addAnimatedRecipeArrowWidget(cookTime).setPosition(65, 14);
    builder.addAnimatedRecipeFlameWidget(300).setPosition(1, 7);

    addExperience(builder, recipe);
    addCookTime(builder, cookTime);
  }

  protected void addExperience(IRecipeExtrasBuilder builder, ForgeRecipe recipe) {
    float experience = recipe.getExperience();
    if (experience > 0) {
      Component experienceString = Component.translatable("gui.jei.category.smelting.experience", experience);
      builder.addText(experienceString, 64, 10)
        .setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.RIGHT, VerticalAlignment.TOP)
        .setTextAlignment(HorizontalAlignment.RIGHT)
        .setColor(0xFF808080);
    }
  }

  /** Based on JEI's AbstractCookingCategory */
  protected void addCookTime(IRecipeExtrasBuilder builder, int cookTime) {
    if (cookTime > 0) {
      int cookTimeSeconds = cookTime / 20;
      Component timeString = Component.translatable("gui.jei.category.smelting.time.seconds", cookTimeSeconds);
      builder.addText(timeString, 64, 10)
        .setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM)
        .setTextAlignment(HorizontalAlignment.RIGHT)
        .setTextAlignment(VerticalAlignment.BOTTOM)
        .setColor(0xFF808080);
    }
  }
}

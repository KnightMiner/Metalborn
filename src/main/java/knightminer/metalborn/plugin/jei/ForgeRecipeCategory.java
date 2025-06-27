package knightminer.metalborn.plugin.jei;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.recipe.ForgeRecipe;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IShapedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** Recipe category for the forge */
public class ForgeRecipeCategory implements IRecipeCategory<ForgeRecipe> {
  private static final int GRID_WIDTH = 2;
  private static final int GRID_HEIGHT = 2;
  public static final RecipeType<ForgeRecipe> TYPE = RecipeType.create(Metalborn.MOD_ID, "forge", ForgeRecipe.class);
  private static final Component TITLE = Metalborn.component("jei", "forge");

  private final IDrawable icon;

  public ForgeRecipeCategory(IGuiHelper guiHelper) {
    this.icon = guiHelper.createDrawableItemLike(Registration.FORGE);
  }

  @Override
  public RecipeType<ForgeRecipe> getRecipeType() {
    return TYPE;
  }

  @Override
  public Component getTitle() {
    return TITLE;
  }

  @Override
  public IDrawable getIcon() {
    return icon;
  }

  @Override
  public int getWidth() {
    return 122;
  }

  @Override
  public int getHeight() {
    return 46;
  }

  @Override
  public ResourceLocation getRegistryName(ForgeRecipe recipe) {
    return recipe.getId();
  }

  /** Based on {@link mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension} for vanilla recipes */
  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, ForgeRecipe recipe, IFocusGroup focuses) {
    List<List<ItemStack>> inputs = new ArrayList<>();
    for (Ingredient ingredient : recipe.getIngredients()) {
      List<ItemStack> items = List.of(ingredient.getItems());
      inputs.add(items);
    }
    IRecipeSlotBuilder output = createAndSetOutputs(builder, recipe.getResult());

    // find recipe size
    int width;
    int height;
    if (recipe instanceof IShapedRecipe<?> shaped) {
      width = shaped.getRecipeWidth();
      height = shaped.getRecipeHeight();
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
  public void createRecipeExtras(IRecipeExtrasBuilder builder, ForgeRecipe recipe, IFocusGroup focuses) {
    int cookTime = recipe.getCookingTime();
    if (cookTime <= 0) {
      cookTime = ForgeRecipe.DEFAULT_COOKING_TIME;
    }
    builder.addAnimatedRecipeArrow(cookTime).setPosition(65, 14);
    builder.addAnimatedRecipeFlame(300).setPosition(1, 7);

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

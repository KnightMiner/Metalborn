package knightminer.metalborn.json.recipe.cooking;

import knightminer.metalborn.core.Registration;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;

/** Extension of {@link SmeltingRecipe} to support {@link ItemOutput} */
public class SmeltingResultRecipe extends SmeltingRecipe {
  static LoadableField<CookingBookCategory, AbstractCookingRecipe> CATEGORY_FIELD = new EnumLoadable<>(CookingBookCategory.class).defaultField("category", CookingBookCategory.MISC, true, AbstractCookingRecipe::category);
  static LoadableField<Float, AbstractCookingRecipe> EXPERIENCE_FIELD = FloatLoadable.FROM_ZERO.defaultField("experience",0f, AbstractCookingRecipe::getExperience);
  static LoadableField<Integer, AbstractCookingRecipe> COOKING_TIME_FIELD = IntLoadable.FROM_ONE.defaultField("cooking_time", 200, true, AbstractCookingRecipe::getCookingTime);
  public static final RecordLoadable<SmeltingResultRecipe> LOADABLE = RecordLoadable.create(
    ContextKey.ID.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP, CATEGORY_FIELD,
    IngredientLoadable.DISALLOW_EMPTY.requiredField("ingredient", r -> r.ingredient),
    ItemOutput.Loadable.REQUIRED_STACK.requiredField("result", r -> r.result),
    EXPERIENCE_FIELD, COOKING_TIME_FIELD,
    SmeltingResultRecipe::new);

  private final ItemOutput result;
  public SmeltingResultRecipe(ResourceLocation id, String group, CookingBookCategory category, Ingredient ingredient, ItemOutput result, float experience, int cookingTime) {
    super(id, group, category, ingredient, ItemStack.EMPTY, experience, cookingTime);
    this.result = result;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return Registration.SMELTING.get();
  }

  @Override
  public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
    return result.get();
  }

  @Override
  public ItemStack assemble(Container pContainer, RegistryAccess pRegistryAccess) {
    return result.copy();
  }
}

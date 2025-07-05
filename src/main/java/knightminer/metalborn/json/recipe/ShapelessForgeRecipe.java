package knightminer.metalborn.json.recipe;

import com.mojang.datafixers.util.Function5;
import knightminer.metalborn.core.Registration;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.ArrayList;
import java.util.List;

/** Forge recipe matching a number of ingredients which can go in any slot */
public class ShapelessForgeRecipe extends AbstractForgeRecipe {
  /** Loadable instance to create the serializer */
  public static final RecordLoadable<ShapelessForgeRecipe> LOADABLE = makeLoader(ShapelessForgeRecipe::new);

  final NonNullList<Ingredient> ingredients;
  private final boolean isSimple;
  protected ShapelessForgeRecipe(ResourceLocation id, ItemOutput result, NonNullList<Ingredient> ingredients, float experience, int cookingTime) {
    super(id, result, experience, cookingTime);
    this.ingredients = ingredients;
    this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return Registration.SHAPELESS_FORGE.get();
  }

  @Override
  public NonNullList<Ingredient> getIngredients() {
    return ingredients;
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return ingredients.size() <= width * height;
  }

  @Override
  public boolean matches(CraftingContainer inv, Level level) {
    // simple matching lets us use the mojang helper, its faster
    if (isSimple) {
      StackedContents contents = new StackedContents();
      int stacks = 0;
      for (int slot = 0; slot < inv.getContainerSize(); slot++) {
        ItemStack itemstack = inv.getItem(slot);
        if (!itemstack.isEmpty()) {
          stacks++;
          contents.accountStack(itemstack, 1);
        }
      }
      return stacks == this.ingredients.size() && contents.canCraft(this, null);
    } else {
      // if not simple, use the forge matcher for full NBT checks
      List<ItemStack> inputs = new ArrayList<>();
      for (int slot = 0; slot < inv.getContainerSize(); slot++) {
        ItemStack itemstack = inv.getItem(slot);
        if (!itemstack.isEmpty()) {
          inputs.add(itemstack);
        }
      }
      return inputs.size() == this.ingredients.size() && RecipeMatcher.findMatches(inputs, this.ingredients) != null;
    }
  }

  /** Makes a loader for this recipe */
  public static <T extends ShapelessForgeRecipe> RecordLoadable<T> makeLoader(Function5<ResourceLocation,ItemOutput,NonNullList<Ingredient>,Float,Integer,T> constructor) {
    return RecordLoadable.create(
      ContextKey.ID.requiredField(), RESULT_FIELD,
      IngredientLoadable.DISALLOW_EMPTY.list(1).xmap((l, error) -> new NonNullList<>(l, null), (l, error) -> l).requiredField("ingredients", r -> r.ingredients),
      EXPERIENCE_FIELD, TIME_FIELD,
      constructor);
  }
}

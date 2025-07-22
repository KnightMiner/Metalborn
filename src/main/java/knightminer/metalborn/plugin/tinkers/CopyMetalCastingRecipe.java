package knightminer.metalborn.plugin.tinkers;

import knightminer.metalborn.item.MetalItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.library.recipe.casting.AbstractCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.ICastingContainer;
import slimeknights.tconstruct.library.recipe.casting.ItemCastingRecipe;

import java.util.Arrays;
import java.util.List;

/** Recipe that copies the metal variant of an input to the output */
public class CopyMetalCastingRecipe extends ItemCastingRecipe {
  /** Loader instance */
  public static final RecordLoadable<CopyMetalCastingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPED_SERIALIZER.requiredField(), ContextKey.ID.requiredField(),
    LoadableRecipeSerializer.RECIPE_GROUP,
    IngredientLoadable.DISALLOW_EMPTY.requiredField("cast", AbstractCastingRecipe::getCast),
    FLUID_FIELD, RESULT_FIELD, COOLING_TIME_FIELD, CAST_CONSUMED_FIELD, SWITCH_SLOTS_FIELD,
    CopyMetalCastingRecipe::new);

  public CopyMetalCastingRecipe(TypeAwareRecipeSerializer<?> serializer, ResourceLocation id, String group, Ingredient cast, FluidIngredient fluid, ItemOutput result, int coolingTime, boolean consumed, boolean switchSlots) {
    super(serializer, id, group, cast, fluid, result, coolingTime, consumed, switchSlots);
  }

  @Override
  public ItemStack assemble(ICastingContainer inv, RegistryAccess access) {
    ItemStack stack = super.assemble(inv, access);
    return MetalItem.setMetal(stack, MetalItem.getMetal(inv.getStack()));
  }


  /* JEI */

  private List<ItemStack> outputs;

  @Override
  public List<ItemStack> getOutputs() {
    if (outputs == null) {
      ItemStack result = this.result.get();
      outputs = Arrays.stream(getCast().getItems())
        .map(stack -> MetalItem.setMetal(result.copy(), MetalItem.getMetal(stack)))
        .toList();
    }
    return outputs;
  }
}

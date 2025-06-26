package knightminer.metalborn.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;

/** Common elements between {@link ShapedForgeRecipe} and {@link ShapelessForgeRecipe} */
public abstract class AbstractForgeRecipe implements ForgeRecipe {
  public static final int DEFAULT_COOKING_TIME = 200;
  protected static final LoadableField<ItemOutput,AbstractForgeRecipe> RESULT_FIELD = ItemOutput.Loadable.REQUIRED_STACK.requiredField("result", r -> r.result);
  protected static final LoadableField<Float,AbstractForgeRecipe> EXPERIENCE_FIELD = FloatLoadable.FROM_ZERO.defaultField("experience",0f, r -> r.experience);
  protected static final LoadableField<Integer,AbstractForgeRecipe> TIME_FIELD = IntLoadable.FROM_ONE.defaultField("cooking_time", DEFAULT_COOKING_TIME, true, r -> r.cookingTime);

  private final ResourceLocation id;
  private final ItemOutput result;
  private final float experience;
  private final int cookingTime;

  protected AbstractForgeRecipe(ResourceLocation id, ItemOutput result, float experience, int cookingTime) {
    this.id = id;
    this.result = result;
    this.experience = experience;
    this.cookingTime = cookingTime;
  }

  @Override
  public ResourceLocation getId() {
    return id;
  }

  @Override
  public ItemStack getResultItem(RegistryAccess registryAccess) {
    return result.get();
  }

  @Override
  public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
    return getResultItem(registryAccess).copy();
  }

  @Override
  public float getExperience() {
    return experience;
  }

  @Override
  public int getCookingTime() {
    return cookingTime;
  }
}

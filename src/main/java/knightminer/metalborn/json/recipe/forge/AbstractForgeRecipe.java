package knightminer.metalborn.json.recipe.forge;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.List;

/** Common elements between {@link ShapedForgeRecipe} and {@link ShapelessForgeRecipe} */
public abstract class AbstractForgeRecipe implements ForgeRecipe {
  protected static final LoadableField<ItemOutput,AbstractForgeRecipe> RESULT_FIELD = ItemOutput.REQUIRED_STACK_LOADABLE.requiredField("result", r -> r.result);
  protected static final LoadableField<Float,AbstractForgeRecipe> EXPERIENCE_FIELD = FloatLoadable.FROM_ZERO.defaultField("experience",0f, r -> r.experience);
  protected static final LoadableField<Integer,AbstractForgeRecipe> TIME_FIELD = IntLoadable.FROM_ONE.defaultField("cooking_time", DEFAULT_COOKING_TIME, true, r -> r.cookingTime);

  /** Ends the codec with the result, experience, and time fields. */
  protected static <T extends AbstractForgeRecipe> Products.P3<RecordCodecBuilder.Mu<T>, ItemOutput, Float, Integer> codecEnd(RecordCodecBuilder.Instance<T> instance) {
    return instance.group(
      ItemOutput.REQUIRED_STACK_CODEC.fieldOf("result").forGetter(r -> r.result),
      Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(r -> r.experience),
      Codec.INT.fieldOf("cooking_time").orElse(DEFAULT_COOKING_TIME).forGetter(r -> r.cookingTime)
    );
  }

  protected final ItemOutput result;
  protected final float experience;
  protected final int cookingTime;

  protected AbstractForgeRecipe(ItemOutput result, float experience, int cookingTime) {
    this.result = result;
    this.experience = experience;
    this.cookingTime = cookingTime;
  }

  @Override
  public List<ItemStack> getResult() {
    return List.of(result.get());
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider lookup) {
    return result.get();
  }

  @Override
  public ItemStack assemble(CraftingInput input, HolderLookup.Provider lookup) {
    return getResultItem(lookup).copy();
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

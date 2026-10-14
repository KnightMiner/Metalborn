package knightminer.metalborn.json.recipe.forge;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import knightminer.metalborn.core.Registration;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import slimeknights.mantle.recipe.helper.ItemOutput;

/** Cross between {@link net.minecraft.world.item.crafting.ShapedRecipe} and {@link net.minecraft.world.item.crafting.AbstractCookingRecipe} */
public class ShapedForgeRecipe extends AbstractForgeRecipe {
  public static final MapCodec<ShapedForgeRecipe> CODEC = codec(ShapedForgeRecipe::new);
  public static final StreamCodec<RegistryFriendlyByteBuf,ShapedForgeRecipe> STREAM_CODEC = streamCodec(ShapedForgeRecipe::new);

  /** Starts the codec with the result, experience, and time fields. */
  protected static <T extends ShapedForgeRecipe> MapCodec<T> codec(Function4<ShapedRecipePattern,ItemOutput,Float,Integer,T> constructor) {
    return RecordCodecBuilder.mapCodec(instance -> instance
      .group(ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern))
      .and(codecEnd(instance))
      .apply(instance, constructor));
  }

  /** Starts the codec with the result, experience, and time fields. */
  protected static <T extends ShapedForgeRecipe> StreamCodec<RegistryFriendlyByteBuf,T> streamCodec(Function4<ShapedRecipePattern,ItemOutput,Float,Integer,T> constructor) {
    return StreamCodec.composite(
      ShapedRecipePattern.STREAM_CODEC, r -> r.pattern,
      ItemOutput.REQUIRED_STREAM_CODEC, r -> r.result,
      ByteBufCodecs.FLOAT, r -> r.experience,
      ByteBufCodecs.VAR_INT, r -> r.cookingTime,
      constructor);
  }

  protected final ShapedRecipePattern pattern;

  public ShapedForgeRecipe(ShapedRecipePattern pattern, ItemOutput result, float experience, int cookingTime) {
    super(result, experience, cookingTime);
    this.pattern = pattern;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return Registration.SHAPED_FORGE.get();
  }

  /** Gets the height of this recipe */
  public int getHeight() {
    return pattern.height();
  }

  /** Gets the width of this recipe */
  public int getWidth() {
    return pattern.width();
  }

  @Override
  public NonNullList<Ingredient> getIngredients() {
    return pattern.ingredients();
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return pattern.width() <= width && pattern.height() <= height;
  }

  @Override
  public boolean matches(CraftingInput input, Level level) {
    return pattern.matches(input);
  }

  @Override
  public boolean isIncomplete() {
    NonNullList<Ingredient> ingredients = this.getIngredients();
    return ingredients.isEmpty() || ingredients.stream().filter(ingredient -> !ingredient.isEmpty()).anyMatch(Ingredient::hasNoItems);
  }
}

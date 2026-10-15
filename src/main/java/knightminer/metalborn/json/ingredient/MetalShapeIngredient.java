package knightminer.metalborn.json.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import knightminer.metalborn.metal.MetalShape;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.util.RegistryHelper;

import java.util.stream.Stream;

/** Ingredient matching a metal. Does not match a metal item with NBT metal. */
public record MetalShapeIngredient(MetalShape shape, MetalFilter filter) implements ICustomIngredient, IngredientWithMetal {
  public static final MapCodec<MetalShapeIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    MetalShape.CODEC.fieldOf("shape").forGetter(MetalShapeIngredient::shape),
    MetalFilter.CODEC.optionalFieldOf("filter", MetalFilter.ANY).forGetter(MetalShapeIngredient::filter)
  ).apply(instance, MetalShapeIngredient::new));
  public static final StreamCodec<RegistryFriendlyByteBuf,MetalShapeIngredient> STREAM_CODEC = StreamCodec.composite(
    MetalShape.STREAM_CODEC, MetalShapeIngredient::shape,
    MetalFilter.STREAM_CODEC, MetalShapeIngredient::filter,
    MetalShapeIngredient::new);

  /** Creates an ingredient matching ingots */
  public static Ingredient ingot(MetalFilter filter) {
    return new MetalShapeIngredient(MetalShape.INGOT, filter).toVanilla();
  }

  /** Creates an ingredient matching nuggets */
  public static Ingredient nugget(MetalFilter filter) {
    return new MetalShapeIngredient(MetalShape.NUGGET, filter).toVanilla();
  }

  @Override
  public IngredientType<MetalShapeIngredient> getType() {
    return Registration.METAL_SHAPE_INGREDIENT.get();
  }

  @Override
  public MetalFilter filter() {
    return filter;
  }

  /** Gets the metal matched by the given stack */
  public MetalPower getMetal(ItemStack stack) {
    if (shape == null || stack.is(shape.getTag())) {
      MetalPower power = MetalManager.INSTANCE.fromIngotOrNugget(stack.getItem());
      if (power != MetalPower.DEFAULT && filter.test(power)) {
        return power;
      }
    }
    return MetalPower.DEFAULT;
  }

  @Override
  public boolean test(ItemStack stack) {
    return getMetal(stack) != MetalPower.DEFAULT;
  }

  @Override
  public boolean isSimple() {
    // simple as we do just match a list of items, no NBT involved
    return true;
  }

  @Override
  public Stream<ItemStack> getItems() {
    return MetalManager.INSTANCE.getSortedPowers()
      .stream()
      .filter(filter)
      .flatMap(power -> RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, power.tag(shape)))
      .map(ItemStack::new);
  }
}

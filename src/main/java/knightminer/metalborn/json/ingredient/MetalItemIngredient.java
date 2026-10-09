package knightminer.metalborn.json.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.metal.MetalManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.stream.Stream;

/**
 * Ingredient that displays all metal variants for an item.
 * @see MetalShapeIngredient
 */
public record MetalItemIngredient(Item item, MetalFilter filter) implements ICustomIngredient, IngredientWithMetal {
  public static final MapCodec<MetalItemIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(MetalItemIngredient::item),
    MetalFilter.CODEC.optionalFieldOf("filter", MetalFilter.ANY).forGetter(MetalItemIngredient::filter)
  ).apply(instance, MetalItemIngredient::new));
  public static final StreamCodec<RegistryFriendlyByteBuf,MetalItemIngredient> STREAM_CODEC = StreamCodec.composite(
    ByteBufCodecs.registry(Registries.ITEM), MetalItemIngredient::item,
    MetalFilter.STREAM_CODEC, MetalItemIngredient::filter,
    MetalItemIngredient::new);

  public static final ResourceLocation ID = Metalborn.resource("metal_item");

  /** Creates a new instance */
  public static Ingredient of(ItemLike item, MetalFilter filter) {
    return new MetalItemIngredient(item.asItem(), filter).toVanilla();
  }

  @Override
  public IngredientType<MetalItemIngredient> getType() {
    return Registration.METAL_ITEM_INGREDIENT.get();
  }

  @Override
  public boolean test(ItemStack stack) {
    return stack.is(item);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public Stream<ItemStack> getItems() {
    return MetalManager.INSTANCE.getSortedPowers().stream()
      .filter(filter)
      .map(power -> MetalItem.setMetal(new ItemStack(item), power.id()));
  }
}

package knightminer.metalborn.json.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import knightminer.metalborn.metal.MetalShape;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.util.RegistryHelper;

import java.util.Collection;
import java.util.stream.Stream;

/** Ingredient matching a metal. Does not match a metal item with NBT metal. */
public class MetalShapeIngredient extends AbstractIngredient implements IngredientWithMetal {
  public static final ResourceLocation ID = Metalborn.resource("metal_shape");

  /** Shape to match, if null then any shape is valid */
  private final MetalShape shape;
  private final MetalFilter filter;
  private MetalShapeIngredient(MetalShape shape, MetalFilter filter) {
    super(Stream.of(new MetalValue(shape, filter)));
    this.filter = filter;
    this.shape = shape;
  }

  /** Creates an ingredient matching ingots */
  public static MetalShapeIngredient ingot(MetalFilter filter) {
    return new MetalShapeIngredient(MetalShape.INGOT, filter);
  }

  /** Creates an ingredient matching nuggets */
  public static MetalShapeIngredient nugget(MetalFilter filter) {
    return new MetalShapeIngredient(MetalShape.NUGGET, filter);
  }

  @Override
  public MetalFilter getFilter() {
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
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && getMetal(stack) != MetalPower.DEFAULT;
  }

  @Override
  public boolean isSimple() {
    // simple as we do just match a list of items, no NBT involved
    return true;
  }

  @Override
  public IIngredientSerializer<MetalShapeIngredient> getSerializer() {
    return SERIALIZER;
  }

  /** Serializes this to JSON */
  private static JsonObject serialize(MetalShape shape, MetalFilter filter) {
    JsonObject json = new JsonObject();
    json.addProperty("shape", MetalShape.LOADABLE.getString(shape));
    if (filter != MetalFilter.ANY) {
      json.addProperty("filter", MetalFilter.LOADABLE.getString(filter));
    }
    return json;
  }

  @Override
  public JsonElement toJson() {
    JsonObject json = serialize(shape, filter);
    json.addProperty("type", ID.toString());
    return json;
  }

  /** Ingredient value for a metal */
  private record MetalValue(MetalShape shape, MetalFilter filter) implements Value {
    @SuppressWarnings("deprecation")
    @Override
    public Collection<ItemStack> getItems() {
      return MetalManager.INSTANCE.getSortedPowers()
        .stream()
        .filter(filter)
        .flatMap(power -> RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, power.tag(shape)))
        .map(ItemStack::new)
        .toList();
    }

    @Override
    public JsonObject serialize() {
      return MetalShapeIngredient.serialize(shape, filter);
    }
  }

  /** Serializer instance */
  public static final IIngredientSerializer<MetalShapeIngredient> SERIALIZER = new IIngredientSerializer<>() {
    @Override
    public MetalShapeIngredient parse(JsonObject json) {
      return new MetalShapeIngredient(
        MetalShape.LOADABLE.getIfPresent(json, "shape"),
        MetalFilter.LOADABLE.getOrDefault(json, "filter", MetalFilter.ANY)
      );
    }

    @Override
    public MetalShapeIngredient parse(FriendlyByteBuf buffer) {
      return new MetalShapeIngredient(
        MetalShape.LOADABLE.decode(buffer),
        MetalFilter.LOADABLE.decode(buffer)
      );
    }

    @Override
    public void write(FriendlyByteBuf buffer, MetalShapeIngredient ingredient) {
      MetalShape.LOADABLE.encode(buffer, ingredient.shape);
      MetalFilter.LOADABLE.encode(buffer, ingredient.filter);
    }
  };
}

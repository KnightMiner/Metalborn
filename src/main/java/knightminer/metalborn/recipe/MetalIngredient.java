package knightminer.metalborn.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.util.RegistryHelper;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

/** Ingredient matching a metal */
public class MetalIngredient extends AbstractIngredient {
  public static final ResourceLocation ID = Metalborn.resource("metal");

  /** Shape to match, if null then any shape is valid */
  private final MetalShape shape;
  final MetalFilter filter;
  private MetalIngredient(MetalShape shape, MetalFilter filter) {
    super(Stream.of(new MetalValue(shape, filter)));
    this.filter = filter;
    this.shape = shape;
  }

  /** Creates an ingredient matching ingots */
  public static MetalIngredient ingot(MetalFilter filter) {
    return new MetalIngredient(MetalShape.INGOT, filter);
  }

  /** Creates an ingredient matching nuggets */
  public static MetalIngredient nugget(MetalFilter filter) {
    return new MetalIngredient(MetalShape.NUGGET, filter);
  }


  /** Gets the metal matched by the given stack */
  public MetalPower getMetal(ItemStack stack) {
    if (shape == null || stack.is(shape.tag)) {
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
  public IIngredientSerializer<MetalIngredient> getSerializer() {
    return SERIALIZER;
  }

  /** Serializes this to JSON */
  private static JsonObject serialize(MetalShape shape, MetalFilter filter) {
    JsonObject json = new JsonObject();
    json.addProperty("shape", MetalShape.LOADABLE.getString(shape));
    json.addProperty("filter", MetalFilter.LOADABLE.getString(filter));
    return json;
  }

  @Override
  public JsonElement toJson() {
    JsonObject json = serialize(shape, filter);
    json.addProperty("type", ID.toString());
    return json;
  }

  /** Represents different shapes of common metal items */
  private enum MetalShape {
    INGOT(Tags.Items.INGOTS),
    NUGGET(Tags.Items.NUGGETS);

    public static final EnumLoadable<MetalShape> LOADABLE = new EnumLoadable<>(MetalShape.class);

    private final TagKey<Item> tag;
    MetalShape(TagKey<Item> tag) {
      this.tag = tag;
    }
  }

  /** Filter on allowed metals */
  public enum MetalFilter implements Predicate<MetalPower> {
    ANY {
      @Override
      public boolean test(MetalPower metalPower) {
        return true;
      }
    },
    NATURAL_FERRING {
      @Override
      public boolean test(MetalPower power) {
        return power.ferring();
      }
    },
    METALMIND {
      @Override
      public boolean test(MetalPower power) {
        return !power.feruchemy().isEmpty();
      }
    },
    SPIKE {
      @Override
      public boolean test(MetalPower power) {
        return power.hemalurgyCharge() > 0 && !power.feruchemy().isEmpty();
      }
    };

    public static final EnumLoadable<MetalFilter> LOADABLE = new EnumLoadable<>(MetalFilter.class);
  }

  /** Ingredient value for a metal */
  private record MetalValue(MetalShape shape, MetalFilter filter) implements Value {
    @SuppressWarnings("deprecation")
    @Override
    public Collection<ItemStack> getItems() {
      return MetalManager.INSTANCE.getSortedPowers()
        .stream()
        .filter(filter)
        .flatMap(power -> RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, shape == MetalShape.INGOT ? power.ingot() : power.nugget()))
        .map(ItemStack::new)
        .toList();
    }

    @Override
    public JsonObject serialize() {
      return MetalIngredient.serialize(shape, filter);
    }
  }

  /** Serializer instance */
  public static final IIngredientSerializer<MetalIngredient> SERIALIZER = new IIngredientSerializer<>() {
    @Override
    public MetalIngredient parse(JsonObject json) {
      return new MetalIngredient(
        MetalShape.LOADABLE.getIfPresent(json, "shape"),
        MetalFilter.LOADABLE.getIfPresent(json, "filter")
      );
    }

    @Override
    public MetalIngredient parse(FriendlyByteBuf buffer) {
      return new MetalIngredient(
        MetalShape.LOADABLE.decode(buffer),
        MetalFilter.LOADABLE.decode(buffer)
      );
    }

    @Override
    public void write(FriendlyByteBuf buffer, MetalIngredient ingredient) {
      MetalShape.LOADABLE.encode(buffer, ingredient.shape);
      MetalFilter.LOADABLE.encode(buffer, ingredient.filter);
    }
  };
}

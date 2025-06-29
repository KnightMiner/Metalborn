package knightminer.metalborn.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.recipe.MetalIngredient.MetalFilter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.Loadables;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Ingredient that displays all metal variants for an item.
 * @see MetalIngredient
 */
public class MetalItemIngredient extends AbstractIngredient {
  public static final ResourceLocation ID = Metalborn.resource("metal_item");

  /** Item for serializing and testing */
  private final Item item;
  private final MetalFilter filter;

  /** Stacking IDs, has to be recreated as our value will create redundant IDs */
  @Nullable
  private IntList stackingIds;

  protected MetalItemIngredient(Item item, MetalFilter filter) {
    super(Stream.of(new MetalItemValue(item, filter)));
    this.item = item;
    this.filter = filter;
  }

  /** Creates a new instance */
  public static MetalItemIngredient of(ItemLike item, MetalFilter filter) {
    return new MetalItemIngredient(item.asItem(), filter);
  }


  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && stack.is(item);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @SuppressWarnings("deprecation")
  @Override
  public IntList getStackingIds() {
    if (stackingIds == null || checkInvalidation()) {
      markValid();
      stackingIds = new IntArrayList(1);
      stackingIds.add(BuiltInRegistries.ITEM.getId(item));
    }
    return stackingIds;
  }

  @Override
  protected void invalidate() {
    super.invalidate();
    stackingIds = null;
  }

  @Override
  public IIngredientSerializer<? extends Ingredient> getSerializer() {
    return SERIALIZER;
  }

  /** Serializes the item and filter to JSON */
  private static JsonObject serialize(Item item, MetalFilter filter) {
    JsonObject json = new JsonObject();
    json.add("item", Loadables.ITEM.serialize(item));
    if (filter != MetalFilter.ANY) {
      json.add("filter", MetalFilter.LOADABLE.serialize(filter));
    }
    return json;
  }

  @Override
  public JsonElement toJson() {
    JsonObject json = serialize(item, filter);
    json.addProperty("type", ID.toString());
    return json;
  }

  /** Item value for vanilla to build its display ingredient list */
  private record MetalItemValue(Item item, MetalFilter filter) implements Value {
    @Override
    public Collection<ItemStack> getItems() {
      return MetalManager.INSTANCE.getSortedPowers().stream()
        .filter(filter)
        .map(power -> MetalItem.setMetal(new ItemStack(item), power.id()))
        .toList();
    }

    @Override
    public JsonObject serialize() {
      return MetalItemIngredient.serialize(item, filter);
    }
  }

  /** Serializer instance */
  public static final IIngredientSerializer<MetalItemIngredient> SERIALIZER = new IIngredientSerializer<>() {
    @Override
    public MetalItemIngredient parse(JsonObject json) {
      return new MetalItemIngredient(
        Loadables.ITEM.getIfPresent(json, "item"),
        MetalFilter.LOADABLE.getOrDefault(json, "filter", MetalFilter.ANY)
      );
    }

    @Override
    public MetalItemIngredient parse(FriendlyByteBuf buffer) {
      return new MetalItemIngredient(
        Loadables.ITEM.decode(buffer),
        MetalFilter.LOADABLE.decode(buffer)
      );
    }

    @Override
    public void write(FriendlyByteBuf buffer, MetalItemIngredient ingredient) {
      Loadables.ITEM.encode(buffer, ingredient.item);
      MetalFilter.LOADABLE.encode(buffer, ingredient.filter);
    }
  };
}

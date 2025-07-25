package knightminer.metalborn.json.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntList;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.item.Fillable;
import knightminer.metalborn.item.metalmind.MetalmindItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;

import java.util.Arrays;

/** Ingredient matching filled stacks, used for spikes or metalminds */
public class FillableIngredient extends AbstractIngredient {
  public static final ResourceLocation ID = Metalborn.resource("fillable");

  private final Ingredient inner;
  private final FillState fillState;
  private ItemStack[] items;
  private FillableIngredient(Ingredient inner, FillState fillState) {
    this.inner = inner;
    this.fillState = fillState;
  }

  /** Creates an ingredient matching filled stacks */
  public static FillableIngredient of(FillState fillState, Ingredient inner) {
    return new FillableIngredient(inner, fillState);
  }

  /** Creates an ingredient matching filled stacks */
  public static FillableIngredient filled(Ingredient inner) {
    return new FillableIngredient(inner, FillState.FILLED);
  }

  /** Gets the ingredient nested inside */
  public Ingredient getInner() {
    return inner;
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return inner.test(stack) && stack != null && stack.getItem() instanceof Fillable fillable && fillState.test(fillable, stack);
  }

  @Override
  public ItemStack[] getItems() {
    if (items == null) {
      items = Arrays.stream(inner.getItems())
        .map(fillState::apply)
        .toArray(ItemStack[]::new);
    }
    return items;
  }

  @Override
  public IntList getStackingIds() {
    return inner.getStackingIds();
  }

  @Override
  public boolean isEmpty() {
    return inner.isEmpty();
  }

  @Override
  protected void invalidate() {
    super.invalidate();
    inner.checkInvalidation();
    items = null;
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IIngredientSerializer<? extends Ingredient> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public JsonElement toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("type", ID.toString());
    json.add("match", inner.toJson());
    json.add("when", FillState.LOADABLE.serialize(fillState));
    return json;
  }

  public enum FillState {
    /** Checks for a metal item that is completely full with any identity */
    FILLED {
      @Override
      public boolean test(Fillable fillable, ItemStack stack) {
        return fillable.isFull(stack);
      }

      @Override
      public ItemStack apply(ItemStack stack) {
        if (stack.getItem() instanceof Fillable fillable) {
          stack = stack.copy();
          fillable.setFull(stack);
        }
        return stack;
      }
    },
    /** Checks for a metal item that is not completely full */
    NOT_FILLED {
      @Override
      public boolean test(Fillable fillable, ItemStack stack) {
        return !fillable.isFull(stack);
      }
    },
    /** Checks for a metal item that is completely empty */
    EMPTY {
      @Override
      public boolean test(Fillable fillable, ItemStack stack) {
        return fillable.isEmpty(stack);
      }
    },
    /** Checks for a metal item that is completely full  */
    UNSEALED {
      @Override
      public boolean test(Fillable fillable, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return fillable.isFull(stack) && (tag == null || !tag.hasUUID(MetalmindItem.TAG_OWNER));
      }

      @Override
      public ItemStack apply(ItemStack stack) {
        return FILLED.apply(stack);
      }
    };

    public static final Loadable<FillState> LOADABLE = new EnumLoadable<>(FillState.class);

    /** Checks if the stack matches the fill state */
    public abstract boolean test(Fillable fillable, ItemStack stack);

    /** Applies the given state to the stack */
    public ItemStack apply(ItemStack stack) {
      return stack;
    }
  }

  /** Serializer instance */
  public static final IIngredientSerializer<FillableIngredient> SERIALIZER = new IIngredientSerializer<>() {
    @Override
    public FillableIngredient parse(JsonObject json) {
      return new FillableIngredient(
        IngredientLoadable.DISALLOW_EMPTY.getIfPresent(json, "match"),
        FillState.LOADABLE.getIfPresent(json, "when")
      );
    }

    @Override
    public FillableIngredient parse(FriendlyByteBuf buffer) {
      return new FillableIngredient(
        Ingredient.fromNetwork(buffer),
        FillState.LOADABLE.decode(buffer)
      );
    }

    @Override
    public void write(FriendlyByteBuf buffer, FillableIngredient ingredient) {
      ingredient.inner.toNetwork(buffer);
      FillState.LOADABLE.encode(buffer, ingredient.fillState);
    }
  };
}

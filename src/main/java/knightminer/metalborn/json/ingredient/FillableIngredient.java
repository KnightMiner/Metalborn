package knightminer.metalborn.json.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.Fillable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.network.MantleStreamCodecs;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

/** Ingredient matching filled stacks, used for spikes or metalminds */
public record FillableIngredient(Ingredient inner, FillState fillState) implements ICustomIngredient {
  public static final MapCodec<FillableIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    Ingredient.CODEC_NONEMPTY.fieldOf("match").forGetter(FillableIngredient::inner),
    FillState.CODEC.fieldOf("when").forGetter(FillableIngredient::fillState)
  ).apply(instance, FillableIngredient::new));
  public static final StreamCodec<RegistryFriendlyByteBuf,FillableIngredient> STREAM_CODEC = StreamCodec.composite(
    Ingredient.CONTENTS_STREAM_CODEC, FillableIngredient::inner,
    FillState.STREAM_CODEC, FillableIngredient::fillState,
    FillableIngredient::new);

  /** Creates an ingredient matching filled stacks */
  public static Ingredient of(FillState fillState, Ingredient inner) {
    return new FillableIngredient(inner, fillState).toVanilla();
  }

  /** Creates an ingredient matching filled stacks */
  public static Ingredient filled(Ingredient inner) {
    return of(FillState.FILLED, inner);
  }

  @Override
  public IngredientType<FillableIngredient> getType() {
    return Registration.FILLABLE_INGREDIENT.get();
  }

  @Override
  public boolean test(ItemStack stack) {
    return inner.test(stack) && stack.getItem() instanceof Fillable fillable && fillState.test(fillable, stack);
  }

  @Override
  public Stream<ItemStack> getItems() {
    return Arrays.stream(inner.getItems()).map(fillState);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  public enum FillState implements Function<ItemStack,ItemStack>, StringRepresentable {
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
    /** Checks for a metal item that is completely full but has no owner  */
    UNSEALED {
      @Override
      public boolean test(Fillable fillable, ItemStack stack) {
        return fillable.isFull(stack) && !stack.has(Registration.OWNER);
      }

      @Override
      public ItemStack apply(ItemStack stack) {
        return FILLED.apply(stack);
      }
    };

    public static final StringRepresentableCodec<FillState> CODEC = StringRepresentable.fromEnum(FillState::values);
    public static final StreamCodec<ByteBuf, FillState> STREAM_CODEC = MantleStreamCodecs.enumCodec(FillState.class, OutOfBoundsStrategy.WRAP);

    private final String name = name().toLowerCase(Locale.ROOT);

    @Override
    public String getSerializedName() {
      return name;
    }

    /** Checks if the stack matches the fill state */
    public abstract boolean test(Fillable fillable, ItemStack stack);

    /** Applies the given state to the stack */
    @Override
    public ItemStack apply(ItemStack stack) {
      return stack;
    }
  }
}

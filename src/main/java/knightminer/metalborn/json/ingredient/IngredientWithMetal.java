package knightminer.metalborn.json.ingredient;

import io.netty.buffer.ByteBuf;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.util.StringRepresentable;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.network.MantleStreamCodecs;

import java.util.Locale;
import java.util.function.Predicate;

/** Interface for {@link knightminer.metalborn.json.recipe.forge.MetalShapedForgeRecipe} and {@link knightminer.metalborn.json.recipe.forge.MetalShapelessForgeRecipe} to get the filter used by ingredients. */
public interface IngredientWithMetal {
  /** Gets the filter for this ingredient */
  MetalFilter filter();

  /** Filter on allowed metals */
  enum MetalFilter implements Predicate<MetalPower>, StringRepresentable {
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
    @SuppressWarnings("deprecation")  // then why is it new?
    public static final StringRepresentable.EnumCodec<MetalFilter> CODEC = StringRepresentable.fromEnum(MetalFilter::values);
    public static final StreamCodec<ByteBuf, MetalFilter> STREAM_CODEC = MantleStreamCodecs.enumCodec(MetalFilter.class, OutOfBoundsStrategy.WRAP);

    private final String name = name().toLowerCase(Locale.ROOT);

    @Override
    public String getSerializedName() {
      return name;
    }
  }
}

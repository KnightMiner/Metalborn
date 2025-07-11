package knightminer.metalborn.metal;

import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static knightminer.metalborn.Metalborn.key;

/** Logic for formatting metal amounts in metalminds */
public enum MetalFormat {
  /** Lists the amount with no label */
  NO_LABEL {
    @Override
    public Component format(MetalId metal, int value, int capacity) {
      return Component.translatable(KEY_NO_LABEL, value, capacity);
    }
  },
  /** Converts the amount from ticks (1/20 of a second) to seconds */
  TICKS {
    @Override
    public Component format(MetalId metal, int value, int capacity) {
      return Component.translatable(KEY_SECONDS, SINGLE_DECIMAL.format(value / 20f), capacity / 20);
    }
  },
  /** Displays the value as seconds directly */
  SECONDS {
    @Override
    public Component format(MetalId metal, int value, int capacity) {
      return Component.translatable(KEY_SECONDS, value, capacity);
    }
  },
  /** Lists the amount with a label fetched from the metal */
  METAL {
    @Override
    public Component format(MetalId metal, int value, int capacity) {
      return Component.translatable(metal.getTranslationKey() + ".label", value, capacity);
    }
  };

  private static final String KEY_NO_LABEL = key("gui", "amount.no_label");
  private static final String KEY_SECONDS = key("gui", "amount.seconds");
  private static final DecimalFormat SINGLE_DECIMAL = new DecimalFormat("#,##0.0", DecimalFormatSymbols.getInstance(Locale.US));

  /** Formats the given value and capacity */
  public abstract Component format(MetalId metal, int value, int capacity);
}

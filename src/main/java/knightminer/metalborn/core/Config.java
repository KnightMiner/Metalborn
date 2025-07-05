package knightminer.metalborn.core;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

public class Config {
  public static final ForgeConfigSpec COMMON_SPEC;
  public static final BooleanValue FORCE_INTEGRATION;

  static {
    ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

    builder.push("debug");
    {
      FORCE_INTEGRATION = builder.comment("Enables all compat metals even if the conditions don't match. Used to test compat content.")
        .define("force_integration", false);
    }
    builder.pop();

    COMMON_SPEC = builder.build();
  }
}

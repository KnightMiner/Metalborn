package knightminer.metalborn.core;

import knightminer.metalborn.metal.MetalId;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class Config {
  public static final ForgeConfigSpec COMMON_SPEC;

  /** Ferring type the player will spawn as */
  public static final ConfigValue<String> SPAWN_AS_FERRING;
  /** If true, ferring type is kept on death */
  public static final BooleanValue KEEP_ON_DEATH;
  /** If true, spikes can be used as a weapon in the offhand  */
  public static final BooleanValue OFFHAND_SPIKE_ATTACK;

  /** Forces integration metals to be enabled */
  public static final BooleanValue FORCE_INTEGRATION;

  static {
    ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();


    builder.push("gameplay");
    {
      SPAWN_AS_FERRING = builder.comment("Metal ID for the ferring type new players will spawn as. If empty, will randomize. Set to 'metalborn:none' to make players spawn without a power.")
        .define("spawn_as_ferring", "", o -> o instanceof String s && MetalId.tryParse(s) != null);
      KEEP_ON_DEATH = builder.comment("If true, ferring type is kept on death. If false, ferring type is reset to spawn type.")
        .define("keep_ferring_on_death", false);
      OFFHAND_SPIKE_ATTACK = builder.comment("If true, spikes can be used to attack mobs in the offhand, allowing you to dual wield them with swords. False requires main hand kills.")
        .define("offhand_spike_attack", true);
    }

    builder.push("debug");
    {
      FORCE_INTEGRATION = builder.comment("Enables all compat metals even if the conditions don't match. Used to test compat content.")
        .define("force_integration", false);
    }
    builder.pop();

    COMMON_SPEC = builder.build();
  }
}

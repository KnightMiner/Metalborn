package knightminer.metalborn.json;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import knightminer.metalborn.core.Config;
import knightminer.metalborn.core.Registration;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;

/** Condition that checks the config */
public record ConfigEnabledCondition(String name, BooleanSupplier supplier) implements ICondition, LootItemCondition {
  /** Map of config names to condition cache */
  private static final Map<String,ConfigEnabledCondition> PROPERTIES = new HashMap<>();
  /** Codec mapping from string to condition instance */
  private static final Codec<ConfigEnabledCondition> BY_NAME_CODEC = Codec.stringResolver(ConfigEnabledCondition::name, PROPERTIES::get);
  /** Map codec used for condition registration */
  public static final MapCodec<ConfigEnabledCondition> CODEC = BY_NAME_CODEC.fieldOf("name");

  /** @apiNote use static fields */
  @Internal
  public ConfigEnabledCondition {}

  @Override
  public MapCodec<ConfigEnabledCondition> codec() {
    return CODEC;
  }

  @Override
  public LootItemConditionType getType() {
    return Registration.CONFIG.get();
  }

  @Override
  public boolean test(IContext context) {
    return supplier.getAsBoolean();
  }

  @Override
  public boolean test(LootContext context) {
    return supplier.getAsBoolean();
  }


  /* Properties */
  /** Adds a condition */
  private static ConfigEnabledCondition add(String prop, BooleanSupplier supplier) {
    ConfigEnabledCondition condition = new ConfigEnabledCondition(prop, supplier);
    PROPERTIES.put(prop.toLowerCase(Locale.ROOT), condition);
    return condition;
  }

  /** Adds a condition */
  @SuppressWarnings("SameParameterValue")
  private static ConfigEnabledCondition add(String prop, ModConfigSpec.BooleanValue supplier) {
    return add(prop, supplier::get);
  }

  /** Passes if integration materials are force enabled */
  public static final ConfigEnabledCondition FORCE_INTEGRATION = add("force_integration", Config.FORCE_INTEGRATION);
}

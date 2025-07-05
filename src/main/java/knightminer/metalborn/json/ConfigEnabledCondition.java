package knightminer.metalborn.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Config;
import knightminer.metalborn.core.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;

/** Condition that checks the config */
public record ConfigEnabledCondition(String name, BooleanSupplier supplier) implements ICondition, LootItemCondition {
  public static final ResourceLocation ID = Metalborn.resource("config");
  /** Map of config names to condition cache */
  private static final Map<String,ConfigEnabledCondition> PROPERTIES = new HashMap<>();

  /** @apiNote use static fields */
  @Internal
  public ConfigEnabledCondition {}

  @Override
  public ResourceLocation getID() {
    return ID;
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

  /** Serializer instance */
  public enum Serializer implements net.minecraft.world.level.storage.loot.Serializer<ConfigEnabledCondition>, IConditionSerializer<ConfigEnabledCondition> {
    INSTANCE;

    @Override
    public ResourceLocation getID() {
      return ID;
    }

    @Override
    public void write(JsonObject json, ConfigEnabledCondition value) {
      json.addProperty("name", value.name());
    }

    @Override
    public void serialize(JsonObject json, ConfigEnabledCondition value, JsonSerializationContext context) {
      write(json, value);
    }

    @Override
    public ConfigEnabledCondition read(JsonObject json) {
      String name = GsonHelper.getAsString(json, "name");
      ConfigEnabledCondition condition = PROPERTIES.get(name);
      if (condition == null) {
        throw new JsonSyntaxException("Unknown config property: " + name);
      }
      return condition;
    }

    @Override
    public ConfigEnabledCondition deserialize(JsonObject json, JsonDeserializationContext context) {
      return read(json);
    }
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
  private static ConfigEnabledCondition add(String prop, BooleanValue supplier) {
    return add(prop, supplier::get);
  }

  /** Passes if integration materials are force enabled */
  public static final ConfigEnabledCondition FORCE_INTEGRATION = add("force_integration", Config.FORCE_INTEGRATION);
}

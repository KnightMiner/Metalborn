package knightminer.metalborn.metal;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import knightminer.metalborn.json.ConfigEnabledCondition;
import knightminer.metalborn.metal.effects.MetalEffect;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.AndCondition;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.OrCondition;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.condition.TagEmptyCondition;
import slimeknights.mantle.recipe.condition.TagFilledCondition;

import java.util.ArrayList;
import java.util.List;

/** Builder for {@link MetalPower} */
@CanIgnoreReturnValue
public class MetalPowerBuilder {
  private final MetalId id;
  private int index;
  private int capacity = 20 * 5 * 60; // 5 minutes
  private MetalFormat format = MetalFormat.TICKS;
  private int hemalurgyCharge = 10; // 10 kills with a spike is probably a big enough ask
  private int temperature = 0;
  private boolean ferring = true;
  private final List<MetalEffect> feruchemy = new ArrayList<>();
  private final List<ICondition> conditions = new ArrayList<>();
  // tags
  private String name;
  @Nullable
  private TagKey<Item> ingot;
  @Nullable
  private TagKey<Item> nugget;
  @Nullable
  private TagKey<Fluid> fluid;

  /** Builder constructor */
  private MetalPowerBuilder(MetalId id) {
    this.id = id;
    this.name = id.getPath();
  }

  /** Creates a new builder instance */
  public static MetalPowerBuilder builder(MetalId id) {
    return new MetalPowerBuilder(id);
  }

  /** Sets the index for the metal. */
  public MetalPowerBuilder index(int index) {
    this.index = index;
    return this;
  }


  /* Tags */

  /** Sets the name in this builder, which defines all tags. By default, it will be the ID path. */
  public MetalPowerBuilder name(String name) {
    this.name = name;
    return this;
  }

  /** Sets the ingot tag */
  public MetalPowerBuilder ingot(TagKey<Item> ingot) {
    this.ingot = ingot;
    return this;
  }

  /** Sets the nugget tag */
  public MetalPowerBuilder nugget(TagKey<Item> nugget) {
    this.nugget = nugget;
    return this;
  }

  /** Sets the fluid tag */
  public MetalPowerBuilder fluid(TagKey<Fluid> fluid) {
    this.fluid = fluid;
    return this;
  }

  /** Gets the tag value, or a default if unset */
  private <T> TagKey<T> orDefault(@Nullable TagKey<T> set, ResourceKey<? extends Registry<T>> registry, String prefix) {
    if (set != null) {
      return set;
    }
    return TagKey.create(registry, Mantle.commonResource(prefix + name));
  }

  /** Gets the ingot tag, constructing from {@link #name} if needed. */
  private TagKey<Item> getIngot() {
    return orDefault(ingot, Registries.ITEM, "ingots/");
  }

  /** Gets the nugget tag, constructing from {@link #name} if needed. */
  private TagKey<Item> getNugget() {
    return orDefault(nugget, Registries.ITEM, "nuggets/");
  }


  /* Conditions */

  /** Adds a condition to the builder */
  public MetalPowerBuilder condition(ICondition condition) {
    this.conditions.add(condition);
    return this;
  }

  /** Sets the metal to be optional based on the ingot and nugget. Must run after {@link #name(String)} for best results (or use the default). */
  public MetalPowerBuilder integrationNoForce() {
    return condition(new OrCondition(new TagFilledCondition<>(getIngot()), new TagFilledCondition<>(getNugget())));
  }

  /** Sets the metal to be optional based on the ingot and nugget. Must run after {@link #name(String)} for best results (or use the default). */
  public MetalPowerBuilder integration() {
    return condition(new OrCondition(
      ConfigEnabledCondition.FORCE_INTEGRATION,
      new TagFilledCondition<>(getIngot()),
      new TagFilledCondition<>(getNugget())
    ));
  }

  /** Sets the metal to be optional based on the ingot and nugget, but only present if the listed metal is *not* present. */
  public MetalPowerBuilder alternative(String disable) {
    return condition(new OrCondition(
      ConfigEnabledCondition.FORCE_INTEGRATION,
      new AndCondition(
        new TagEmptyCondition<>(ItemTags.create(Mantle.commonResource("ingots/" + disable))),
        new TagEmptyCondition<>(ItemTags.create(Mantle.commonResource("nuggets/" + disable))),
        new OrCondition(
          new TagFilledCondition<>(getIngot()),
          new TagFilledCondition<>(getNugget())
        )
      )
    ));
  }

  /** Sets the metal to be enabled unless the specified metal is present. */
  public MetalPowerBuilder unless(String disable) {
    return condition(new AndCondition(
      new TagEmptyCondition<>(ItemTags.create(Mantle.commonResource("ingots/" + disable))),
      new TagEmptyCondition<>(ItemTags.create(Mantle.commonResource("nuggets/" + disable)))
    ));
  }


  /* Effects */

  /** Disallows ferrings to spawn with this metal. Not relevant */
  public MetalPowerBuilder disallowFerring() {
    this.ferring = false;
    return this;
  }

  /** Adds a new feruchemy effect */
  public MetalPowerBuilder feruchemy(MetalEffect effect) {
    this.feruchemy.add(effect);
    return this;
  }

  /** Sets the metalmind capacity for this metal. */
  public MetalPowerBuilder capacity(MetalFormat format, int capacity) {
    this.format = format;
    this.capacity = capacity;
    return this;
  }

  /** Sets the amount of charge needed to fill this spike */
  public MetalPowerBuilder hemalurgyCharge(int charge) {
    this.hemalurgyCharge = charge;
    return this;
  }

  /** Sets the temperature for this fluid, for Tinkers' Construct compat. If 0, this will not be castable. */
  public MetalPowerBuilder temperature(int temperature) {
    this.temperature = temperature;
    return this;
  }


  /** Builds the instance with fewer checks */
  @CheckReturnValue
  private MetalPower buildInternal() {
    return new MetalPower(
      id, index,
      getIngot(), getNugget(), orDefault(fluid, Registries.FLUID, "molten_"), temperature,
      MetalId.getTargetTag(id),
      ferring, feruchemy, capacity, format,
      hemalurgyCharge);
  }

  /** Builds the final power */
  @CheckReturnValue
  public MetalPower build() {
    if (!conditions.isEmpty()) {
      throw new IllegalStateException("Cannot have conditions when building a metal power statically");
    }
    return buildInternal();
  }

  /** Builds the final power for serializing in datagen */
  @CheckReturnValue
  public JsonObject serialize() {
    JsonObject json = new JsonObject();
    // name is just used during deserializing to default values
    if (ingot == null || nugget == null || (temperature > 0 && fluid == null)) {
      json.addProperty("name", name);
    }
    MetalPower.LOADABLE.serialize(buildInternal(), json);
    if (!conditions.isEmpty()) {
      JsonArray array = new JsonArray();
      for (ICondition condition : conditions) {
        array.add(CraftingHelper.serialize(condition));
      }
      json.add("conditions", array);
    }
    return json;
  }
}

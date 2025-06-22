package knightminer.metalborn.metal;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import knightminer.metalborn.metal.effects.MetalEffect;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.condition.TagFilledCondition;

import java.util.ArrayList;
import java.util.List;

/** Builder for {@link MetalPower} */
@CanIgnoreReturnValue
public class MetalPowerBuilder {
  private final MetalId id;
  private String name;
  private int index;
  private int capacity = 20 * 5 * 60; // 5 minutes
  private boolean ferring = true;
  private final List<MetalEffect> feruchemy = new ArrayList<>();
  private final List<MetalEffect> hemalurgy = new ArrayList<>();
  private final List<ICondition> conditions = new ArrayList<>();

  /** Builder constructor */
  private MetalPowerBuilder(MetalId id) {
    this.id = id;
    this.name = id.getPath();
  }

  /** Creates a new builder instance */
  public static MetalPowerBuilder builder(MetalId id) {
    return new MetalPowerBuilder(id);
  }

  /** Sets the name in this builder. By default, it will be the ID path. */
  public MetalPowerBuilder name(String name) {
    this.name = name;
    return this;
  }

  /** Sets the index for the metal. */
  public MetalPowerBuilder index(int index) {
    this.index = index;
    return this;
  }

  /** Adds a condition to the builder */
  public MetalPowerBuilder condition(ICondition condition) {
    this.conditions.add(condition);
    return this;
  }

  /** Sets the metal to be optional based on the ingot and nugget. Must run after {@link #name(String)} for best results (or use the default). */
  public MetalPowerBuilder optional() {
    // TODO: force enable condition for easier testing
    condition(new TagFilledCondition<>(ItemTags.create(Mantle.commonResource("ingots/" + name))));
    condition(new TagFilledCondition<>(ItemTags.create(Mantle.commonResource("nuggets/" + name))));
    return this;
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
  public MetalPowerBuilder capacity(int capacity) {
    this.capacity = capacity;
    return this;
  }

  /** Adds a new hemalurgy effect */
  public MetalPowerBuilder hemalurgy(MetalEffect effect) {
    this.hemalurgy.add(effect);
    return this;
  }

  /** Adds a new effect to both feruchemy and hemalurgy */
  public MetalPowerBuilder effect(MetalEffect effect) {
    feruchemy(effect);
    hemalurgy(effect);
    return this;
  }


  /** Builds the instance with fewer checks */
  @CheckReturnValue
  private MetalPower buildInternal() {
    return new MetalPower(id, name, index, ferring, feruchemy, feruchemy.isEmpty() ? 0 : capacity, hemalurgy);
  }

  /** Builds the final power */
  @CheckReturnValue
  public MetalPower build() {
    if (!conditions.isEmpty()) {
      throw new IllegalStateException("Cannot have conditions when building a metal power statically");
    }
    return buildInternal();
  }

  /** Builds the final power for serialziing in datagen */
  @CheckReturnValue
  public JsonObject serialize() {
    JsonObject json = new JsonObject();
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

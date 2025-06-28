package knightminer.metalborn.metal;

import knightminer.metalborn.metal.effects.MetalEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.RegistryHelper;

import java.util.List;

/**
 * Data container for all metal effects performed by a metal, from Feruchemy and Hemalurgy.
 * @param id         ID of this metal, corresponds to storage in NBT and recipes.
 * @param name       Tag suffix for this metal
 * @param ingot      Ingot tag for this metal, used in recipes.
 * @param nugget     Nugget tag for this metal, used in recipes.
 * @param target     Entity tag of targets that can provide this metal through hemalurgy.
 * @param index      Sort order for this metal.
 * @param ferring    If true, this power can be randomly granted.
 * @param feruchemy  List of feruchemical effects to perform
 * @param capacity   Capacity of the base size metalmind.
 * @param hemalurgyCharge  Amount of charge needed to fill this spike. If 0, this is not usable as a spike.
 */
public record MetalPower(
  MetalId id,
  String name,
  TagKey<Item> ingot,
  TagKey<Item> nugget,
  TagKey<EntityType<?>> target,
  int index,
  boolean ferring,
  List<MetalEffect> feruchemy,
  int capacity,
  int hemalurgyCharge
) {
  /** Loader instane for parsing from JSON and syncing over the network */
  public static final RecordLoadable<MetalPower> LOADABLE = RecordLoadable.create(
    ContextKey.ID.mappedField((id, error) -> new MetalId(id)),
    StringLoadable.DEFAULT.requiredField("name", MetalPower::name),
    IntLoadable.FROM_ZERO.requiredField("index", MetalPower::index),
    new AllowFerringField("allow_ferring", "feruchemy"),
    MetalEffect.LIST_LOADABLE.defaultField("feruchemy", List.of(), MetalPower::feruchemy),
    IntLoadable.FROM_ZERO.defaultField("capacity", 0, MetalPower::capacity),
    IntLoadable.FROM_ZERO.defaultField("hemalurgy_charge", 0, MetalPower::hemalurgyCharge),
    MetalPower::new);

  /** Default instance for when a metal does not exist */
  public static final MetalPower DEFAULT = new MetalPower(MetalId.NONE, "none", -1, false, List.of(), 0, 0);

  /** @apiNote Use {@link MetalPowerBuilder} */
  @Internal
  public MetalPower {}

  /**
   * Constructor that automatically creates the tag names
   * @apiNote Use {@link MetalPowerBuilder}
   */
  MetalPower(MetalId id, String name, int index, boolean ferring, List<MetalEffect> feruchemy, int capacity, int hemalurgyCharge) {
    this(
      id, name,
      ItemTags.create(Mantle.commonResource("ingots/" + name)),
      ItemTags.create(Mantle.commonResource("nuggets/" + name)),
      MetalId.getTargetTag(id),
      index, ferring, feruchemy, capacity, hemalurgyCharge
    );
  }

  /* Empty */

  /** Checks if this metal power is registered in datapacks */
  public boolean isPresent() {
    return this != DEFAULT;
  }


  /* Resource matching */

  /** {@return true if this stack is a valid ingot for this power} */
  public boolean matchesIngot(Item item) {
    return RegistryHelper.contains(ingot, item);
  }

  /** {@return true if this stack is a valid nugget for this power} */
  public boolean matchesNugget(Item item) {
    return RegistryHelper.contains(nugget, item);
  }

  /** {@return true if this stack is a valid ingot or nugget for this power} */
  public boolean matches(Item item) {
    return matchesIngot(item) || matchesNugget(item);
  }

  /** {@return true if this entity matches the metal} */
  public boolean matches(EntityType<?> entity) {
    return entity.is(this.target);
  }


  /* Effect hooks */

  /** Called when the level of this effect changes to update behavior */
  public void onChange(LivingEntity entity, int level, int previous) {
    for (MetalEffect effect : feruchemy) {
      effect.onChange(this, entity, level, previous);
    }
  }

  /** Called every tick while this power is active. */
  public int onTick(LivingEntity entity, int level) {
    int consumed = 0;
    for (MetalEffect effect : feruchemy) {
      int effectConsumed = effect.onTick(this, entity, level);
      // take the most extreme effect
      if (level > 0 ? (effectConsumed > consumed) : (effectConsumed < consumed)) {
        consumed = effectConsumed;
      }
    }
    return consumed;
  }

  /** Called every tick while this power is active. */
  public void getTooltip(LivingEntity entity, int level, List<Component> tooltip) {
    for (MetalEffect effect : feruchemy) {
      effect.getTooltip(this, entity, level, tooltip);
    }
  }
}

package knightminer.metalborn.metal;

import knightminer.metalborn.metal.effects.MetalEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.RegistryHelper;

import java.util.List;

/**
 * Data container for all metal effects performed by a metal, from Feruchemy and Hemalurgy.
 * @param id           ID of this metal, corresponds to storage in NBT and recipes.
 * @param name         Tag suffix for this metal
 * @param ingot        Ingot tag for this metal, used in recipes.
 * @param nugget       Nugget tag for this metal, used in recipes.
 * @param fluid        Fluid tag for Tinkers' Construct compatibility
 * @param temperature  Temperature for Tinkers' Construct compatibility
 * @param target       Entity tag of targets that can provide this metal through hemalurgy.
 * @param index        Sort order for this metal.
 * @param ferring      If true, this power can be randomly granted.
 * @param feruchemy    List of feruchemical effects to perform
 * @param capacity     Capacity of the base size metalmind.
 * @param format       Logic for formatting the amount.
 * @param hemalurgyCharge  Amount of charge needed to fill this spike. If 0, this is not usable as a spike.
 */
public record MetalPower(
  MetalId id, int index,
  TagKey<Item> ingot, TagKey<Item> nugget, TagKey<Fluid> fluid, int temperature,
  TagKey<EntityType<?>> target,
  boolean ferring, List<MetalEffect> feruchemy,
  int capacity, MetalFormat format,
  int hemalurgyCharge
) {
  /** Loader instane for parsing from JSON and syncing over the network */
  public static final RecordLoadable<MetalPower> LOADABLE = RecordLoadable.create(
    ContextKey.ID.mappedField((id, error) -> new MetalId(id)),
    IntLoadable.FROM_ZERO.requiredField("index", MetalPower::index),
    new DefaultingTagField<>("ingot", Registries.ITEM, "ingots/", MetalPower::ingot),
    new DefaultingTagField<>("nugget", Registries.ITEM, "nuggets/", MetalPower::nugget),
    new DefaultingTagField<>("fluid", Registries.FLUID, "molten_", MetalPower::fluid),
    IntLoadable.FROM_ZERO.defaultField("temperature", 0, false, MetalPower::temperature),
    ContextKey.ID.mappedField((id, error) -> MetalId.getTargetTag(id)),
    new AllowFerringField("allow_ferring", "feruchemy"),
    MetalEffect.LIST_LOADABLE.defaultField("feruchemy", List.of(), MetalPower::feruchemy),
    IntLoadable.FROM_ZERO.defaultField("capacity", 0, MetalPower::capacity),
    new EnumLoadable<>(MetalFormat.class).requiredField("format", MetalPower::format),
    IntLoadable.FROM_ZERO.defaultField("hemalurgy_charge", 0, MetalPower::hemalurgyCharge),
    MetalPower::new);

  /** Default instance for when a metal does not exist */
  public static final MetalPower DEFAULT = new MetalPower(
    MetalId.NONE, -1,
    // random tags go, really just gives us an error state if this ends up displayed somewhere
    Tags.Items.STORAGE_BLOCKS_LAPIS, Tags.Items.GEMS_LAPIS, Tags.Fluids.MILK, 0, MetalId.getTargetTag(MetalId.NONE),
    false, List.of(), 0, MetalFormat.NO_LABEL, 0);

  /** @apiNote Use {@link MetalPowerBuilder} */
  @Internal
  public MetalPower {}

  /** Checks if this metal power is registered in datapacks */
  public boolean isPresent() {
    return this != DEFAULT;
  }

  /** Formats the amount and capacity according to the formatter */
  public Component format(int amount, int capacityMultiplier) {
    return format.format(id, amount, capacity * capacityMultiplier);
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

  /** Gets the tag for the given metal shape */
  public TagKey<Item> tag(MetalShape shape) {
    return switch (shape) {
      case INGOT -> ingot;
      case NUGGET -> nugget;
    };
  }

  /** {@return true if this fluid matches the metal} */
  @SuppressWarnings("deprecation")
  public boolean matches(Fluid fluid) {
    return fluid.is(this.fluid);
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
  public int onTap(LivingEntity entity, int level) {
    int consumed = 0;
    for (MetalEffect effect : feruchemy) {
      int effectConsumed = effect.onTap(this, entity, level);
      // take the most extreme effect
      if (effectConsumed > consumed) {
        consumed = effectConsumed;
      }
    }
    return consumed;
  }

  /** Called every tick while this power is active. */
  public int onStore(LivingEntity entity, int level) {
    int consumed = 0;
    for (MetalEffect effect : feruchemy) {
      int effectConsumed = effect.onStore(this, entity, level);
      // take the most extreme effect
      if (effectConsumed > consumed) {
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

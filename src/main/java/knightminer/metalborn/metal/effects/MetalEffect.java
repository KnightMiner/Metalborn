package knightminer.metalborn.metal.effects;

import knightminer.metalborn.metal.MetalPower;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;

import java.util.List;

/** Represents an effect that can be performed as a metal is used */
public interface MetalEffect extends IHaveLoader {
  /** Registry for fluid effect loaders */
  GenericLoaderRegistry<MetalEffect> REGISTRY = new GenericLoaderRegistry<>("Metal effect", true);
  /** Loadable for a list of metal effects */
  Loadable<List<MetalEffect>> LIST_LOADABLE = REGISTRY.list(0);

  /**
   * Called when the level of this effect changes to update behavior
   * @param power    Power instance
   * @param entity   Entity receiving the effect
   * @param level    Current level of the power. May be 0 to indicate power was removed, or negative to indicate reversed power.
   * @param previous Previous level of power. May be 0 to indicate power was not present before.
   */
  default void onChange(MetalPower power, LivingEntity entity, int level, int previous) {}

  /**
   * Called every tick while this power is tapping.
   * @param power    Power instance
   * @param entity   Entity receiving the effect
   * @param level    Current level of the power.
   * @return amount of the level to drain. Return 0 if nothing happened.
   */
  default int onTap(MetalPower power, LivingEntity entity, int level) {
    return level;
  }

  /**
   * Called every tick while this power is active.
   * @param power    Power instance
   * @param entity   Entity receiving the effect
   * @param level    Current level of the power.
   * @return amount of the level to store. Return 0 if nothing happened.
   */
  default int onStore(MetalPower power, LivingEntity entity, int level) {
    return level;
  }

  /**
   * Called when viewing all current effects
   * @param power       Power instance
   * @param entity      Entity receiving the effect
   * @param level       Current level of the power. May be negative to indicate reversed power, but never 0.
   * @param tooltip     Tooltip being built
   */
  void getTooltip(MetalPower power, LivingEntity entity, int level, List<Component> tooltip);
}

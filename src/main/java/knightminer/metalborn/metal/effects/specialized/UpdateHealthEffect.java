package knightminer.metalborn.metal.effects.specialized;

import knightminer.metalborn.metal.MetalPower;
import knightminer.metalborn.metal.effects.MetalEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;

import java.util.List;

/** Effect that ensures health does not exceed max health on change */
public enum UpdateHealthEffect implements MetalEffect {
  INSTANCE;

  public static final RecordLoadable<UpdateHealthEffect> LOADER = new SingletonLoader<>(INSTANCE);

  @Override
  public void onChange(MetalPower power, LivingEntity entity, int level, int previous) {
    float maxHealth = entity.getMaxHealth();
    if (entity.getHealth() > maxHealth) {
      entity.setHealth(maxHealth);
    }
  }

  @Override
  public int onTap(MetalPower power, LivingEntity entity, int level) {
    return 0;
  }

  @Override
  public int onStore(MetalPower power, LivingEntity entity, int level) {
    return 0;
  }

  @Override
  public void getTooltip(MetalPower power, LivingEntity entity, int level, List<Component> tooltip) {}

  @Override
  public RecordLoadable<? extends IHaveLoader> getLoader() {
    return LOADER;
  }
}

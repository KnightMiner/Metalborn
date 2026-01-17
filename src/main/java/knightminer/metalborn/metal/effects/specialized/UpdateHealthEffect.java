package knightminer.metalborn.metal.effects.specialized;

import knightminer.metalborn.core.Registration;
import knightminer.metalborn.metal.MetalPower;
import knightminer.metalborn.metal.effects.MetalEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;
import slimeknights.mantle.util.CombatHelper;

import java.util.List;

/**
 * Effect that ensures health does not exceed max health on change
 * @param amount  If non-zero, health is adjusted by this amount on update.
 */
public record UpdateHealthEffect(int amount) implements MetalEffect {
  public static final RecordLoadable<UpdateHealthEffect> LOADER = RecordLoadable.create(IntLoadable.ANY_BYTE.defaultField("amount", 0, false, UpdateHealthEffect::amount), UpdateHealthEffect::new);

  @Override
  public void onChange(MetalPower power, LivingEntity entity, int level, int previous) {
    float health = entity.getHealth();
    float bonus = 0;
    // if we have an amount, update health before capping
    if (amount != 0) {
      bonus = amount * (level - previous);
    }
    // ensure new health is not greater than max
    float maxHealth = entity.getMaxHealth();
    float newHealth = health + bonus;
    if (newHealth > maxHealth) {
      entity.setHealth(maxHealth);
    } else if (bonus > 0) {
      // if healing, grant immediately
      entity.setHealth(newHealth);
    } else if (bonus < 0) {
      // if harming, apply using a hurt to trigger a death message if it kills you
      entity.hurt(CombatHelper.damageSource(entity.level(), Registration.UPDATE_HEALTH), -bonus);
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

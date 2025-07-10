package knightminer.metalborn.metal.effects;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;

import java.util.List;

/** Effect that adjust fire and freezing on the target */
public record WarmthMetalEffect() implements MetalEffect {
  private static final String KEY_HEAT = Metalborn.key("metal_effect", "warmth.heat");
  private static final String KEY_COLD = Metalborn.key("metal_effect", "warmth.cold");

  public static final RecordLoadable<WarmthMetalEffect> LOADER = new SingletonLoader<>(new WarmthMetalEffect());

  @Override
  public int onTap(MetalPower power, LivingEntity entity, int level) {
    // if currently freezing, reduce freezing rapidly
    if (entity.isFullyFrozen()) {
      entity.setTicksFrozen(entity.getTicksFrozen() - 2 * level);
    }
    else if (entity.isInWater()) {
      if (entity.tickCount % 10 == 0) {
        Level world = entity.level();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        world.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1f, 2.6f + (world.random.nextFloat() - world.random.nextFloat()) * 0.8f);

        // add particles for extinguishing fire
        if (world instanceof ServerLevel server) {
          server.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 8, 0, 0, 0, 0);
        }
      }
    } else {
      // keep adding 1 second of fire every second, but don't
      int fireTicks = entity.getRemainingFireTicks();
      // if currently not on fire, add just enough fire ticks to not take damage at level 1
      if (fireTicks <= 0) {
        entity.setRemainingFireTicks(20 * level - 1);
      }
      // if on fire and its "damage time", increase fire by 1 second per level
      else if (fireTicks % 20 == 0) {
        entity.setRemainingFireTicks(fireTicks + 20 * level);
      }
    }
    return level;
  }

  @Override
  public int onStore(MetalPower power, LivingEntity entity, int level) {
    // if currently on fire, reduce fire rapidly, reduce freezing amount
    if (entity.isOnFire()) {
      // reduce fire in second intervals as fire damage tick is tied to its remaining time
      if (entity.tickCount % 20 == 0) {
        entity.setRemainingFireTicks(entity.getRemainingFireTicks() - 20 * level);
      }
    } else {
      // if we are wearing gear that makes us freeze immune, that removes most of the downside to storing
      if (level > 1 && !entity.canFreeze()) {
        level = 1;
      }
      // at level 1, this will keep you frozen but not taking damage
      // at level 2+, this will slowly build up your frozenness over time
      entity.setTicksFrozen(Math.max(entity.getTicksFrozen(), entity.getTicksRequiredToFreeze()) + level * 2 - 1);
    }
    return level;
  }

  @Override
  public void getTooltip(MetalPower power, LivingEntity entity, int level, List<Component> tooltip) {
    if (level > 0) {
      tooltip.add(Component.translatable(KEY_HEAT, level).withStyle(ChatFormatting.BLUE));
    } else {
      tooltip.add(Component.translatable(KEY_COLD, -level).withStyle(ChatFormatting.RED));
    }
  }

  @Override
  public RecordLoadable<? extends IHaveLoader> getLoader() {
    return LOADER;
  }
}

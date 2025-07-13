package knightminer.metalborn.metal.effects.specialized;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.metal.MetalPower;
import knightminer.metalborn.metal.effects.MetalEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.CombatHelper;
import slimeknights.mantle.util.TranslationHelper;

import java.util.List;

/**
 * Metal effect which heals or damages the target over time
 * @param delay  Time in ticks between each heart healed
 */
public record HealMetalEffect(int delay) implements MetalEffect {
  private static final String KEY_HEAL = Metalborn.key("metal_effect", "health.heal");
  private static final String KEY_HARM = Metalborn.key("metal_effect", "health.harm");
  public static final RecordLoadable<HealMetalEffect> LOADER = RecordLoadable.create(
    IntLoadable.FROM_ONE.requiredField("delay", HealMetalEffect::delay),
    HealMetalEffect::new);

  @Override
  public RecordLoadable<HealMetalEffect> getLoader() {
    return LOADER;
  }

  @Override
  public int onTap(MetalPower power, LivingEntity entity, int level) {
    int frequency = delay / level;
    if (frequency == 0 || entity.tickCount % frequency == 1 && entity.getHealth() < entity.getMaxHealth()) {
      entity.heal(1);
      // on tapping, higher speeds also reduce how much you get
      return level;
    }
    return 0;
  }

  @Override
  public int onStore(MetalPower power, LivingEntity entity, int level) {
    int frequency = delay / level;
    if (frequency == 0 || entity.tickCount % frequency == 1 && entity.getHealth() > 1) {
      if (entity.hurt(CombatHelper.damageSource(entity.level(), Registration.METAL_HURT), 1)) {
        // on storing, you have to actually lose health to gain something
        return 1;
      }
    }
    return 0;
  }

  @Override
  public void getTooltip(MetalPower power, LivingEntity entity, int level, List<Component> tooltip) {
    int frequency = delay / Math.abs(level);
    if (frequency == 0) {
      frequency = 1;
    }
    String value = TranslationHelper.COMMA_FORMAT.format(frequency / 20f);
    if (level > 0) {
      tooltip.add(Component.translatable(KEY_HEAL, value).withStyle(ChatFormatting.BLUE));
    } else {
      tooltip.add(Component.translatable(KEY_HARM, value).withStyle(ChatFormatting.RED));
    }
  }
}

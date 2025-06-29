package knightminer.metalborn.metal.effects;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.List;

/**
 * Metal effect which stores or restores XP
 * @param multiplier  Multiplier on XP amount to store per second
 */
public record ExperienceMetalEffect(int multiplier) implements MetalEffect {
  private static final String KEY_GAIN = Metalborn.key("metal_effect", "xp.gain");
  private static final String KEY_STORE = Metalborn.key("metal_effect", "xp.store");
  public static final RecordLoadable<ExperienceMetalEffect> LOADER = RecordLoadable.create(
    IntLoadable.FROM_ONE.requiredField("multiplier", ExperienceMetalEffect::multiplier),
    ExperienceMetalEffect::new);

  @Override
  public RecordLoadable<ExperienceMetalEffect> getLoader() {
    return LOADER;
  }

  @Override
  public int onTap(MetalPower power, LivingEntity entity, int level) {
    if (entity.tickCount % 20 == 0 && entity instanceof Player player) {
      player.giveExperiencePoints(level * multiplier);
      return level / multiplier;
    }
    return 0;
  }

  @Override
  public int onStore(MetalPower power, LivingEntity entity, int level) {
    if (entity.tickCount % 20 == 0 && entity instanceof Player player) {
      // ensure we don't try to store more XP than the player has
      int take = level * multiplier;
      if (take > player.totalExperience) {
        take = player.totalExperience;
      }
      // update XP
      player.giveExperiencePoints(-take);
      // return how much we actually stored
      return take / multiplier;
    }
    return 0;
  }

  @Override
  public void getTooltip(MetalPower power, LivingEntity entity, int level, List<Component> tooltip) {
    if (level > 0) {
      tooltip.add(Component.translatable(KEY_GAIN, level * multiplier).withStyle(ChatFormatting.BLUE));
    } else {
      tooltip.add(Component.translatable(KEY_STORE, level * -multiplier).withStyle(ChatFormatting.RED));
    }
  }
}

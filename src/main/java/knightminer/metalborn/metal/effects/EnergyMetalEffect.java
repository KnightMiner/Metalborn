package knightminer.metalborn.metal.effects;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.List;

/** Metal effect which restores or exhausts energy. */
public record EnergyMetalEffect(float saturation, float exhaustion) implements MetalEffect {
  private static final String KEY_GAIN = Metalborn.key("metal_effect", "saturation.gain");
  private static final String KEY_STORE = Metalborn.key("metal_effect", "saturation.store");
  public static final RecordLoadable<EnergyMetalEffect> LOADER = RecordLoadable.create(
    FloatLoadable.FROM_ZERO.requiredField("saturation", EnergyMetalEffect::saturation),
    FloatLoadable.FROM_ZERO.requiredField("exhaustion", EnergyMetalEffect::exhaustion),
    EnergyMetalEffect::new);

  @Override
  public RecordLoadable<EnergyMetalEffect> getLoader() {
    return LOADER;
  }

  @Override
  public int onTick(MetalPower power, LivingEntity entity, int level) {
    if (entity.tickCount % 20 == 0 && entity instanceof Player player) {
      FoodData data = player.getFoodData();
      if (level > 0) {
        if (data.getFoodLevel() < 20) {
          data.eat(1, saturation * level);
          return level;
        }
      } else {
        if (data.getFoodLevel() > 0) {
          data.addExhaustion(exhaustion * -level);
          return level;
        }
      }
    }
    return 0;
  }

  @Override
  public void getTooltip(MetalPower power, LivingEntity entity, int level, List<Component> tooltip) {
    if (level > 0) {
      tooltip.add(Component.translatable(KEY_GAIN, level * saturation).withStyle(ChatFormatting.BLUE));
    } else {
      tooltip.add(Component.translatable(KEY_STORE, -level * exhaustion).withStyle(ChatFormatting.RED));
    }
  }
}

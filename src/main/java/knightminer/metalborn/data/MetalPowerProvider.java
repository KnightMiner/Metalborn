package knightminer.metalborn.data;

import knightminer.metalborn.core.Registration;
import knightminer.metalborn.metal.AbstractMetalPowerProvider;
import knightminer.metalborn.metal.effects.AttributeMetalEffect;
import knightminer.metalborn.metal.effects.EnergyMetalEffect;
import knightminer.metalborn.metal.effects.ExperienceMetalEffect;
import knightminer.metalborn.metal.effects.HealMetalEffect;
import knightminer.metalborn.metal.effects.StoringMetalEffect;
import knightminer.metalborn.metal.effects.TappingMetalEffect;
import knightminer.metalborn.metal.effects.UpdateHealthEffect;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.ApiStatus.Internal;

/** Adds powers from metalborn to the mod */
@Internal
public class MetalPowerProvider extends AbstractMetalPowerProvider {
  public MetalPowerProvider(PackOutput output) {
    super(output);
  }

  @Override
  public void addMetals() {
    metal(MetalIds.iron).index(1).temperature(800)
      .hemalurgyCharge(5) // hoglins are a pain to hunt down and fight, double the health bar
      .feruchemy(AttributeMetalEffect.builder(ForgeMod.ENTITY_GRAVITY, Operation.MULTIPLY_TOTAL).swapColors().eachLevel(0.05f))
      .feruchemy(AttributeMetalEffect.builder(Registration.FALL_DISTANCE_MULTIPLIER, Operation.MULTIPLY_TOTAL).swapColors().eachLevel(0.05f))
      .feruchemy(new TappingMetalEffect(AttributeMetalEffect.builder(Attributes.KNOCKBACK_RESISTANCE, Operation.ADDITION).eachLevel(0.05f)))
      .feruchemy(new StoringMetalEffect(AttributeMetalEffect.builder(Registration.KNOCKBACK_MULTIPLIER, Operation.MULTIPLY_TOTAL).swapColors().eachLevel(-0.1f)));
    metal(MetalIds.steel).index(2).temperature(950)
      .feruchemy(AttributeMetalEffect.builder(Attributes.MOVEMENT_SPEED, Operation.MULTIPLY_TOTAL).eachLevel(0.1f))
      .feruchemy(AttributeMetalEffect.builder(Attributes.ATTACK_SPEED, Operation.MULTIPLY_TOTAL).eachLevel(0.05f))
      .feruchemy(AttributeMetalEffect.builder(Registration.MINING_SPEED_MULTIPLIER, Operation.MULTIPLY_TOTAL).eachLevel(0.1f));
    metal(MetalIds.tin).index(3).temperature(225)
      .feruchemy(AttributeMetalEffect.builder(ForgeMod.ENTITY_REACH, Operation.ADDITION).eachLevel(0.25f))
      .feruchemy(AttributeMetalEffect.builder(ForgeMod.BLOCK_REACH, Operation.ADDITION).eachLevel(0.25f));
    metal(MetalIds.pewter).index(4).temperature(400)
      .feruchemy(AttributeMetalEffect.builder(Attributes.ATTACK_DAMAGE, Operation.ADDITION).eachLevel(1))
      .feruchemy(AttributeMetalEffect.builder(Attributes.ARMOR_TOUGHNESS, Operation.ADDITION).eachLevel(0.5f))
      .feruchemy(AttributeMetalEffect.builder(Registration.MINING_SPEED_MULTIPLIER, Operation.MULTIPLY_TOTAL).eachLevel(0.1f));
    metal(MetalIds.copper).index(5).temperature(500)
      .capacity(100) // about 8 levels
      .feruchemy(new ExperienceMetalEffect(1));
    metal(MetalIds.bronze).index(6).temperature(700)
      .capacity(40) // 2 full food bars
      .feruchemy(new EnergyMetalEffect(0.5f, 2f));
    metal(MetalIds.gold).index(7).temperature(700)
      .capacity(40) // 2 full health bars
      .feruchemy(new HealMetalEffect(100));
    metal(MetalIds.roseGold).index(8).temperature(550)
      .feruchemy(AttributeMetalEffect.builder(Attributes.MAX_HEALTH, Operation.ADDITION).eachLevel(2))
      .feruchemy(UpdateHealthEffect.INSTANCE);

    // compat
    // TODO: silver
    // TODO: electrum
    // TODO: zinc
    // TODO: brass
    // tinkers
    // TODO: cobalt
    // TODO: hepatizon
  }

  @Override
  public String getName() {
    return "Metalborn Metal Powers";
  }
}

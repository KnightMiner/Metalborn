package knightminer.metalborn.data;

import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.metalmind.InvestitureMetalmindItem;
import knightminer.metalborn.metal.AbstractMetalPowerProvider;
import knightminer.metalborn.metal.MetalFormat;
import knightminer.metalborn.metal.effects.general.AttributeMetalEffect;
import knightminer.metalborn.metal.effects.general.MobEffectMetalEffect;
import knightminer.metalborn.metal.effects.nesting.CappedMetalEffect;
import knightminer.metalborn.metal.effects.nesting.OffsetMetalEffect;
import knightminer.metalborn.metal.effects.nesting.StoringMetalEffect;
import knightminer.metalborn.metal.effects.nesting.TappingMetalEffect;
import knightminer.metalborn.metal.effects.specialized.EnergyMetalEffect;
import knightminer.metalborn.metal.effects.specialized.ExperienceMetalEffect;
import knightminer.metalborn.metal.effects.specialized.HealMetalEffect;
import knightminer.metalborn.metal.effects.specialized.UpdateHealthEffect;
import knightminer.metalborn.metal.effects.specialized.WarmthMetalEffect;
import net.minecraft.data.PackOutput;
import net.minecraft.world.effect.MobEffects;
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
    metal(MetalIds.tin).index(3).temperature(225).capacity(MetalFormat.SECONDS, 5 * 60)
      .feruchemy(MobEffectMetalEffect.storing(MobEffects.BLINDNESS).flat(1))
      .feruchemy(MobEffectMetalEffect.tapping(MobEffects.NIGHT_VISION).duration(230).flat(1));
    metal(MetalIds.pewter).index(4).temperature(400)
      .feruchemy(AttributeMetalEffect.builder(Attributes.ATTACK_DAMAGE, Operation.ADDITION).eachLevel(1))
      .feruchemy(AttributeMetalEffect.builder(Attributes.MAX_HEALTH, Operation.ADDITION).eachLevel(2))
      .feruchemy(UpdateHealthEffect.INSTANCE);
    metal(MetalIds.copper).index(5).temperature(500).hemalurgyCharge(20)
      .capacity(MetalFormat.METAL, 100) // about 8 levels
      .feruchemy(new ExperienceMetalEffect(1));
    metal(MetalIds.bronze).index(6).temperature(700).hemalurgyCharge(5)
      .feruchemy(AttributeMetalEffect.builder(ForgeMod.ENTITY_REACH, Operation.ADDITION).eachLevel(0.25f))
      .feruchemy(AttributeMetalEffect.builder(ForgeMod.BLOCK_REACH, Operation.ADDITION).eachLevel(0.5f));
    metal(MetalIds.gold).index(7).temperature(700)
      .capacity(MetalFormat.METAL, 40) // 2 full health bars
      .feruchemy(new HealMetalEffect(100));
    metal(MetalIds.roseGold).index(8).temperature(550)
      .capacity(MetalFormat.METAL, 40) // 2 full food bars
      .feruchemy(new EnergyMetalEffect(0.5f, 4f));

    // special - does not use standard metal effects but wants capacity and alike
    metal(InvestitureMetalmindItem.METAL).name("nicrosil").index(16).temperature(1100).hemalurgyCharge(0);

    // compat
    metal(MetalIds.silver).index(9).temperature(790).integration().hemalurgyCharge(10)
      .feruchemy(AttributeMetalEffect.builder(Attributes.LUCK, Operation.ADDITION).eachLevel(0.5f))
      .feruchemy(AttributeMetalEffect.builder(Registration.EXPERIENCE_MULTIPLIER, Operation.MULTIPLY_TOTAL).eachLevel(0.1f))
      .feruchemy(new TappingMetalEffect(AttributeMetalEffect.builder(Registration.LOOTING_BOOST, Operation.ADDITION).eachLevel(0.25f)))
      .feruchemy(new StoringMetalEffect(AttributeMetalEffect.builder(Registration.DROP_CHANCE, Operation.MULTIPLY_TOTAL).eachLevel(0.1f)));
    metal(MetalIds.electrum).index(10).temperature(760).integration()
      .hemalurgyCharge(8) // guardians are tough creatures
      .feruchemy(AttributeMetalEffect.builder(Registration.DETERMINATION, Operation.MULTIPLY_TOTAL).eachLevel(0.1f));

    metal(MetalIds.zinc).index(11).temperature(420).integration().hemalurgyCharge(15)
      // when storing, go from 100% (default) to 200% ... 500%
      .feruchemy(new StoringMetalEffect(new CappedMetalEffect(4, AttributeMetalEffect.builder(Registration.VISIBILITY_MULTIPLIER, Operation.MULTIPLY_TOTAL).swapColors().eachLevel(-1))))
      // when tapping, go from 100% (default) to 75% ... 0%
      .feruchemy(new TappingMetalEffect(new CappedMetalEffect(3, AttributeMetalEffect.builder(Registration.VISIBILITY_MULTIPLIER, Operation.MULTIPLY_TOTAL).swapColors().eachLevel(-0.25f))))
      // when storing, apply glowing at level 4
      .feruchemy(new OffsetMetalEffect(3, MobEffectMetalEffect.storing(MobEffects.GLOWING).flat(1)))
      // when storing, apply invisibility at level 4
      .feruchemy(new OffsetMetalEffect(3, MobEffectMetalEffect.tapping(MobEffects.INVISIBILITY).alwaysStore().flat(1)));
    // nickel is only present if no zinc
    metal(MetalIds.nickel).index(11).temperature(950).alternative("zinc").hemalurgyCharge(15)
      // when storing, go from 100% (default) to 200% ... 500%
      .feruchemy(new StoringMetalEffect(new CappedMetalEffect(4, AttributeMetalEffect.builder(Registration.VISIBILITY_MULTIPLIER, Operation.MULTIPLY_TOTAL).swapColors().eachLevel(-1))))
      // when tapping, go from 100% (default) to 75% ... 0%
      .feruchemy(new TappingMetalEffect(new CappedMetalEffect(3, AttributeMetalEffect.builder(Registration.VISIBILITY_MULTIPLIER, Operation.MULTIPLY_TOTAL).swapColors().eachLevel(-0.25f))))
      // when storing, apply glowing at level 4
      .feruchemy(new OffsetMetalEffect(3, MobEffectMetalEffect.storing(MobEffects.GLOWING).flat(1)))
      // when storing, apply invisibility at level 4
      .feruchemy(new OffsetMetalEffect(3, MobEffectMetalEffect.tapping(MobEffects.INVISIBILITY).alwaysStore().flat(1)));

    metal(MetalIds.brass).index(12).temperature(605).integration()
      .feruchemy(new WarmthMetalEffect())
      .feruchemy(AttributeMetalEffect.builder(Registration.WARMTH, Operation.MULTIPLY_TOTAL).eachLevel(0.1f))
      .feruchemy(new StoringMetalEffect(AttributeMetalEffect.builder(Attributes.ATTACK_DAMAGE, Operation.ADDITION).eachLevel(1)))
      .feruchemy(new TappingMetalEffect(AttributeMetalEffect.builder(Registration.HEAT_DAMAGE, Operation.ADDITION).eachLevel(1)));
    // constantan is only present if no brass
    metal(MetalIds.constantan).index(12).temperature(920).alternative("brass")
      .feruchemy(new WarmthMetalEffect())
      .feruchemy(AttributeMetalEffect.builder(Registration.WARMTH, Operation.MULTIPLY_TOTAL).eachLevel(0.1f))
      .feruchemy(new StoringMetalEffect(AttributeMetalEffect.builder(Attributes.ATTACK_DAMAGE, Operation.ADDITION).eachLevel(1)))
      .feruchemy(new TappingMetalEffect(AttributeMetalEffect.builder(Registration.HEAT_DAMAGE, Operation.ADDITION).eachLevel(1)));
  }

  @Override
  public String getName() {
    return "Metalborn Metal Powers";
  }
}

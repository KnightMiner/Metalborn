package knightminer.metalborn.data;

import knightminer.metalborn.metal.AbstractMetalPowerProvider;
import knightminer.metalborn.metal.effects.AttributeMetalEffect;
import knightminer.metalborn.metal.effects.ExperienceMetalEffect;
import knightminer.metalborn.metal.effects.HealMetalEffect;
import knightminer.metalborn.metal.effects.RangeMetalEffect;
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
    metal(MetalIds.iron).index(1)
      .feruchemy(AttributeMetalEffect.builder(ForgeMod.ENTITY_GRAVITY.get(), Operation.MULTIPLY_TOTAL).eachLevel(0.05f))
      .feruchemy(RangeMetalEffect.tapping(AttributeMetalEffect.builder(Attributes.KNOCKBACK_RESISTANCE, Operation.ADDITION).eachLevel(0.05f)));
    // TODO: knockback multiplier when storing
    // TODO: fall damage adjustment
    metal(MetalIds.steel).index(2)
      .feruchemy(AttributeMetalEffect.builder(Attributes.MOVEMENT_SPEED, Operation.MULTIPLY_TOTAL).eachLevel(0.1f))
      .feruchemy(AttributeMetalEffect.builder(Attributes.ATTACK_SPEED, Operation.MULTIPLY_TOTAL).eachLevel(0.05f));
    // TODO: mining speed
    metal(MetalIds.tin).index(3);
    // TODO: tin effects
    metal(MetalIds.pewter).index(4)
      .feruchemy(AttributeMetalEffect.builder(Attributes.ATTACK_DAMAGE, Operation.ADDITION).eachLevel(1))
      .feruchemy(AttributeMetalEffect.builder(Attributes.ARMOR_TOUGHNESS, Operation.ADDITION).eachLevel(0.5f));
    // TODO: jump height?
    metal(MetalIds.copper).index(5)
      .capacity(100) // about 8 levels
      .feruchemy(new ExperienceMetalEffect(1));
    // TODO: hemalurgy
    metal(MetalIds.bronze).index(6);
    // TODO: bronze
    metal(MetalIds.gold).index(7)
      .capacity(40) // 2 full health bars
      .feruchemy(new HealMetalEffect(100));
    // TODO: gold hemalurgy
    metal(MetalIds.roseGold).index(8);
    // TODO: rose gold

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

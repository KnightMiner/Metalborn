package knightminer.metalborn.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import knightminer.metalborn.core.Registration;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import slimeknights.mantle.loot.AbstractLootModifierBuilder.GenericLootModifierBuilder;

/** Loot modifier that applies {@link Registration#DROP_CHANCE} to loot. */
public class ApplyDropChanceLootModifier extends LootModifier {
  public static final Codec<ApplyDropChanceLootModifier> CODEC = RecordCodecBuilder.create(instance -> codecStart(instance).apply(instance, ApplyDropChanceLootModifier::new));

  protected ApplyDropChanceLootModifier(LootItemCondition[] conditions) {
    super(conditions);
  }

  /** Creates a new builder instance */
  public static GenericLootModifierBuilder<ApplyDropChanceLootModifier> builder() {
    return new GenericLootModifierBuilder<>(ApplyDropChanceLootModifier::new);
  }

  @Override
  public Codec<? extends IGlobalLootModifier> codec() {
    return CODEC;
  }

  /** Gets the chance for the given loot parameter */
  private static double getChance(LootContext context, LootContextParam<Entity> param) {
    Entity entity = context.getParamOrNull(param);
    if (entity instanceof LivingEntity living) {
      return living.getAttributeValue(Registration.DROP_CHANCE.get());
    }
    return 1;
  }

  @Override
  protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
    // some loot tables use this and some use killer for the player, so just check both
    // as a bonus, means monsters could get this to reduce their drop rates
    double chance = getChance(context, LootContextParams.THIS_ENTITY) * getChance(context, LootContextParams.KILLER_ENTITY);
    if (chance <= 0) {
      generatedLoot.clear();
    } else if (chance < 1) {
      RandomSource random = context.getRandom();
      generatedLoot.removeIf(stack -> random.nextDouble() >= chance);
    }
    return generatedLoot;
  }
}

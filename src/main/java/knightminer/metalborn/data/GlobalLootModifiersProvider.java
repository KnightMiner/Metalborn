package knightminer.metalborn.data;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.loot.ApplyDropChanceLootModifier;
import knightminer.metalborn.loot.HasLootContextSetCondition;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

/** Provider for custom global loot modifiers */
public class GlobalLootModifiersProvider extends GlobalLootModifierProvider {
  public GlobalLootModifiersProvider(PackOutput output) {
    super(output, Metalborn.MOD_ID);
  }

  @Override
  protected void start() {
    add("apply_drop_chance", ApplyDropChanceLootModifier.builder()
      .addCondition(AnyOfCondition.anyOf(
        HasLootContextSetCondition.builder(LootContextParamSets.BLOCK),
        HasLootContextSetCondition.builder(LootContextParamSets.ENTITY)
      ).build()).build());
  }
}

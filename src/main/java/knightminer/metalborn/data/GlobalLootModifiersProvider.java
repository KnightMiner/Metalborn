package knightminer.metalborn.data;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.json.loot.ApplyDropChanceLootModifier;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import slimeknights.mantle.loot.condition.HasLootContextSetCondition;

import java.util.concurrent.CompletableFuture;

/** Provider for custom global loot modifiers */
public class GlobalLootModifiersProvider extends GlobalLootModifierProvider {
  public GlobalLootModifiersProvider(PackOutput output, CompletableFuture<Provider> registries) {
    super(output, registries, Metalborn.MOD_ID);
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

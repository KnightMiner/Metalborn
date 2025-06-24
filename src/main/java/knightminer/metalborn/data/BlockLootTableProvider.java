package knightminer.metalborn.data;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/** Adds all relevant loot tables for the mod */
public class BlockLootTableProvider extends BlockLootSubProvider {
  public BlockLootTableProvider() {
    super(Set.of(), FeatureFlags.REGISTRY.allFlags());
  }

  @SuppressWarnings("deprecation")
  @Override
  protected Iterable<Block> getKnownBlocks() {
    return BuiltInRegistries.BLOCK.entrySet().stream()
      .filter(entry -> Metalborn.MOD_ID.equals(entry.getKey().location().getNamespace()))
      .map(Entry::getValue)
      .collect(Collectors.toList());
  }

  @Override
  protected void generate() {
    dropSelf(Registration.TIN.get());
    dropSelf(Registration.PEWTER.get());
    dropSelf(Registration.STEEL.get());
    dropSelf(Registration.BRONZE.get());
    dropSelf(Registration.ROSE_GOLD.get());
  }
}

package knightminer.metalborn.metal;

import knightminer.metalborn.Metalborn;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import slimeknights.mantle.data.GenericDataProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Data provider for adding new metals */
public abstract class AbstractMetalPowerProvider extends GenericDataProvider {
  /** Map of all metals being built */
  private final Map<MetalId,MetalPowerBuilder> metals = new HashMap<>();

  public AbstractMetalPowerProvider(PackOutput output) {
    super(output, Target.DATA_PACK, MetalManager.FOLDER);
  }

  /** Adds all relevant metals */
  public abstract void addMetals();

  /** Creates a new builder for the given metal */
  public MetalPowerBuilder metal(MetalId metalId) {
    return metals.computeIfAbsent(metalId, MetalPowerBuilder::builder);
  }

  @Override
  public CompletableFuture<?> run(CachedOutput cache) {
    addMetals();
    return allOf(metals.entrySet().stream().map(entry -> {
      try {
        return saveJson(cache, entry.getKey(), entry.getValue().serialize());
      } catch (Exception e) {
        Metalborn.LOG.error("Failed to serialize metal {}", entry.getKey(), e);
        throw e;
      }
    }));
  }
}

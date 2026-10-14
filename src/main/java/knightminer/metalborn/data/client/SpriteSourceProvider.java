package knightminer.metalborn.data.client;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.client.model.ExtendablePalettedPermutations;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static knightminer.metalborn.Metalborn.resource;

/** Adds any relevant sprite source providers, such as the metal texture generator */
public class SpriteSourceProvider extends net.neoforged.neoforge.common.data.SpriteSourceProvider {
  public SpriteSourceProvider(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper fileHelper) {
    super(output, lookupProvider, Metalborn.MOD_ID, fileHelper);
  }

  @Override
  protected void gather() {
    atlas(BLOCKS_ATLAS)
      .addSource(new ExtendablePalettedPermutations(
        List.of(
          resource("metal/item/bracer"),
          resource("metal/item/ring"),
          resource("metal/item/spike")
        ),
        resource("metal/palettes"),
        ItemModelProvider.FERUCHEMY_METALS
      ))
      .addSource(new ExtendablePalettedPermutations(
        List.of(resource("metal/item/nugget")),
        resource("metal/palettes"),
        Metalborn.resource("metals/nuggets")
      ))
      .addSource(new ExtendablePalettedPermutations(
        List.of(resource("metal/item/ingot")),
        resource("metal/palettes"),
        Metalborn.resource("metals/ingots")
      ));
  }
}

package knightminer.metalborn.data.client;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.client.ExtendablePalettedPermutations;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.List;

import static knightminer.metalborn.Metalborn.resource;

/** Adds any relevant sprite source providers, such as the metal texture generator */
public class SpriteSourceProvider extends net.minecraftforge.common.data.SpriteSourceProvider {
  public SpriteSourceProvider(PackOutput output, ExistingFileHelper fileHelper) {
    super(output, fileHelper, Metalborn.MOD_ID);
  }

  @Override
  protected void addSources() {
    atlas(BLOCKS_ATLAS)
      .addSource(new ExtendablePalettedPermutations(
        List.of(
          resource("metal/item/nugget_overlay"),
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

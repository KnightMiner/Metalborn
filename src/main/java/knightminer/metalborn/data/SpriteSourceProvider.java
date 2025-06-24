package knightminer.metalborn.data;

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
          resource("item/materials/lerasium_nugget_overlay"),
          resource("item/metalmind/bracer"),
          resource("item/metalmind/ring")
        ),
        resource("metal_palettes"),
        ItemModelProvider.FERUCHEMY_METALS
      ));
  }
}

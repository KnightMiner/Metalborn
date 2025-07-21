package knightminer.metalborn.client.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import knightminer.metalborn.client.MetalbornClient;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.HashMap;
import java.util.List;

/** Extension of {@link PalettedPermutations} to allow pulling the palette list from a mergable resource, making addons more practical. */
public class ExtendablePalettedPermutations extends PalettedPermutations {
  public static final Codec<ExtendablePalettedPermutations> CODEC = RecordCodecBuilder.create(inst -> inst.group(
    Codec.list(ResourceLocation.CODEC).fieldOf("textures").forGetter(palette -> palette.textures),
    ResourceLocation.CODEC.fieldOf("palette_folder").forGetter(palette -> palette.paletteFolder),
    ResourceLocation.CODEC.fieldOf("permutation_list").forGetter(palette -> palette.permutationList)
  ).apply(inst, ExtendablePalettedPermutations::new));

  private final ResourceLocation paletteFolder;
  private final ResourceLocation permutationList;
  public ExtendablePalettedPermutations(List<ResourceLocation> textures, ResourceLocation paletteFolder, ResourceLocation permutationList) {
    super(textures, paletteFolder.withSuffix("/base"), new HashMap<>());
    this.paletteFolder = paletteFolder;
    this.permutationList = permutationList;
  }

  @Override
  public SpriteSourceType type() {
    return MetalbornClient.EXTENDABLE_PALETTE;
  }

  @Override
  public void run(ResourceManager manager, Output output) {
    // start by locating all variants from the JSON
    List<ResourceLocation> values = PaletteListManager.INSTANCE.getPermutationList(manager, permutationList);
    // TODO: consider caching above so we don't need to load it multiple times

    // now we have a list of variants, construct the map
    permutations.clear();
    for (ResourceLocation value : values) {
      String asString = PalettedItemModel.toSuffix(value);
      permutations.put(asString, paletteFolder.withSuffix('/' + asString));
    }

    // run base class logic for the rest
    super.run(manager, output);
  }
}

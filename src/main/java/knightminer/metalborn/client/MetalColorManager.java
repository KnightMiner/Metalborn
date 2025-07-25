package knightminer.metalborn.client;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import knightminer.metalborn.Metalborn;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import slimeknights.mantle.data.listener.ISafeManagerReloadListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

/** Handles reading in metal colors from the palette list */
public class MetalColorManager implements ISafeManagerReloadListener {
  private static final FileToIdConverter CONVERTER = new FileToIdConverter("textures/metal/palettes", ".png");
  public static final MetalColorManager INSTANCE = new MetalColorManager();

  /** Loaded mapping of colors */
  private Object2IntMap<ResourceLocation> colors = Object2IntMaps.emptyMap();

  private MetalColorManager() {}

  @Override
  public void onReloadSafe(ResourceManager manager) {
    Object2IntMap<ResourceLocation> colors = new Object2IntOpenHashMap<>();
    for (Entry<ResourceLocation, Resource> entry : CONVERTER.listMatchingResources(manager).entrySet()) {
      ResourceLocation path = entry.getKey();
      if (Metalborn.MOD_ID.equals(path.getNamespace())) {
        String id = CONVERTER.fileToId(path).getPath();
        int index = id.indexOf('_');
        if (index != -1) {
          ResourceLocation metal = new ResourceLocation(id.substring(0, index), id.substring(index + 1));
          try (
            InputStream input = entry.getValue().open();
            NativeImage image = NativeImage.read(input);
          ) {
            colors.put(metal, translateColorBGR(image.getPixelRGBA(2, 0)));
          } catch (IOException e) {
            Metalborn.LOG.error("Failed to fetch color from metal palette {} at {}", metal, path, e);
          }
        }
      }
    }
    this.colors = colors;
  }

  /** Gets the color for the given metal */
  public int getColor(ResourceLocation metal, int defaultColor) {
    return colors.getOrDefault(metal, defaultColor);
  }

  /** Converts an ARGB color to a ABGR color or vice versa */
  private static int translateColorBGR(int color) {
    return (color & 0xFF00FF00) | (((color & 0x00FF0000) >> 16) & 0x000000FF) | (((color & 0x000000FF) << 16) & 0x00FF0000);
  }
}

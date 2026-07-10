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
  private Object2IntMap<String> colors = Object2IntMaps.emptyMap();

  private MetalColorManager() {}

  @Override
  public void onReloadSafe(ResourceManager manager) {
    Object2IntMap<String> colors = new Object2IntOpenHashMap<>();
    for (Entry<ResourceLocation, Resource> entry : CONVERTER.listMatchingResources(manager).entrySet()) {
      ResourceLocation path = entry.getKey();
      if (Metalborn.MOD_ID.equals(path.getNamespace())) {
        String id = CONVERTER.fileToId(path).getPath();
        // don't care about palettes that lack underscore, means they are a fallback
        if (id.indexOf('_') != -1) {
          try (
            InputStream input = entry.getValue().open();
            NativeImage image = NativeImage.read(input);
          ) {
            colors.put(id, translateColorBGR(image.getPixelRGBA(2, 0)));
          } catch (IOException e) {
            Metalborn.LOG.error("Failed to fetch color from metal palette {} at {}", id, path, e);
          }
        }
      }
    }
    this.colors = colors;
  }

  /** Gets the color for the given metal */
  public int getColor(ResourceLocation metal, int defaultColor) {
    String id = metal.getNamespace() + '_' + metal.getPath();
    return colors.getOrDefault(id, defaultColor);
  }

  /** Converts an ARGB color to a ABGR color or vice versa */
  private static int translateColorBGR(int color) {
    return (color & 0xFF00FF00) | (((color & 0x00FF0000) >> 16) & 0x000000FF) | (((color & 0x000000FF) << 16) & 0x00FF0000);
  }
}

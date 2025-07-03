package knightminer.metalborn.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import knightminer.metalborn.Metalborn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.listener.ISafeManagerReloadListener;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;

import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Manager for permutation lists, used in {@link ExtendablePalettedPermutations} and {@link PalettedItemModel} */
public class PaletteListManager implements ISafeManagerReloadListener {
  /** Loadable for the list of resource locations in the tag file */
  private static final Loadable<List<ResourceLocation>> RESOURCE_LOCATION_LIST = Loadables.RESOURCE_LOCATION.list(0);
  /** Singleton instance of this loader */
  public static final PaletteListManager INSTANCE = new PaletteListManager();

  private final Map<ResourceLocation, List<ResourceLocation>> cache = new HashMap<>();

  private PaletteListManager() {}

  @Override
  public void onReloadSafe(ResourceManager manager) {
    cache.clear();
  }

  /** Gets a permutation list from the cache, loading it if missing */
  public List<ResourceLocation> getPermutationList(ResourceManager manager, ResourceLocation location) {
    List<ResourceLocation> list = cache.get(location);
    if (list == null) {
      // linked hash set ensures uniqueness while preserving order
      Set<ResourceLocation> values = new LinkedHashSet<>();
      // read from each resource pack, this is loosely based on tag loading
      for (Resource resource : manager.getResourceStack(location.withSuffix(".json"))) {
        try (Reader reader = resource.openAsReader()) {
          // fetch root json
          JsonObject json = GsonHelper.convertToJsonObject(JsonParser.parseReader(reader), "palettes");
          // if replace is set, clear lower entries
          if (GsonHelper.getAsBoolean(json, "replace", false)) {
            values.clear();
          }
          // add all new entries
          values.addAll(RESOURCE_LOCATION_LIST.getIfPresent(json, "values"));
        } catch (Exception ex) {
          Metalborn.LOG.error("Couldn't parse palette list {} in resource pack {}", location, resource.sourcePackId(), ex);
        }
      }
      list = List.copyOf(values);
      cache.put(location, list);
    }
    return list;
  }
}

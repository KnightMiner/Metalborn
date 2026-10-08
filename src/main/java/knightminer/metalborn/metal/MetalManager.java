package knightminer.metalborn.metal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import knightminer.metalborn.Metalborn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.network.PacketHelper;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * Handles loading {@link MetalPower} from datapacks.
 */
public class MetalManager extends SimpleJsonResourceReloadListener {
  /** Location of dynamic modifiers */
  public static final String FOLDER = "metalborn/effects";
  /** Singleton instance of the manager */
  public static final MetalManager INSTANCE = new MetalManager();
  /** Loadable for the list of fallback IDs to use if a power's conditions fail. */
  public static final Loadable<List<MetalId>> FALLBACK_LOADABLE = MetalId.LOADABLE.list(ArrayLoadable.COMPACT_OR_EMPTY);

  /** Currently loaded map of all powers */
  private Map<MetalId,MetalPower> powers = Map.of();
  /** All powers in sorted order for display */
  private List<MetalPower> sortedPowers = List.of();
  /** All powers that can be used by a ferring */
  private List<MetalPower> ferrings = List.of();
  /** Mapping from unused metal IDs to existing powers, for migrating power list changes. */
  private Map<MetalId, MetalPower> redirects = Map.of();
  /** Map of {@link MetalHolder} used on items */
  private final Map<MetalId,MetalHolder> holders = new HashMap<>();

  /** Cache of the metal power for each item type */
  private final Map<Item,MetalPower> itemCache = new HashMap<>();
  /** Cache resolver for getting a power from an item */
  private final Function<Item,MetalPower> itemGetter = item -> {
    for (MetalPower power : sortedPowers) {
      if (power.matches(item)) {
        return power;
      }
    }
    return MetalPower.DEFAULT;
  };

  /** Cache of the metal power for each item type */
  private final Map<EntityType<?>,MetalPower> entityCache = new HashMap<>();
  /** Cache resolver for getting a power from an item */
  private final Function<EntityType<?>,MetalPower> entityGetter = entity -> {
    for (MetalPower power : sortedPowers) {
      if (power.matches(entity)) {
        return power;
      }
    }
    return MetalPower.DEFAULT;
  };

  /** Cache of the metal power for each fluid type, for Tinkers' Construct compat */
  private final Map<Fluid,MetalPower> fluidCache = new HashMap<>();
  /** Cache resolver for getting a power from an item */
  private final Function<Fluid,MetalPower> fluidGetter = fluid -> {
    for (MetalPower power : sortedPowers) {
      if (power.temperature() > 0 && power.matches(fluid)) {
        return power;
      }
    }
    return MetalPower.DEFAULT;
  };


  private MetalManager() {
    super(JsonHelper.DEFAULT_GSON, FOLDER);
  }


  /* Network and sync */

  @Internal
  public void init() {
    NeoForge.EVENT_BUS.addListener(AddReloadListenerEvent.class, this::addDataPackListeners);
    NeoForge.EVENT_BUS.addListener(OnDatapackSyncEvent.class, e -> PacketHelper.syncPackets(e, new UpdateMetalPowerPacket(this.powers, this.redirects)));
  }

  /** Adds the managers as datapack listeners */
  private void addDataPackListeners(final AddReloadListenerEvent event) {
    event.addListener(this);
  }

  /** Updates the list of metal powers from the given map */
  @Internal
  void updateMetalPowers(Map<MetalId,MetalPower> powers, Map<MetalId,MetalPower> redirects) {
    this.powers = powers;
    this.redirects = redirects;
    this.sortedPowers = powers.values().stream().sorted(Comparator.comparing(MetalPower::index).thenComparing(MetalPower::id)).toList();
    this.ferrings = sortedPowers.stream().filter(power -> power.ferring() && !power.feruchemy().isEmpty()).toList();
    this.itemCache.clear();
    this.entityCache.clear();
    this.holders.values().forEach(MetalHolder::clearCache);
  }


  /* JSON parsing */

  /** Creates context for modifier parsing */
  public static TypedMap createContext(ResourceLocation id) {
    return TypedMapBuilder.builder().put(ContextKey.ID, id).put(ContextKey.DEBUG, "Metal " + id).build();
  }

  @Override
  protected void apply(Map<ResourceLocation, JsonElement> splashList, ResourceManager manager, ProfilerFiller profiler) {
    long time = System.nanoTime();

    Map<MetalId,MetalPower> powers = new HashMap<>();
    Map<MetalId,List<MetalId>> potentialRedirects = new HashMap<>();
    for (Entry<ResourceLocation,JsonElement> entry : splashList.entrySet()) {
      JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), "metal");
      // ensure load conditions pass
      try {
        if (JsonHelper.processConditions(json, "conditions", getContext())) {
          // load the power
          MetalPower power = MetalPower.LOADABLE.deserialize(json, createContext(entry.getKey()));
          // store it into the map
          powers.put(power.id(), power);
        } else {
          // if the condition disables it, load in fallback options, though wait to process as we have not yet loaded all powers
          List<MetalId> fallback = FALLBACK_LOADABLE.getOrDefault(json, "fallback", List.of());
          if (!fallback.isEmpty()) {
            potentialRedirects.put(new MetalId(entry.getKey()), fallback);
          }
        }
      } catch (Exception e) {
        Metalborn.LOG.error("Failed to load metal {}", entry.getKey(), e);
      }
    }
    // process redirects
    Map<MetalId,MetalPower> redirects = new HashMap<>();
    // check if any of the fallback options exists
    // no worry of circular redirect here as we do not recursively resolve redirects
    for (Map.Entry<MetalId,List<MetalId>> entry : potentialRedirects.entrySet()) {
      for (MetalId potential : entry.getValue()) {
        MetalPower power = powers.get(potential);
        if (power != null) {
          redirects.put(entry.getKey(), power);
          break;
        }
      }
    }
    // update the stored data structures
    updateMetalPowers(Map.copyOf(powers), Map.copyOf(redirects));
    Metalborn.LOG.info("Loaded {} metal powers with {} redirects in {} ms", powers.size(), redirects.size(), (System.nanoTime() - time) / 1_000_000f);
  }


  /* Querying */

  /** Gets the power with the given ID, following redirects, returning {@link MetalPower#DEFAULT} if missing. */
  public MetalPower get(MetalId id) {
    MetalPower power = powers.get(id);
    if (power == null) {
      power = redirects.getOrDefault(id, MetalPower.DEFAULT);
    }
    return power;
  }

  /** Resolves the given redirect, returning null if its not redirected. */
  @Nullable
  public MetalPower resolve(MetalId id) {
    return redirects.get(id);
  }

  /** Gets the power with the given ID, bypassing redirects, returning {@code null} if missing. Useful for checking if an ID is registered. */
  @Nullable
  public MetalPower getDirect(MetalId id) {
    return powers.getOrDefault(id, MetalPower.DEFAULT);
  }

  /** Gets a metal holder for the given ID, which lazily fetches a metal power and refreshes on reload. */
  public MetalHolder getHolder(MetalId id) {
    if (MetalId.NONE.equals(id)) {
      return MetalHolder.EMPTY;
    }
    return holders.computeIfAbsent(id, MetalHolder::new);
  }

  /** Gets a list of all powers in sorted order */
  public List<MetalPower> getSortedPowers() {
    return sortedPowers;
  }

  /** Gets a list of all ferrings */
  public List<MetalPower> getFerrings() {
    return ferrings;
  }

  /** Gets a random ferring from all metals available to ferrings by default */
  public MetalPower getRandomFerring(RandomSource random) {
    List<MetalPower> metals = getFerrings();
    if (metals.isEmpty()) {
      return MetalPower.DEFAULT;
    } else {
      return metals.get(random.nextInt(metals.size()));
    }
  }

  /** {@return metal power for the given ingot or nugget} */
  public MetalPower fromIngotOrNugget(ItemLike item) {
    return itemCache.computeIfAbsent(item.asItem(), itemGetter);
  }

  /** {@return metal power for the given fluid} */
  public MetalPower fromFluid(Fluid fluid) {
    return fluidCache.computeIfAbsent(fluid, fluidGetter);
  }

  /** {@return metal power for the given entity target} */
  public MetalPower fromTarget(EntityType<?> type) {
    return entityCache.computeIfAbsent(type, entityGetter);
  }
}

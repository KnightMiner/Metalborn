package knightminer.metalborn.metal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.network.MetalbornNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MetalManager extends SimpleJsonResourceReloadListener {
  /** Location of dynamic modifiers */
  public static final String FOLDER = "metalborn/effects";
  /** Singleton instance of the manager */
  public static final MetalManager INSTANCE = new MetalManager();

  /** Currently loaded map of all powers */
  private Map<MetalId,MetalPower> powers = Map.of();
  /** All powers in sorted order for display */
  private List<MetalPower> sortedPowers = List.of();
  /** All powers that can be used by a ferring */
  private List<MetalPower> ferrings = List.of();

  /** Condition context for loading */
  private IContext conditionContext = IContext.EMPTY;

  private MetalManager() {
    super(JsonHelper.DEFAULT_GSON, FOLDER);
  }


  /* Network and sync */

  @Internal
  public void init() {
    MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, AddReloadListenerEvent.class, this::addDataPackListeners);
    MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, OnDatapackSyncEvent.class, e -> JsonHelper.syncPackets(e, MetalbornNetwork.getInstance(), new UpdateMetalPowerPacket(this.powers)));
  }

  /** Adds the managers as datapack listeners */
  private void addDataPackListeners(final AddReloadListenerEvent event) {
    event.addListener(this);
    conditionContext = event.getConditionContext();
  }

  /** Updates the list of metal powers from the given map */
  void updateMetalPowers(Map<MetalId,MetalPower> powers) {
    this.powers = powers;
    this.sortedPowers = powers.values().stream().sorted(Comparator.comparing(MetalPower::index)).toList();
    this.ferrings = sortedPowers.stream().filter(power -> power.ferring() && !power.feruchemy().isEmpty()).toList();
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
    for (Entry<ResourceLocation,JsonElement> entry : splashList.entrySet()) {
      JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), "metal");
      // ensure load conditions pass
      try {
        if (CraftingHelper.processConditions(json, "conditions", conditionContext)) {
          // load the power
          MetalPower power = MetalPower.LOADABLE.deserialize(json, createContext(entry.getKey()));
          // store it into the map
          powers.put(power.id(), power);
        }
      } catch (Exception e) {
        Metalborn.LOG.error("Failed to load metal {}", entry.getKey(), e);
      }
    }
    // update the powers
    updateMetalPowers(Map.copyOf(powers));
    Metalborn.LOG.info("Loaded {} metal powers in {} ms", powers.size(), (System.nanoTime() - time) / 1_000_000f);
  }


  /* Querying */

  /** Gets the power with the given ID */
  public MetalPower get(MetalId id) {
    return powers.getOrDefault(id, MetalPower.DEFAULT);
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

  // TODO: lookup by ingot
  // TODO: lookup by nugget
  // TODO: lookup by entity
}

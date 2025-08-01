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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
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
import java.util.function.Function;

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
    this.itemCache.clear();
    this.entityCache.clear();
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

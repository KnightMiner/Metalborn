package knightminer.metalborn.core;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.network.MetalbornNetwork;
import knightminer.metalborn.network.SyncMetalbornDataPacket;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.Nullable;

/** Capability containing all data related to metalborn status. Includes ferring type, tapping/storing, metalminds, spikes, and alike. */
public class MetalbornCapability implements ICapabilitySerializable<CompoundTag>, MetalbornData, Runnable {
  /** Capability ID */
  private static final ResourceLocation ID = Metalborn.resource("metal");
  /** Capability token */
  public static final Capability<MetalbornData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

  /* Fields */
  /** Current capability instance for listeners */
  private LazyOptional<MetalbornData> capability = LazyOptional.of(() -> this);

  /** Player with this metal */
  private final Player player;
  /** Type of ferring for the player */
  @Nullable
  private MetalId ferringType;

  /** Handler for effects on active metalminds */
  private final ActiveMetalminds activeMetalminds;
  /** Inventory of all metalminds */
  private final MetalmindInventory metalminds;

  private MetalbornCapability(Player player) {
    this.player = player;
    this.activeMetalminds = new ActiveMetalminds(player);
    this.metalminds = new MetalmindInventory(activeMetalminds, player);
  }


  /* Metal ability */

  @Override
  public boolean canUse(MetalId metal) {
    return getFerringType().equals(metal);
  }

  @Override
  public void setFerringType(MetalId metalId) {
    // TODO: only run this if the new metal is not available elsehwere
    if (ferringType != null && !ferringType.equals(metalId)) {
      activeMetalminds.clearMetal(ferringType);
    }
    ferringType = metalId;
    // TODO: sync selection?
  }

  @Override
  public MetalId getFerringType() {
    if (ferringType == null) {
      ferringType = MetalManager.INSTANCE.getRandomFerring(player.getRandom()).id();
      // TODO: sync selection?
    }
    return ferringType;
  }

  @Override
  public void tick() {
    activeMetalminds.tick();
  }


  /* Capability logic */

  @Override
  public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
    return CAPABILITY.orEmpty(cap, capability);
  }

  @Override
  public void copyFrom(MetalbornData data, boolean wasDeath) {
    // the only reason this should fail is if the original player had no data
    if (data instanceof MetalbornCapability other) {
      if (!wasDeath) {
        this.ferringType = other.ferringType;
      }
      this.metalminds.copyFrom(other.metalminds);
    }
  }

  @Override
  public void run() {
    // called when capabilities invalidate, create a new cap just in case they are revived later
    capability.invalidate();
    capability = LazyOptional.of(() -> this);
  }


  /* NBT */

  private static final String FERRING_TYPE = "ferring_type";
  private static final String METALMINDS = "metalminds";

  @Override
  public CompoundTag serializeNBT() {
    CompoundTag tag = new CompoundTag();
    if (ferringType != null) {
      tag.putString(FERRING_TYPE, ferringType.toString());
    }
    tag.put(METALMINDS, metalminds.serializeNBT());
    return tag;
  }

  @Override
  public void deserializeNBT(CompoundTag nbt) {
    if (nbt.contains(FERRING_TYPE, Tag.TAG_STRING)) {
      this.ferringType = MetalId.tryParse(nbt.getString(FERRING_TYPE));
    }
    if (nbt.contains(METALMINDS, Tag.TAG_LIST)) {
      this.metalminds.deserializeNBT(nbt.getList(METALMINDS, Tag.TAG_COMPOUND));
      this.metalminds.refreshActive();
    }
  }


  /* Capability registration */

  /** Registers this capability with any relevant events */
  public static void register() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.NORMAL, false, RegisterCapabilitiesEvent.class, MetalbornCapability::register);
    MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, MetalbornCapability::attachCapability);
    MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.Clone.class, MetalbornCapability::playerClone);
    //MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerRespawnEvent.class, event -> sync(event.getEntity()));
    //MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerChangedDimensionEvent.class, event -> sync(event.getEntity()));
    //MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerLoggedInEvent.class, event -> sync(event.getEntity()));
  }


  /** Registers the capability with the event bus */
  private static void register(RegisterCapabilitiesEvent event) {
    event.register(MetalbornData.class);
  }

  /** Event listener to attach the capability */
  private static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
    Entity entity = event.getObject();
    // must be on living entities as we use this for potions, but also support anything else with modifiers, this is their data
    if (entity instanceof Player player) {
      MetalbornCapability provider = new MetalbornCapability(player);
      event.addCapability(ID, provider);
      event.addListener(provider);
    }
  }

  /** Gets the data for the given player */
  public static MetalbornData getData(LivingEntity player) {
    return player.getCapability(CAPABILITY).orElse(MetalbornData.EMPTY);
  }

  /** Syncs the data to the given player */
  private static void sync(Player player) {
    MetalbornNetwork.getInstance().sendTo(new SyncMetalbornDataPacket(getData(player)), player);
  }

  /** copy caps when the player respawns/returns from the end */
  private static void playerClone(PlayerEvent.Clone event) {
    getData(event.getEntity()).copyFrom(getData(event.getOriginal()), event.isWasDeath());
  }
}

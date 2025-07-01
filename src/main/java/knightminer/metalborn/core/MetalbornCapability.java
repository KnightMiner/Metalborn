package knightminer.metalborn.core;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.inventory.ActiveMetalminds;
import knightminer.metalborn.core.inventory.MetalmindInventory;
import knightminer.metalborn.core.inventory.SpikeInventory;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

import java.util.Collection;
import java.util.List;

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
  /** Inventory of all spikes */
  private final SpikeInventory spikes;

  private MetalbornCapability(Player player) {
    this.player = player;
    this.activeMetalminds = new ActiveMetalminds(player);
    this.metalminds = new MetalmindInventory(this, activeMetalminds, player);
    this.spikes = new SpikeInventory(activeMetalminds, player);
  }

  /** Gets the metalmind inventory */
  public MetalmindInventory getMetalminds() {
    return metalminds;
  }

  /** Gets the metalmind inventory */
  public SpikeInventory getSpikes() {
    return spikes;
  }


  /* Metal ability */

  @Override
  public boolean canUse(MetalId metal) {
    return getFerringType().equals(metal) || spikes.canUse(metal);
  }

  @Override
  public void setFerringType(MetalId metalId) {
    if (ferringType != null && !ferringType.equals(metalId) && !spikes.canUse(metalId)) {
      activeMetalminds.clearMetal(ferringType);
    }
    ferringType = metalId;
  }

  @Override
  public MetalId getFerringType() {
    if (ferringType == null) {
      ferringType = MetalManager.INSTANCE.getRandomFerring(player.getRandom()).id();
    }
    return ferringType;
  }

  @Override
  public void tick() {
    activeMetalminds.tick();
  }

  @Override
  public void getFeruchemyTooltip(List<Component> tooltip) {
    activeMetalminds.getTooltip(tooltip);
  }

  @Override
  public void getHemalurgyTooltip(List<Component> tooltip) {
    spikes.getTooltip(tooltip);
  }

  @Override
  public boolean equip(ItemStack stack) {
    return metalminds.equip(stack) || spikes.equip(stack);
  }

  @Override
  public void dropItems(Collection<ItemEntity> drops) {
    metalminds.dropItems(player, drops);
    spikes.dropItems(player, drops);
  }

  @Override
  public void clear() {
    metalminds.clear();
    spikes.clear();
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
      this.spikes.copyFrom(other.spikes);
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
  private static final String SPIKES = "spikes";

  @Override
  public CompoundTag serializeNBT() {
    CompoundTag tag = new CompoundTag();
    if (ferringType != null) {
      tag.putString(FERRING_TYPE, ferringType.toString());
    }
    tag.put(METALMINDS, metalminds.serializeNBT());
    tag.put(SPIKES, spikes.serializeNBT());
    return tag;
  }

  @Override
  public void deserializeNBT(CompoundTag nbt) {
    if (nbt.contains(FERRING_TYPE, Tag.TAG_STRING)) {
      this.ferringType = MetalId.tryParse(nbt.getString(FERRING_TYPE));
    }
    // important that spikes are deserialized before metalminds as they determine what may be tapped
    if (nbt.contains(SPIKES, Tag.TAG_LIST)) {
      this.spikes.deserializeNBT(nbt.getList(SPIKES, Tag.TAG_COMPOUND));
      this.spikes.refreshActive();
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

  /** copy caps when the player respawns/returns from the end */
  private static void playerClone(PlayerEvent.Clone event) {
    Player original = event.getOriginal();
    original.reviveCaps();
    getData(event.getEntity()).copyFrom(getData(original), event.isWasDeath());
    original.invalidateCaps();
  }
}

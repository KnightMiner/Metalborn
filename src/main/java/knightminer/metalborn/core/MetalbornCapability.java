package knightminer.metalborn.core;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.inventory.ActiveMetalminds;
import knightminer.metalborn.core.inventory.MetalmindInventory;
import knightminer.metalborn.core.inventory.MetalmindInventory.MetalmindStack;
import knightminer.metalborn.core.inventory.SpikeInventory;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
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
    this.activeMetalminds = new ActiveMetalminds(this, player);
    this.metalminds = new MetalmindInventory(this, player);
    this.spikes = new SpikeInventory(this, player);
  }

  /** Gets the metalmind inventory */
  public MetalmindInventory getMetalminds() {
    return metalminds;
  }

  /** Gets the metalmind inventory */
  public SpikeInventory getSpikes() {
    return spikes;
  }


  /* Ferring */

  @Override
  public void setFerringType(MetalId metalId) {
    MetalId oldType = ferringType;
    ferringType = metalId;
    if (oldType != null && !oldType.equals(metalId)) {
      // stop storing the power if we were
      if (activeMetalminds.isStoringFerring()) {
        activeMetalminds.clearStoringFerring();
      } else {
        // notify that the ferring type is no longer available
        onRemoved(oldType);
      }
    }
  }

  @Override
  public MetalId getFerringType() {
    if (ferringType == null) {
      ferringType = MetalManager.INSTANCE.getRandomFerring(player.getRandom()).id();
    }
    return ferringType;
  }

  @Override
  public void storeFerring(int index) {
    // find the stack and store that
    MetalmindStack stack = metalminds.getSlot(index);
    if (stack != null) {
      activeMetalminds.storeFerring(stack);
    }
  }

  @Override
  public void stopStoringFerring(int index) {
    // find the stack and store that
    MetalmindStack stack = metalminds.getSlot(index);
    if (stack != null) {
      activeMetalminds.stopStoringFerring(stack);
    }
  }


  /* Metal powers */

  @Override
  public boolean canUse(MetalId metal) {
    // can use our ferring type (which may need to be computed) as long as not storing it
    return (!activeMetalminds.isStoringFerring() && getFerringType().equals(metal))
      // can use any power granted by a spike or metalmind
      || spikes.canUse(metal) || activeMetalminds.canUse(metal);
  }

  @Override
  public void updatePower(MetalId metal, int index, int newLevel, int oldLevel) {
    MetalmindStack stack = metalminds.getSlot(index);
    if (stack != null) {
      activeMetalminds.getMetal(metal).update(stack, newLevel, oldLevel);
    }
  }

  /**
   * Called when a source of a metal is removed to deactivate the related power.
   * No need to check if the metal is still usable via {@link #canUse(MetalId)} before calling, that will be checked internally.
   */
  public void onRemoved(MetalId metal) {
    if (!canUse(metal)) {
      activeMetalminds.onRemoved(metal);
    }
  }

  @Override
  public void grantPower(MetalId metal, int index) {
    MetalmindStack stack = metalminds.getSlot(index);
    if (stack != null) {
      activeMetalminds.getMetal(metal).grantPower(stack);
    }
  }

  @Override
  public void revokePower(MetalId metal, int index) {
    MetalmindStack stack = metalminds.getSlot(index);
    if (stack != null) {
      activeMetalminds.getMetal(metal).revokePower(stack);
    }
  }


  /* Inventory */

  @Override
  public void tick() {
    activeMetalminds.tick();
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


  /* Menu */

  @Override
  public void getFeruchemyTooltip(List<Component> tooltip) {
    activeMetalminds.getTooltip(tooltip);
  }

  @Override
  public void getHemalurgyTooltip(List<Component> tooltip) {
    spikes.getTooltip(tooltip);
  }

  @Override
  public void clear() {
    activeMetalminds.clear();
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
      this.activeMetalminds.clear();
      // there may be some redundant work as all the metalminds are copied over
      // however, it did not seem work having a suppress updates part of the API when this is rarely called
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
    // clear any active powers, ensures we don't apply double effects upon refreshing metalminds
    this.activeMetalminds.clear();
    // read both inventories from NBT, this will automatically update any relevant effects
    this.spikes.deserializeNBT(nbt.getList(SPIKES, Tag.TAG_COMPOUND));
    this.metalminds.deserializeNBT(nbt.getList(METALMINDS, Tag.TAG_COMPOUND));
  }


  /* Capability registration */

  /** Registers this capability with any relevant events */
  public static void register() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.NORMAL, false, RegisterCapabilitiesEvent.class, MetalbornCapability::register);
    MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, MetalbornCapability::attachCapability);
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
}

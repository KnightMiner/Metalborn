package knightminer.metalborn.core;

import knightminer.metalborn.core.inventory.ActiveMetalminds;
import knightminer.metalborn.core.inventory.MetalmindInventory;
import knightminer.metalborn.core.inventory.MetalmindInventory.MetalmindStack;
import knightminer.metalborn.core.inventory.SpikeInventory;
import knightminer.metalborn.item.SatchelItem.SatchelType;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Instance of {@link MetalbornData} containing all data related to metalborn status for players. Includes ferring type, tapping/storing, metalminds, spikes, and alike. */
public class MetalbornPlayer implements MetalbornData {
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
  /** Copy of {@link Entity#walkDistO} for usage in ticking logic since it gets cleared at start of tick. */
  private float lastWalkDistance = 0;

  MetalbornPlayer(Player player) {
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
      String spawnType = Config.SPAWN_AS_FERRING.get();
      if (!spawnType.isEmpty()) {
        ferringType = MetalId.tryParse(spawnType);
        if (ferringType != null) {
          return ferringType;
        }
      }
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


  /* Identity */

  @Nullable
  @Override
  public UUID getIdentity() {
    return activeMetalminds.getIdentity();
  }

  @Override
  public String getIdentityName() {
    return activeMetalminds.getIdentityName();
  }

  @Override
  public boolean canTapIdentity(int index) {
    // if not in a slot, just say yes
    if (index == -1) {
      return true;
    }
    // check that the slot is the active slot, or we aren't tapping
    MetalmindStack stack = metalminds.getSlot(index);
    return stack != null && activeMetalminds.canTapIdentity(stack);
  }

  @Override
  public void updateTappingIdentity(int index, @Nullable UUID uuid, String name) {
    MetalmindStack stack = metalminds.getSlot(index);
    if (stack != null) {
      activeMetalminds.updateTappingIdentity(stack, uuid, name);
    }
  }

  @Override
  public void startStoringIdentity(int index) {
    MetalmindStack stack = metalminds.getSlot(index);
    if (stack != null) {
      activeMetalminds.startStoringIdentity(stack);
    }
  }

  @Override
  public void stopStoringIdentity(int index) {
    MetalmindStack stack = metalminds.getSlot(index);
    if (stack != null) {
      activeMetalminds.stopStoringIdentity(stack);
    }
  }


  /* Breath */

  @Override
  public float getLastWalkDistance() {
    return lastWalkDistance;
  }

  @Override
  public void setLastWalkDistance(float lastWalkDistance) {
    this.lastWalkDistance = lastWalkDistance;
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

  /** Creates an item entity for the given stack */
  private ItemEntity createDrop(ItemStack stack) {
    ItemEntity itemEntity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack);
    itemEntity.setDefaultPickUpDelay();
    return itemEntity;
  }

  @Override
  public void dropItems(Collection<ItemEntity> drops) {
    List<ItemStack> stacks = new ArrayList<>(14);
    metalminds.dropItems(player, stacks);
    spikes.dropItems(player, stacks);

    // if we have at least 2 items and are using the mist satchel, drop as that
    if (stacks.size() > 1 && Config.DROP_AS_SATCHEL.get()) {
      ItemStack satchel = new ItemStack(Registration.SATCHEL.get(SatchelType.MIST));
      IItemHandler inventory = satchel.getCapability(Capabilities.ItemHandler.ITEM);

      // ensure we have enough slots in the satchel for all items and can quickly fill, should always be true but better to be safe
      if (inventory != null && inventory.getSlots() >= stacks.size() && inventory instanceof IItemHandlerModifiable modifiable) {
        for (int i = 0; i < stacks.size(); i++) {
          modifiable.setStackInSlot(i, stacks.get(i));
        }
        drops.add(createDrop(satchel));
        return;
      }
    }

    // drop each item separately
    for (ItemStack stack : stacks) {
      drops.add(createDrop(stack));
    }
  }


  /* Menu */

  @Override
  public boolean getFeruchemyTooltip(List<Component> tooltip) {
    return activeMetalminds.getTooltip(tooltip);
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
  

  /* Attachment logic */

  @Override
  public void copyFrom(MetalbornData data, boolean wasDeath) {
    // the only reason this should fail is if the original player had no data
    if (data instanceof MetalbornPlayer other) {
      if (!wasDeath || Config.KEEP_ON_DEATH.get()) {
        this.ferringType = other.ferringType;
      }
      this.activeMetalminds.clear();
      // copy spikes first as they may be what allows a power to be active
      this.spikes.copyFrom(other.spikes, wasDeath);
      // there may be some redundant work as all the metalminds are copied over
      // however, it did not seem work having a suppress updates part of the API when this is rarely called
      this.metalminds.copyFrom(other.metalminds, wasDeath);
    }
  }


  /* NBT */

  private static final String FERRING_TYPE = "ferring_type";
  private static final String METALMINDS = "metalminds";
  private static final String SPIKES = "spikes";

  @Override
  public CompoundTag serializeNBT(HolderLookup.Provider provider) {
    CompoundTag tag = new CompoundTag();
    if (ferringType != null) {
      tag.putString(FERRING_TYPE, ferringType.toString());
    }
    tag.put(METALMINDS, metalminds.serializeNBT(provider));
    tag.put(SPIKES, spikes.serializeNBT(provider));
    return tag;
  }

  @Override
  public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
    if (nbt.contains(FERRING_TYPE, Tag.TAG_STRING)) {
      this.ferringType = MetalId.tryParse(nbt.getString(FERRING_TYPE));
      // resolve the redirect in the metal power ID if present
      if (this.ferringType != null) {
        MetalPower power = MetalManager.INSTANCE.resolve(this.ferringType);
        // do not delete their power if its missing
        if (power != null) {
          ferringType = power.id();
        }
      }
    }
    // clear any active powers, ensures we don't apply double effects upon refreshing metalminds
    this.activeMetalminds.clear();
    // read both inventories from NBT, this will automatically update any relevant effects
    this.spikes.deserializeNBT(provider, nbt.getList(SPIKES, Tag.TAG_COMPOUND));
    this.metalminds.deserializeNBT(provider, nbt.getList(METALMINDS, Tag.TAG_COMPOUND));
  }
}

package knightminer.metalborn.core;

import knightminer.metalborn.metal.MetalId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Base interface for metalborn capability. Allows having a read only empty instance and a writable player instance */
@NonExtendable
public interface MetalbornData extends INBTSerializable<CompoundTag> {
  /* Ferring */

  /** Sets the ferring metal type */
  default void setFerringType(MetalId metalId) {}

  /**
   * Gets the type of ferring for the player.
   * Note if the goal is to check whether this metal is usable, {@link #canUse(MetalId)} is a better choice.
   */
  default MetalId getFerringType() {
    return MetalId.NONE;
  }

  /**
   * Updates the index currently storing ferring power.
   * @param index  Index to store, or -1 to stop storing.
   */
  default void storeFerring(int index) {}

  /**
   * Updates the index currently storing ferring power.
   * @param index  Index to store, or -1 to stop storing.
   */
  default void stopStoringFerring(int index) {}


  /* Powers */

  /** Checks if the given metal can be used */
  default boolean canUse(MetalId metal) {
    return false;
  }

  /** Called to update standard metal effects for the given metal */
  default void updatePower(MetalId metal, int index, int newLevel, int oldLevel) {}

  /** Called to start granting a power from the given index */
  default void grantPower(MetalId metal, int index) {}

  /** Called to stop granting a power from the given index */
  default void revokePower(MetalId metal, int index) {}


  /* Identity */

  /** Gets the identity of the entity */
  @Nullable
  default UUID getIdentity() {
    return null;
  }

  /** Gets the name of the entity based on the identity */
  default String getIdentityName() {
    return "";
  }

  /** Checks if identity may be tapped at the given index */
  default boolean canTapIdentity(int index) {
    return false;
  }

  /**
   * Updates the identity being tapped
   *
   * @param index Index to tap.
   * @param uuid  New identity. Set to null to stop tapping identity.
   * @param name  Name of the new identity. Unused if {@code uuid} is null.
   */
  default void updateTappingIdentity(int index, @Nullable UUID uuid, String name) {}

  /** Starts tapping identity at the given index */
  default void startStoringIdentity(int index) {}

  /** Stops tapping identity at the given index */
  default void stopStoringIdentity(int index) {}


  /* Breath */

  default float getLastWalkDistance() {
    return 0;
  }

  default void setLastWalkDistance(float value) {}


  /* Unsealed */

  /** Checks if we can use an unsealed metalmind at the given index */
  default boolean canUseUnsealed(int index) {
    return false;
  }

  /** Starts using unsealed at the given index */
  default void useUnsealed(int index) {}

  /** Stops using unsealed at the given index */
  default void stopUsingUnsealed(int index) {}


  /* Inventory */

  /**
   * Attempts to add the given item to the metalborn inventory.
   * Note this may not behave as expected if called client side as the inventory only syncs when open.
   * @param stack    Stack to add
   * @return true if an item was added.
   */
  @SuppressWarnings("UnusedReturnValue")
  default boolean equip(ItemStack stack) {
    return false;
  }

  /** Called when the player dies to drop all metalminds */
  default void dropItems(Collection<ItemEntity> drops) {}

  /** Copies the passed data into this data */
  default void copyFrom(MetalbornData data, boolean wasDeath) {}

  /** Ticks all effects */
  default void tick() {}


  /* Menu */

  /**
   * Gets the tooltip for display in the screen.
   * @return true if any powers are active.
   */
  default boolean getFeruchemyTooltip(List<Component> tooltip) {
    return false;
  }

  /** Gets the tooltip for display in the screen */
  default void getHemalurgyTooltip(List<Component> tooltip) {}

  /** Called client side to clear old data when the inventory is opened */
  default void clear() {}


  /** Empty instance for defaulting data related methods */
  MetalbornData EMPTY = new MetalbornData() {
    @Override
    public CompoundTag serializeNBT() {
      return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {}
  };

  /** Gets the data for the given player */
  static MetalbornData getData(LivingEntity player) {
    return player.getCapability(MetalbornCapability.CAPABILITY).orElse(EMPTY);
  }
}

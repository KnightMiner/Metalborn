package knightminer.metalborn.core;

import knightminer.metalborn.metal.MetalId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

import java.util.Collection;
import java.util.List;

/** Base interface for metalborn capability. Allows having a read only empty instance and a writable player instance */
@NonExtendable
public interface MetalbornData extends INBTSerializable<CompoundTag> {
  /** Sets the ferring metal type */
  default void setFerringType(MetalId metalId) {}

  /**
   * Gets the type of ferring for the player.
   * Note if the goal is to check whether this metal is usable, {@link #canUse(MetalId)} is a better choice.
   */
  default MetalId getFerringType() {
    return MetalId.NONE;
  }

  /** Checks if the given metal can be used */
  default boolean canUse(MetalId metal) {
    return false;
  }

  /** Called to update standard metal effects for the given metal */
  default void updatePower(MetalId metal, int index, int newLevel, int oldLevel) {}

  /**
   * Called when a source of a metal is removed to deactivate the related power.
   * No need to check if the metal is still usable via {@link #canUse(MetalId)} before calling, that will be checked internally.
   */
  default void onRemoved(MetalId metal) {}

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

  /** Gets the tooltip for display in the screen */
  default void getFeruchemyTooltip(List<Component> tooltip) {}

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

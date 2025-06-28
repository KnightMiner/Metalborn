package knightminer.metalborn.core;

import knightminer.metalborn.metal.MetalId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
  void setFerringType(MetalId metalId);

  /**
   * Gets the type of ferring for the player.
   * Note if the goal is to check whether this metal is usable, {@link #canUse(MetalId)} is a better choice.
   */
  MetalId getFerringType();

  /** Checks if the given metal can be used */
  boolean canUse(MetalId metal);

  /**
   * Attempts to add the given item to the metalborn inventory.
   * Note this may not behave as expected if called client side as the inventory only syncs when open.
   * @param stack    Stack to add
   * @return true if an item was added.
   */
  @SuppressWarnings("UnusedReturnValue")
  boolean equip(ItemStack stack);

  /** Called when the player dies to drop all metalminds */
  void dropItems(Collection<ItemEntity> drops);

  /** Copies the passed data into this data */
  void copyFrom(MetalbornData data, boolean wasDeath);

  /** Ticks all effects */
  void tick();

  /** Gets the tooltip for display in the screen */
  void getFeruchemyTooltip(List<Component> tooltip);

  /** Gets the tooltip for display in the screen */
  void getHemalurgyTooltip(List<Component> tooltip);

  /** Called client side to clear old data when the inventory is opened */
  void clear();

  /** Empty instance for defaulting data related methods */
  MetalbornData EMPTY = new MetalbornData() {
    @Override
    public void setFerringType(MetalId metalId) {}

    @Override
    public MetalId getFerringType() {
      return MetalId.NONE;
    }

    @Override
    public boolean canUse(MetalId metal) {
      return false;
    }

    @Override
    public boolean equip(ItemStack stack) {
      return false;
    }

    @Override
    public void dropItems(Collection<ItemEntity> drops) {}

    @Override
    public void copyFrom(MetalbornData data, boolean wasDeath) {}

    @Override
    public CompoundTag serializeNBT() {
      return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {}

    @Override
    public void tick() {}

    @Override
    public void getFeruchemyTooltip(List<Component> tooltip) {}

    @Override
    public void getHemalurgyTooltip(List<Component> tooltip) {}

    @Override
    public void clear() {}
  };
}

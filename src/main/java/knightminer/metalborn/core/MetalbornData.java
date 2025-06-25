package knightminer.metalborn.core;

import knightminer.metalborn.metal.MetalId;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

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

  /** Copies the passed data into this data */
  void copyFrom(MetalbornData data, boolean wasDeath);

  /** Ticks all effects */
  void tick();

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
    public void copyFrom(MetalbornData data, boolean wasDeath) {}

    @Override
    public CompoundTag serializeNBT() {
      return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {}

    @Override
    public void tick() {}
  };
}

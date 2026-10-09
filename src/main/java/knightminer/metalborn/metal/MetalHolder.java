package knightminer.metalborn.metal;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.registration.object.IdAwareObject;

/**
 * Data component for metal powers. Contains lazily loaded metal power, which is automatically cleared when datapacks reload.
 * Instances can be obtained from {@link MetalManager#getHolder(MetalId)}.
 */
public class MetalHolder implements IdAwareObject {
  /** Empty instance, always resolves to {@link MetalPower#DEFAULT}. */
  public static final MetalHolder EMPTY = new MetalHolder(MetalId.NONE, MetalPower.DEFAULT);
  /** Codec creating metal holders by ID. */
  public static final Codec<MetalHolder> CODEC = MetalId.CODEC.xmap(MetalManager.INSTANCE::getHolder, holder -> holder.power().id());
  /** Stream codec syncing metal holders by ID */
  public static final StreamCodec<ByteBuf,MetalHolder> STREAM_CODEC = MetalId.STREAM_CODEC.map(MetalManager.INSTANCE::getHolder, MetalHolder::id);

  private final MetalId id;
  private MetalPower power;

  /** Constructor used by {@link MetalManager} */
  MetalHolder(MetalId id) {
    this.id = id;
  }

  /** Constructor for empty instance */
  private MetalHolder(MetalId id, MetalPower power) {
    this.id = id;
    this.power = power;
  }

  @Override
  public MetalId id() {
    return id;
  }

  /** Gets the metal power, or {@link MetalPower#DEFAULT} if absent. */
  public MetalPower power() {
    if (power == null) {
      power = MetalManager.INSTANCE.get(id);
    }
    return power;
  }

  /** Checks if this power is present in the datapack. */
  public boolean isPresent() {
    return power().isPresent();
  }

  /** Gets a holder instance after applying redirects. */
  public MetalHolder resolve() {
    MetalId resolved = power().id();
    if (resolved.equals(id)) {
      return this;
    }
    return MetalManager.INSTANCE.getHolder(resolved);
  }

  /** Clears the cached power instance */
  void clearCache() {
    power = null;
  }

  @Override
  public boolean equals(@Nullable Object object) {
    return object instanceof MetalHolder other && this.id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}

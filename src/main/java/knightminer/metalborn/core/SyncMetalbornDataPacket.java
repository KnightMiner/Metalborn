package knightminer.metalborn.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.network.packet.IThreadsafePacket;

/** Packet to sync full metalborn data to the client */
public record SyncMetalbornDataPacket(CompoundTag tag) implements IThreadsafePacket {
  public SyncMetalbornDataPacket(MetalbornData data) {
    this(data.serializeNBT());
  }

  /** Decodes the packet from NBT */
  public static SyncMetalbornDataPacket decode(FriendlyByteBuf buffer) {
    CompoundTag tag = buffer.readNbt();
    if (tag == null) {
      tag = new CompoundTag();
    }
    return new SyncMetalbornDataPacket(tag);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeNbt(this.tag);
  }

  @Override
  public void handleThreadsafe(Context context) {
    Player player = SafeClientAccess.getPlayer();
    if (player != null) {
      player.getCapability(MetalbornCapability.CAPABILITY).ifPresent(data -> data.deserializeNBT(this.tag));
    }
  }
}

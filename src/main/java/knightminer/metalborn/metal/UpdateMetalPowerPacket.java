package knightminer.metalborn.metal;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.packet.ISimplePacket;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Packet to sync metal powers to the client */
@Internal
public record UpdateMetalPowerPacket(Map<MetalId, MetalPower> powers) implements ISimplePacket {
  /** Decodes the packet from the buffer */
  public static UpdateMetalPowerPacket decode(FriendlyByteBuf buffer) {
    Map<MetalId,MetalPower> map = new HashMap<>();
    int size = buffer.readVarInt();
    for (int i = 0; i < size; i++) {
      MetalId id = MetalId.LOADABLE.decode(buffer);
      MetalPower power = MetalPower.LOADABLE.decode(buffer, MetalManager.createContext(id));
      map.put(id, power);
    }
    return new UpdateMetalPowerPacket(Map.copyOf(map));
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(powers.size());
    for (MetalPower power : powers.values()) {
      MetalId.LOADABLE.encode(buffer, power.id());
      MetalPower.LOADABLE.encode(buffer, power);
    }
  }

  @Override
  public void handle(Supplier<Context> context) {
    MetalManager.INSTANCE.updateMetalPowers(powers);
  }
}

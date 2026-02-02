package knightminer.metalborn.metal;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.packet.ISimplePacket;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

/** Packet to sync metal powers to the client */
@Internal
public record UpdateMetalPowerPacket(Map<MetalId, MetalPower> powers, Map<MetalId, MetalPower> redirects) implements ISimplePacket {
  /** Decodes the packet from the buffer */
  public static UpdateMetalPowerPacket decode(FriendlyByteBuf buffer) {
    // read powers first
    Map<MetalId,MetalPower> powers = new HashMap<>();
    int size = buffer.readVarInt();
    for (int i = 0; i < size; i++) {
      MetalId id = MetalId.LOADABLE.decode(buffer);
      MetalPower power = MetalPower.LOADABLE.decode(buffer, MetalManager.createContext(id));
      powers.put(id, power);
    }
    // read redirects, only the ID syncs
    Map<MetalId,MetalPower> redirects = new HashMap<>();
    size = buffer.readVarInt();
    for (int i = 0; i < size; i++) {
      MetalId id = MetalId.LOADABLE.decode(buffer);
      // if it's missing, don't worry about it; though should never happen
      MetalPower target = powers.get(MetalId.LOADABLE.decode(buffer));
      if (target != null) {
        redirects.put(id, target);
      }
    }
    return new UpdateMetalPowerPacket(Map.copyOf(powers), Map.copyOf(redirects));
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(powers.size());
    for (MetalPower power : powers.values()) {
      MetalId.LOADABLE.encode(buffer, power.id());
      MetalPower.LOADABLE.encode(buffer, power);
    }
    // for redirects, just write the ID, not the full power
    buffer.writeVarInt(redirects.size());
    for (Entry<MetalId,MetalPower> entry : redirects.entrySet()) {
      MetalId.LOADABLE.encode(buffer, entry.getKey());
      MetalId.LOADABLE.encode(buffer, entry.getValue().id());
    }
  }

  @Override
  public void handle(Supplier<Context> context) {
    MetalManager.INSTANCE.updateMetalPowers(powers, redirects);
  }
}

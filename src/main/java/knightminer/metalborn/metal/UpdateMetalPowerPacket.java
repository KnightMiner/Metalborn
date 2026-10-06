package knightminer.metalborn.metal;

import knightminer.metalborn.Metalborn;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.ISimplePacket;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/** Packet to sync metal powers to the client */
@Internal
public record UpdateMetalPowerPacket(Map<MetalId, MetalPower> powers, Map<MetalId, MetalPower> redirects) implements ISimplePacket {
  public static final Type<UpdateMetalPowerPacket> TYPE = new Type<>(Metalborn.resource("update_metal_powers"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateMetalPowerPacket> CODEC = new StreamCodec<>() {
    @Override
    public UpdateMetalPowerPacket decode(RegistryFriendlyByteBuf buffer) {
      // read powers first
      Map<MetalId,MetalPower> powers = new HashMap<>();
      int size = buffer.readVarInt();
      for (int i = 0; i < size; i++) {
        ResourceLocation id = buffer.readResourceLocation();
        MetalPower power = MetalPower.LOADABLE.decode(buffer, MetalManager.createContext(id));
        powers.put(power.id(), power);
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
    public void encode(RegistryFriendlyByteBuf buffer, UpdateMetalPowerPacket packet) {
      buffer.writeVarInt(packet.powers.size());
      for (MetalPower power : packet.powers.values()) {
        buffer.writeResourceLocation(power.id());
        MetalPower.LOADABLE.encode(buffer, power);
      }
      // for redirects, just write the ID, not the full power
      buffer.writeVarInt(packet.redirects.size());
      for (Entry<MetalId,MetalPower> entry : packet.redirects.entrySet()) {
        MetalId.LOADABLE.encode(buffer, entry.getKey());
        MetalId.LOADABLE.encode(buffer, entry.getValue().id());
      }
    }
  };

  @Override
  public Type<UpdateMetalPowerPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    MetalManager.INSTANCE.updateMetalPowers(powers, redirects);
  }
}

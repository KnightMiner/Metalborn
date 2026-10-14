package knightminer.metalborn.network;

import io.netty.buffer.ByteBuf;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.menu.MetalbornMenu;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.mantle.network.codec.MantleStreamCodecs;

/** Packet sent from client to server when keybindings are pressed */
public enum ControlPacket implements ISimplePacket {
  INVENTORY;

  public static final Type<ControlPacket> TYPE = new Type<>(Metalborn.resource("control"));
  public static final StreamCodec<ByteBuf, ControlPacket> CODEC = MantleStreamCodecs.enumCodec(ControlPacket.class, OutOfBoundsStrategy.ZERO);

  @Override
  public Type<ControlPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    Player player = context.player();
    if (!player.hasContainerOpen() && !player.isSpectator()) {
      MetalId ferringType = MetalbornData.getData(player).getFerringType();
      player.openMenu(new SimpleMenuProvider(
        (id, inventory, p) -> new MetalbornMenu(id, inventory),
        Component.empty()
      ), buffer -> {
        buffer.writeResourceLocation(ferringType);
        // no satchel
        buffer.writeByte(-1);
      });
    }
  }
}

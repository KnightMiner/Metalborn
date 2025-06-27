package knightminer.metalborn.network;

import knightminer.metalborn.core.MetalbornCapability;
import knightminer.metalborn.menu.MetalbornMenu;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkHooks;
import slimeknights.mantle.network.packet.IThreadsafePacket;

/** Packet sent from client to server when keybindings are pressed */
public enum ControlPacket implements IThreadsafePacket {
  INVENTORY;

  public static ControlPacket decode(FriendlyByteBuf buffer) {
    return buffer.readEnum(ControlPacket.class);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeEnum(this);
  }

  @Override
  public void handleThreadsafe(Context context) {
    ServerPlayer player = context.getSender();
    if (player != null) {
      MetalId ferringType = MetalbornCapability.getData(player).getFerringType();
      NetworkHooks.openScreen(player, new SimpleMenuProvider(
        (id, inventory, p) -> new MetalbornMenu(id, inventory),
        Component.empty()
      ), buffer -> buffer.writeResourceLocation(ferringType));
    }
  }
}

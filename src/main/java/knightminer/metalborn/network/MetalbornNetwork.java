package knightminer.metalborn.network;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.UpdateMetalPowerPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import slimeknights.mantle.network.ISimplePacket;

/** Network instance for Metalborn */
@EventBusSubscriber(modid = Metalborn.MOD_ID)
public class MetalbornNetwork {
  /**
   * Network instance
   * 21.0: Initial 1.21.1 release
   */
  private static final String VERSION = "21.0";

  /**
   * Registers packets into this network
   */
  @SubscribeEvent
  static void registerPackets(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar registrar = event.registrar(VERSION);

    // datapacks
    registrar.executesOn(HandlerThread.NETWORK);
    registrar.playToClient(UpdateMetalPowerPacket.TYPE, UpdateMetalPowerPacket.CODEC, ISimplePacket.handle());

    // controls
    registrar.executesOn(HandlerThread.MAIN);
    registrar.playToServer(ControlPacket.TYPE, ControlPacket.CODEC, ISimplePacket.handle());
  }
}

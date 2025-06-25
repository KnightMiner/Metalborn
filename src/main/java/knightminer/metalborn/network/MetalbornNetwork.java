package knightminer.metalborn.network;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.UpdateMetalPowerPacket;
import net.minecraftforge.network.NetworkDirection;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.NetworkWrapper;

/** Network instance for Metalborn */
public class MetalbornNetwork extends NetworkWrapper {
  private static MetalbornNetwork instance = null;
  private MetalbornNetwork() {
    super(Metalborn.resource("network"));
  }

  /** Gets the instance of the network */
  public static MetalbornNetwork getInstance() {
    if (instance == null) {
      throw new IllegalStateException("Attempt to call network getInstance before network is setup");
    }
    return instance;
  }

  /** Called during mod construction to setup the network */
  @Internal
  public static void setup() {
    if (instance != null) {
      return;
    }
    instance = new MetalbornNetwork();

    // datapacks
    instance.registerPacket(UpdateMetalPowerPacket.class, UpdateMetalPowerPacket::decode, NetworkDirection.PLAY_TO_CLIENT);
    // capability
    instance.registerPacket(SyncMetalbornDataPacket.class, SyncMetalbornDataPacket::decode, NetworkDirection.PLAY_TO_CLIENT);
    // controls
    instance.registerPacket(ControlPacket.class, ControlPacket::decode, NetworkDirection.PLAY_TO_SERVER);
  }
}

package knightminer.metalborn;

import knightminer.metalborn.core.MetalbornNetwork;
import knightminer.metalborn.metal.MetalManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Base mod class, communicating with the modloader. */
@Mod(Metalborn.MOD_ID)
public class Metalborn {
  public static final String MOD_ID = "metalborn";
  public static final Logger LOG = LogManager.getLogger(MOD_ID);

  public Metalborn() {
    MetalbornNetwork.setup();
    MetalManager.INSTANCE.init();
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(this::gatherData);
  }

  private void gatherData(GatherDataEvent event) {
  }


  /* Mod ID helpers */

  /**
   * Gets a resource location at the Ceramics namespace
   * @param name  Resource path
   * @return  Resource location for Ceramics
   */
  public static ResourceLocation resource(String name) {
    return new ResourceLocation(MOD_ID, name);
  }

  /**
   * Forms the mod ID into a language key
   * @param group Language key group
   * @param name Name within group
   * @return Language key
   */
  public static String key(String group, String name) {
    return String.format("%s.%s.%s", group, MOD_ID, name);
  }

  /**
   * Forms a translation component with the following details
   * @param group Language key group
   * @param name Name within group
   * @return Language key
   */
  public static MutableComponent component(String group, String name) {
    return Component.translatable(key(group, name));
  }
}

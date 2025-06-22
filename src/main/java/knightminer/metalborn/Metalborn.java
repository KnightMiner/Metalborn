package knightminer.metalborn;

import knightminer.metalborn.command.MetalbornCommand;
import knightminer.metalborn.core.MetalbornCapability;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.data.DamageTypeTagProvider;
import knightminer.metalborn.data.EntityTagProvider;
import knightminer.metalborn.data.MetalPowerProvider;
import knightminer.metalborn.data.RegistryProvider;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.network.MetalbornNetwork;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/** Base mod class, communicating with the modloader. */
@Mod(Metalborn.MOD_ID)
public class Metalborn {
  public static final String MOD_ID = "metalborn";
  public static final Logger LOG = LogManager.getLogger(MOD_ID);

  public Metalborn() {
    MetalbornNetwork.setup();
    MetalManager.INSTANCE.init();
    Registration.init();
    MetalbornCommand.init();
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(this::commonSetup);
    bus.addListener(this::gatherData);
  }

  private void commonSetup(FMLCommonSetupEvent event) {
    MetalbornCapability.register();
  }

  private void gatherData(GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();
    PackOutput packOutput = generator.getPackOutput();
    ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
    CompletableFuture<Provider> lookupProvider = event.getLookupProvider();
    boolean server = event.includeServer();

    DatapackBuiltinEntriesProvider datapackRegistry = RegistryProvider.prepare(packOutput, lookupProvider);
    generator.addProvider(server, datapackRegistry);
    generator.addProvider(server, new DamageTypeTagProvider(packOutput, datapackRegistry.getRegistryProvider(), existingFileHelper));
    generator.addProvider(server, new EntityTagProvider(packOutput, lookupProvider, existingFileHelper));
    generator.addProvider(server, new MetalPowerProvider(packOutput));
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
   * Prefixes the given unlocalized name with tinkers prefix. Use this when passing unlocalized names for a uniform
   * namespace.
   */
  public static String prefix(String name) {
    return MOD_ID + "." + name.toLowerCase(Locale.US);
  }

  /**
   * Forms the mod ID into a language key
   * @param group Language key group
   * @param name Name within group
   * @return Language key
   */
  public static String key(String group, String name) {
    return group + '.' + MOD_ID + '.' + name;
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

  /**
   * Forms a translation component with the following details
   * @param group Language key group
   * @param name Name within group
   * @param args Additional format arguments
   * @return Language key
   */
  public static MutableComponent component(String group, String name, Object... args) {
    return Component.translatable(key(group, name), args);
  }
}

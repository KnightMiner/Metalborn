package knightminer.metalborn;

import knightminer.metalborn.client.MetalbornClient;
import knightminer.metalborn.command.MetalbornCommand;
import knightminer.metalborn.core.Config;
import knightminer.metalborn.core.MetalbornCapability;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.data.BlockLootTableProvider;
import knightminer.metalborn.data.GlobalLootModifiersProvider;
import knightminer.metalborn.data.MetalPowerProvider;
import knightminer.metalborn.data.RecipeProvider;
import knightminer.metalborn.data.RegistryProvider;
import knightminer.metalborn.data.client.BlockModelProvider;
import knightminer.metalborn.data.client.ItemModelProvider;
import knightminer.metalborn.data.client.SpriteSourceProvider;
import knightminer.metalborn.data.tag.BlockTagProvider;
import knightminer.metalborn.data.tag.DamageTypeTagProvider;
import knightminer.metalborn.data.tag.EntityTagProvider;
import knightminer.metalborn.data.tag.ItemTagProvider;
import knightminer.metalborn.data.tag.MenuTagProvider;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.network.MetalbornNetwork;
import knightminer.metalborn.plugin.tinkers.TinkersPlugin;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Base mod class, communicating with the modloader. */
@Mod(Metalborn.MOD_ID)
public class Metalborn {
  public static final String MOD_ID = "metalborn";
  public static final Logger LOG = LogManager.getLogger(MOD_ID);

  /** Tinkers' Construct mod ID for various compat */
  public static final String TINKERS = "tconstruct";

  public Metalborn() {
    ModLoadingContext.get().registerConfig(Type.COMMON, Config.COMMON_SPEC);
    MetalbornNetwork.setup();
    MetalManager.INSTANCE.init();
    Registration.init();
    MetalbornCommand.init();

    // Tinkers' Construct support
    if (ModList.get().isLoaded(TINKERS)) {
      TinkersPlugin.init();
    }

    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(this::commonSetup);
    bus.addListener(Metalborn::gatherData);

    // client setup
    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> MetalbornClient::onConstruct);
  }

  private void commonSetup(FMLCommonSetupEvent event) {
    MetalbornCapability.register();
  }

  private static void gatherData(GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();
    PackOutput packOutput = generator.getPackOutput();
    ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
    CompletableFuture<Provider> lookupProvider = event.getLookupProvider();
    boolean server = event.includeServer();
    boolean client = event.includeClient();

    // server
    DatapackBuiltinEntriesProvider datapackRegistry = RegistryProvider.prepare(packOutput, lookupProvider);
    generator.addProvider(server, datapackRegistry);
    generator.addProvider(server, new MetalPowerProvider(packOutput));
    generator.addProvider(server, new LootTableProvider(packOutput, Set.of(), List.of(new SubProviderEntry(BlockLootTableProvider::new, LootContextParamSets.BLOCK))));
    generator.addProvider(server, new RecipeProvider(packOutput));
    generator.addProvider(server, new GlobalLootModifiersProvider(packOutput));
    // tags
    BlockTagProvider blockTags = new BlockTagProvider(packOutput, lookupProvider, existingFileHelper);
    generator.addProvider(server, blockTags);
    generator.addProvider(server, new ItemTagProvider(packOutput, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
    generator.addProvider(server, new DamageTypeTagProvider(packOutput, datapackRegistry.getRegistryProvider(), existingFileHelper));
    generator.addProvider(server, new EntityTagProvider(packOutput, lookupProvider, existingFileHelper));
    generator.addProvider(server, new MenuTagProvider(packOutput, lookupProvider, existingFileHelper));
    // client
    generator.addProvider(client, new ItemModelProvider(packOutput, existingFileHelper));
    generator.addProvider(client, new BlockModelProvider(packOutput, existingFileHelper));
    generator.addProvider(client, new SpriteSourceProvider(packOutput, existingFileHelper));

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

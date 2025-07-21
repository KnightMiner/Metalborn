package knightminer.metalborn.client;

import com.mojang.blaze3d.platform.InputConstants;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.client.book.MetalbornBook;
import knightminer.metalborn.client.model.ExtendablePalettedPermutations;
import knightminer.metalborn.client.model.PaletteListManager;
import knightminer.metalborn.client.model.PalettedItemModel;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.network.ControlPacket;
import knightminer.metalborn.network.MetalbornNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static knightminer.metalborn.Metalborn.resource;

/** Handles any client specific registrations */
@EventBusSubscriber(modid = Metalborn.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
public class MetalbornClient {
  public static final SpriteSourceType EXTENDABLE_PALETTE = SpriteSources.register(resource("extendable_palette").toString(), ExtendablePalettedPermutations.CODEC);
  /** Universal keybinding */
  private static final KeyMapping KEY = new KeyMapping(Metalborn.key("key", "binding"), KeyConflictContext.IN_GAME, InputConstants.getKey("key.keyboard.m"), "key.categories.metalborn");

  /** Runs during mod constructor to register any important client only things */
  public static void onConstruct() {
    MetalbornBook.init();
    MinecraftForge.EVENT_BUS.addListener(MetalbornClient::clientTick);
  }

  @SubscribeEvent
  static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(PaletteListManager.INSTANCE);
  }

  @SubscribeEvent
  static void registerKeyBindings(RegisterKeyMappingsEvent event) {
    event.register(KEY);
  }

  @SubscribeEvent
  static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
    event.register("paletted", PalettedItemModel.LOADER);
  }

  @SubscribeEvent
  static void registerScreens(FMLClientSetupEvent event) {
    event.enqueueWork(() -> MenuScreens.register(Registration.METALBORN_MENU.get(), MetalbornScreen::new));
    event.enqueueWork(() -> MenuScreens.register(Registration.FORGE_MENU.get(), ForgeScreen::new));
  }

  private static void clientTick(ClientTickEvent event) {
    if (event.phase == TickEvent.Phase.END) {
      while (KEY.consumeClick()) {
        MetalbornNetwork.getInstance().sendToServer(ControlPacket.INVENTORY);
      }
    }
  }
}

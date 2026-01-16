package knightminer.metalborn.client;

import com.mojang.blaze3d.platform.InputConstants;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.client.book.MetalbornBook;
import knightminer.metalborn.client.model.ExtendablePalettedPermutations;
import knightminer.metalborn.client.model.MetalShapeModel;
import knightminer.metalborn.client.model.PaletteListManager;
import knightminer.metalborn.client.model.PalettedItemModel;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.item.SatchelItem.SatchelType;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.network.ControlPacket;
import knightminer.metalborn.network.MetalbornNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
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
  /** Item properties key for a satchel being dyed */
  public static final ResourceLocation DYED = Metalborn.resource("dyed");
  /** Item properties key for an investiture metal item having no metal set */
  public static final ResourceLocation NO_METAL = Metalborn.resource("no_metal");

  /** Runs during mod constructor to register any important client only things */
  public static void onConstruct() {
    MetalbornBook.init();
    MinecraftForge.EVENT_BUS.addListener(MetalbornClient::clientTick);
  }

  @SubscribeEvent
  static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(PaletteListManager.INSTANCE);
    event.registerReloadListener(MetalColorManager.INSTANCE);
  }

  @SubscribeEvent
  static void registerKeyBindings(RegisterKeyMappingsEvent event) {
    event.register(KEY);
  }

  @SubscribeEvent
  static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
    event.register("paletted", PalettedItemModel.LOADER);
    event.register("metal_shape", MetalShapeModel.LOADER);
  }

  @SubscribeEvent
  static void registerItemColors(RegisterColorHandlersEvent.Item event) {
    Registration.SATCHEL.forEach((type, satchel) -> {
      // mist satchels want to tint layer 0
      if (type == SatchelType.MIST) {
        event.register((stack, tintIndex) -> {
          if (satchel.hasCustomColor(stack)) {
            return satchel.getColor(stack);
          }
          return -1;
        }, satchel);
      } else {
        event.register((stack, tintIndex) -> {
          if (tintIndex == 1) {
            return satchel.getColor(stack);
          }
          return -1;
        }, satchel);
      }
    });
  }

  @SubscribeEvent
  static void clientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(() -> {
      MenuScreens.register(Registration.METALBORN_MENU.get(), MetalbornScreen::new);
      MenuScreens.register(Registration.FORGE_MENU.get(), ForgeScreen::new);
    });
    event.enqueueWork(() -> {
      Registration.SATCHEL.forEach(satchel -> {
        ItemProperties.register(satchel, DYED, (stack, level, entity, seed) -> satchel.hasCustomColor(stack) ? 1 : 0);
      });
      ItemPropertyFunction noMetal = (stack, level, entity, seed) -> MetalItem.getMetal(stack) != MetalId.NONE ? 0 : 1;
      ItemProperties.register(Registration.INVESTITURE_BRACER.get(), NO_METAL, noMetal);
      ItemProperties.register(Registration.INVESTITURE_RING.get(), NO_METAL, noMetal);
      ItemProperties.register(Registration.INVESTITURE_SPIKE.get(), NO_METAL, noMetal);
    });
  }

  private static void clientTick(ClientTickEvent event) {
    if (event.phase == TickEvent.Phase.END) {
      while (KEY.consumeClick()) {
        MetalbornNetwork.getInstance().sendToServer(ControlPacket.INVENTORY);
      }
    }
  }
}

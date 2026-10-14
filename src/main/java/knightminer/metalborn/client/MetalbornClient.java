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
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import slimeknights.mantle.client.model.ItemKey;

import static knightminer.metalborn.Metalborn.resource;

/** Handles any client specific registrations */
@Mod(value = Metalborn.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Metalborn.MOD_ID, value = Dist.CLIENT)
public class MetalbornClient {
  public static final SpriteSourceType EXTENDABLE_PALETTE = SpriteSources.register(resource("extendable_palette").toString(), ExtendablePalettedPermutations.CODEC);
  /** Item key instance for metal items */
  public static final ItemKey METAL_KEY = ItemKey.simple(stack -> MetalItem.getMetal(stack).toString());
  /** Universal keybinding */
  private static final KeyMapping KEY = new KeyMapping(Metalborn.key("key", "binding"), KeyConflictContext.IN_GAME, InputConstants.getKey("key.keyboard.m"), "key.categories.metalborn");
  /** Item properties key for a satchel being dyed */
  public static final ResourceLocation DYED = Metalborn.resource("dyed");
  /** Item properties key for an investiture metal item having no metal set */
  public static final ResourceLocation NO_METAL = Metalborn.resource("no_metal");

  /** Runs during mod constructor to register any important client only things */
  public MetalbornClient() {
    MetalbornBook.init();
    ItemKey.LOADER.register(resource("metal"), METAL_KEY.getLoader());
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
    event.register(resource("paletted"), PalettedItemModel.LOADER);
    event.register(resource("metal_shape"), MetalShapeModel.LOADER);
  }

  @SubscribeEvent
  static void registerItemColors(RegisterColorHandlersEvent.Item event) {
    Registration.SATCHEL.forEach((type, satchel) -> {
      // mist satchels want to tint layer 0
      if (type == SatchelType.MIST) {
        event.register((stack, tintIndex) -> DyedItemColor.getOrDefault(stack, -1), satchel);
      } else {
        event.register((stack, tintIndex) -> {
          if (tintIndex == 1) {
            return DyedItemColor.getOrDefault(stack, -1);
          }
          return -1;
        }, satchel);
      }
    });
  }

  @SubscribeEvent
  static void registerScreens(RegisterMenuScreensEvent event) {
    event.register(Registration.METALBORN_MENU.get(), MetalbornScreen::new);
    event.register(Registration.FORGE_MENU.get(), ForgeScreen::new);
  }

  @SubscribeEvent
  static void clientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(() -> {
      ItemPropertyFunction isDyed = (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) ? 1 : 0;
      Registration.SATCHEL.forEach(satchel -> ItemProperties.register(satchel, DYED, isDyed));
      ItemPropertyFunction noMetal = (stack, level, entity, seed) -> MetalItem.getMetal(stack) != MetalId.NONE ? 0 : 1;
      ItemProperties.register(Registration.INVESTITURE_BRACER.get(), NO_METAL, noMetal);
      ItemProperties.register(Registration.INVESTITURE_RING.get(), NO_METAL, noMetal);
      ItemProperties.register(Registration.INVESTITURE_SPIKE.get(), NO_METAL, noMetal);
      ItemProperties.register(Registration.SOULBOUND_INVESTITURE_RING.get(), NO_METAL, noMetal);
    });
  }

  @SubscribeEvent
  static void clientTick(ClientTickEvent.Post event) {
    while (KEY.consumeClick()) {
      PacketDistributor.sendToServer(ControlPacket.INVENTORY);
    }
  }
}

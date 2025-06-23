package knightminer.metalborn.client;

import knightminer.metalborn.Metalborn;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import static knightminer.metalborn.Metalborn.resource;

/** Handles any client specific registrations */
@EventBusSubscriber(modid = Metalborn.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
public class MetalbornClient {
  public static final SpriteSourceType EXTENDABLE_PALETTE = SpriteSources.register(resource("extendable_palette").toString(), ExtendablePalettedPermutations.CODEC);

  /** Runs during mod constructor to register any important client only things */
  public static void onConstruct() {}

  @SubscribeEvent
  static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
    event.register("paletted", PalettedItemModel.LOADER);
  }
}

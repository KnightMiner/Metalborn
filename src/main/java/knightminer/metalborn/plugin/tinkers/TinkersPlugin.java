package knightminer.metalborn.plugin.tinkers;

import knightminer.metalborn.core.Registration;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;

/** Plugin for Tinkers' Construct additional features */
public class TinkersPlugin {
  private TinkersPlugin() {}

  /**
   * Called on construct to load the Tinkers' Construct plugin.
   * Should only be called if Tinkers' is loaded to prevent invalid class loading.
   */
  @Internal
  public static void init() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(TinkersPlugin::register);
  }

  /** Handles any Tinkers' specific registrations */
  private static void register(RegisterEvent event) {
    if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
      ForgeRegistries.RECIPE_SERIALIZERS.register(Registration.METAL_CASTING_BASIN.getId(), LoadableRecipeSerializer.of(MetalCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_BASIN));
      ForgeRegistries.RECIPE_SERIALIZERS.register(Registration.METAL_CASTING_TABLE.getId(), LoadableRecipeSerializer.of(MetalCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_TABLE));
      ForgeRegistries.RECIPE_SERIALIZERS.register(Registration.METAL_MELTING.getId(), LoadableRecipeSerializer.of(MetalMeltingRecipe.LOADER));
    }
  }
}

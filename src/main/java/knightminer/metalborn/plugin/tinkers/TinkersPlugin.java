package knightminer.metalborn.plugin.tinkers;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.ApiStatus.Internal;

/** Plugin for Tinkers' Construct additional features */
public class TinkersPlugin {
  private TinkersPlugin() {}

  /**
   * Called on construct to load the Tinkers' Construct plugin.
   * Should only be called if Tinkers' is loaded to prevent invalid class loading.
   */
  @Internal
  public static void init(IEventBus bus) {
    // bus.addListener(TinkersPlugin::registerSerializers);
  }

  /* FIXME: update when Tinkers' ports
  /** Handles any Tinkers' specific registrations *//*
  private static void registerSerializers(RegisterEvent event) {
    if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
      register(Registration.METAL_CASTING_BASIN, LoadableRecipeSerializer.of(MetalCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_BASIN));
      register(Registration.METAL_CASTING_TABLE, LoadableRecipeSerializer.of(MetalCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_TABLE));
      register(Registration.METAL_MELTING, LoadableRecipeSerializer.of(MetalMeltingRecipe.LOADER));
      register(Registration.COPY_METAL_BASIN, LoadableRecipeSerializer.of(CopyMetalCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_BASIN));
      register(Registration.COPY_METAL_TABLE, LoadableRecipeSerializer.of(CopyMetalCastingRecipe.LOADER, TinkerRecipeTypes.CASTING_TABLE));
    }
  }
  */

  /** Registers the given serializer using the given ID. */
  private static void register(DeferredHolder<?,?> id, RecipeSerializer<?> serializer) {
    Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id.getId(), serializer);
  }
}

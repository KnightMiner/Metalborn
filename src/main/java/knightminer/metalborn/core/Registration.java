package knightminer.metalborn.core;


import knightminer.metalborn.metal.effects.AttributeMetalEffect;
import knightminer.metalborn.metal.effects.ExperienceMetalEffect;
import knightminer.metalborn.metal.effects.HealMetalEffect;
import knightminer.metalborn.metal.effects.MetalEffect;
import knightminer.metalborn.metal.effects.RangeMetalEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus.Internal;

import static knightminer.metalborn.Metalborn.resource;

/** Handles any in code registrations */
public class Registration {
  private Registration() {}

  /** Initializes the registration event busses */
  @Internal
  public static void init() {
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(Registration::registerMisc);
  }

  /** Damage type for metal damaging the player, used in gold's effect */
  public static final ResourceKey<DamageType> METAL_HURT = ResourceKey.create(Registries.DAMAGE_TYPE, resource("metal_hurt"));

  /** Registers any relevant static entries */
  private static void registerMisc(RegisterEvent event) {
    if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
      MetalEffect.REGISTRY.register(resource("range"), RangeMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("attribute"), AttributeMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("heal"), HealMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("experience"), ExperienceMetalEffect.LOADER);
    }
  }
}

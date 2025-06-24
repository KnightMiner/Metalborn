package knightminer.metalborn.data;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import static knightminer.metalborn.Metalborn.resource;

/** Any resource keys used in the mod exclusively for datagen */
public class MetalbornKeys {
  public static final ResourceKey<ConfiguredFeature<?, ?>> TIN_ORE = create(Registries.CONFIGURED_FEATURE, "tin_ore");
  public static final ResourceKey<ConfiguredFeature<?, ?>> TIN_ORE_SMALL = create(Registries.CONFIGURED_FEATURE, "tin_ore_small");
  public static final ResourceKey<PlacedFeature> TIN_ORE_UPPER = create(Registries.PLACED_FEATURE, "tin_ore_upper");
  public static final ResourceKey<PlacedFeature> TIN_ORE_MIDDLE = create(Registries.PLACED_FEATURE, "tin_ore_middle");
  public static final ResourceKey<PlacedFeature> PLACED_TIN_ORE_SMALL = create(Registries.PLACED_FEATURE, "tin_ore_small");
  public static final ResourceKey<BiomeModifier> OVERWORLD_ORES = create(ForgeRegistries.Keys.BIOME_MODIFIERS, "overworld_ores");

  private static <T> ResourceKey<T> create(ResourceKey<? extends Registry<T>> registry, String name) {
    return ResourceKey.create(registry, resource(name));
  }
}

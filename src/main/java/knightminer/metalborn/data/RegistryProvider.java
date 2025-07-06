package knightminer.metalborn.data;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers.AddFeaturesBiomeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static knightminer.metalborn.Metalborn.prefix;

/** Data provider for all datapack registries */
@Internal
public class RegistryProvider {
  /** Prepares all our datapack registry builders */
  public static DatapackBuiltinEntriesProvider prepare(PackOutput packOutput, CompletableFuture<Provider> lookupProvider) {
    RegistrySetBuilder builder = new RegistrySetBuilder();
    builder.add(Registries.DAMAGE_TYPE, RegistryProvider::damageSources);
    builder.add(Registries.CONFIGURED_FEATURE, RegistryProvider::configuredFeatures);
    builder.add(Registries.PLACED_FEATURE, RegistryProvider::placedFeatures);
    builder.add(ForgeRegistries.Keys.BIOME_MODIFIERS, RegistryProvider::biomeModifiers);
    return new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, builder, Set.of(Metalborn.MOD_ID));
  }

  /** Adds damage sources */
  private static void damageSources(BootstapContext<DamageType> context) {
    context.register(Registration.METAL_HURT, new DamageType(prefix("metal_hurt"), DamageScaling.NEVER, 0f, DamageEffects.HURT));
    context.register(Registration.ADD_SPIKE, new DamageType(prefix("add_spike"), DamageScaling.NEVER, 0.1f, DamageEffects.THORNS));
    context.register(Registration.MAKE_SPIKE, new DamageType(prefix("make_spike"), DamageScaling.NEVER, 1f, DamageEffects.HURT));
    context.register(Registration.MELEE_HEAT, new DamageType(prefix("melee_heat"), DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1f, DamageEffects.BURNING));
  }

  private static void configuredFeatures(BootstapContext<ConfiguredFeature<?, ?>> context) {
    List<TargetBlockState> tinOres = List.of(
      OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), Registration.TIN_ORE.get().defaultBlockState()),
      OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), Registration.DEEPSLATE_TIN_ORE.get().defaultBlockState()));
    FeatureUtils.register(context, MetalbornKeys.TIN_ORE, Feature.ORE, new OreConfiguration(tinOres, 9));
    FeatureUtils.register(context, MetalbornKeys.TIN_ORE_SMALL, Feature.ORE, new OreConfiguration(tinOres, 4));
  }

  private static List<PlacementModifier> commonOrePlacement(int pCount, PlacementModifier pHeightRange) {
    return List.of(CountPlacement.of(pCount), InSquarePlacement.spread(), pHeightRange, BiomeFilter.biome());
  }

  private static void placedFeatures(BootstapContext<PlacedFeature> context) {
    HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(Registries.CONFIGURED_FEATURE);
    Holder<ConfiguredFeature<?, ?>> tinOre = holdergetter.getOrThrow(MetalbornKeys.TIN_ORE);
    Holder<ConfiguredFeature<?, ?>> smallTinOre = holdergetter.getOrThrow(MetalbornKeys.TIN_ORE_SMALL);
    PlacementUtils.register(context, MetalbornKeys.TIN_ORE_UPPER, tinOre, commonOrePlacement(20, HeightRangePlacement.triangle(VerticalAnchor.absolute(80), VerticalAnchor.absolute(384))));
    PlacementUtils.register(context, MetalbornKeys.TIN_ORE_MIDDLE, tinOre, commonOrePlacement(3, HeightRangePlacement.triangle(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(56))));
    PlacementUtils.register(context, MetalbornKeys.PLACED_TIN_ORE_SMALL, smallTinOre, commonOrePlacement(3, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(72))));  }

  private static void biomeModifiers(BootstapContext<BiomeModifier> context) {
    HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
    HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
    context.register(MetalbornKeys.OVERWORLD_ORES, new AddFeaturesBiomeModifier(biomes.getOrThrow(BiomeTags.IS_OVERWORLD), HolderSet.direct(
      placed.getOrThrow(MetalbornKeys.TIN_ORE_UPPER),
      placed.getOrThrow(MetalbornKeys.TIN_ORE_MIDDLE),
      placed.getOrThrow(MetalbornKeys.PLACED_TIN_ORE_SMALL)
    ), Decoration.UNDERGROUND_ORES));
  }
}

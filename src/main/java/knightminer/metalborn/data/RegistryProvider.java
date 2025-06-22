package knightminer.metalborn.data;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import org.jetbrains.annotations.ApiStatus.Internal;

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
    return new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, builder, Set.of(Metalborn.MOD_ID));
  }

  /** Adds damage sources */
  private static void damageSources(BootstapContext<DamageType> context) {
    context.register(Registration.METAL_HURT, new DamageType(prefix("metal_hurt"), DamageScaling.NEVER, 0f, DamageEffects.HURT));
  }
}

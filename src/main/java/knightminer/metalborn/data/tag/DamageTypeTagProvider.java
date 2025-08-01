package knightminer.metalborn.data.tag;

import knightminer.metalborn.Metalborn;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static knightminer.metalborn.core.Registration.ADD_SPIKE;
import static knightminer.metalborn.core.Registration.MAKE_SPIKE;
import static knightminer.metalborn.core.Registration.MELEE_HEAT;
import static knightminer.metalborn.core.Registration.METAL_HURT;

/** Provider for metalborn damage type tags */
@Internal
public class DamageTypeTagProvider extends DamageTypeTagsProvider {
  public DamageTypeTagProvider(PackOutput packOutput, CompletableFuture<Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
    super(packOutput, lookupProvider, Metalborn.MOD_ID, existingFileHelper);
  }

  @Override
  public String getName() {
    return "Metalborn DamageType tags";
  }

  @Override
  protected void addTags(Provider provider) {
    tag(DamageTypeTags.BYPASSES_ARMOR).add(METAL_HURT, ADD_SPIKE, MAKE_SPIKE);
    tag(DamageTypeTags.BYPASSES_COOLDOWN).add(METAL_HURT, ADD_SPIKE, MAKE_SPIKE);
    tag(DamageTypeTags.BYPASSES_EFFECTS).add(METAL_HURT, ADD_SPIKE, MAKE_SPIKE);
    tag(DamageTypeTags.IS_FIRE).add(MELEE_HEAT);
    tag(TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Metalborn.TINKERS, "protection/melee"))).add(MELEE_HEAT);
  }
}

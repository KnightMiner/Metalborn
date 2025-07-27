package knightminer.metalborn.data.tag;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.data.MetalIds;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.Mantle;

import java.util.concurrent.CompletableFuture;

/** Adds tags for entities */
public class EntityTagProvider extends EntityTypeTagsProvider {
  public EntityTagProvider(PackOutput packOutput, CompletableFuture<Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
    super(packOutput, lookupProvider, Metalborn.MOD_ID, existingFileHelper);
  }

  @Override
  protected void addTags(Provider pProvider) {
    // physical
    tag(MetalIds.iron).add(EntityType.HOGLIN, EntityType.RAVAGER);
    tag(MetalIds.steel).add(EntityType.SPIDER, EntityType.CAVE_SPIDER);
    tag(MetalIds.tin).add(EntityType.SKELETON, EntityType.STRAY);
    tag(MetalIds.pewter).add(EntityType.CREEPER);
    // cognitive
    tag(MetalIds.copper).add(EntityType.SLIME).addOptionalTag(Mantle.commonResource("slimes"));
    tag(MetalIds.bronze).add(EntityType.ENDERMAN);
    // hybrid
    tag(MetalIds.gold).add(EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.ZOMBIFIED_PIGLIN);
    tag(MetalIds.roseGold).add(EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK);
    // spiritual
    tag(MetalIds.netherite).add(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.ILLUSIONER, EntityType.WITCH);
    // compat cognitive
    tag(MetalIds.silver).add(EntityType.SILVERFISH);
    tag(MetalIds.electrum).add(EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN);
    // compat hybrid
    tag(MetalIds.zinc).add(EntityType.DROWNED);
    tag(MetalIds.nickel).add(EntityType.DROWNED);
    tag(MetalIds.brass).add(EntityType.BLAZE);
    tag(MetalIds.constantan).add(EntityType.BLAZE);
  }

  /** Creates a tag for a metal */
  public IntrinsicTagAppender<EntityType<?>> tag(MetalId metal) {
    return tag(MetalId.getTargetTag(metal));
  }
}

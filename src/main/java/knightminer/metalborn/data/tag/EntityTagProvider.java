package knightminer.metalborn.data.tag;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.data.MetalIds;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static slimeknights.mantle.Mantle.commonResource;

/** Adds tags for entities */
public class EntityTagProvider extends EntityTypeTagsProvider {
  public EntityTagProvider(PackOutput packOutput, CompletableFuture<Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
    super(packOutput, lookupProvider, Metalborn.MOD_ID, existingFileHelper);
  }

  @Override
  protected void addTags(Provider pProvider) {
    tag(MetalbornTags.Entities.BLAZES).add(EntityType.BLAZE);
    tag(MetalbornTags.Entities.ZOMBIES).add(EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK);
    tag(MetalbornTags.Entities.PIGLINS).add(EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.ZOMBIFIED_PIGLIN);
    tag(MetalbornTags.Entities.ILLAGERS).add(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.ILLUSIONER, EntityType.WITCH);
    tag(MetalbornTags.Entities.SPIDERS).add(EntityType.SPIDER, EntityType.CAVE_SPIDER);
    tag(MetalbornTags.Entities.SILVERFISH).add(EntityType.SILVERFISH);
    tag(MetalbornTags.Entities.GUARDIANS).add(EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN);

    // physical
    tag(MetalIds.iron).add(EntityType.HOGLIN, EntityType.RAVAGER);
    tag(MetalIds.steel).addTag(MetalbornTags.Entities.SPIDERS);
    tag(MetalIds.tin).addTag(EntityTypeTags.SKELETONS);
    tag(MetalIds.pewter).add(EntityType.CREEPER).addOptional(commonResource("creepers"));
    // cognitive
    tag(MetalIds.copper).add(EntityType.SLIME).addOptionalTag(commonResource("slimes"));
    tag(MetalIds.bronze).add(EntityType.ENDERMAN).addOptionalTag(commonResource("endermen"));
    // hybrid
    tag(MetalIds.gold).addTag(MetalbornTags.Entities.PIGLINS);
    tag(MetalIds.roseGold).addTag(MetalbornTags.Entities.ZOMBIES).remove(EntityType.DROWNED);
    tag(MetalIds.bendalloy).addTag(MetalbornTags.Entities.ZOMBIES).remove(EntityType.DROWNED);
    // spiritual
    tag(MetalIds.emerald).addTag(MetalbornTags.Entities.ILLAGERS);
    tag(MetalIds.chromium).addTag(MetalbornTags.Entities.ILLAGERS);
    tag(MetalIds.netherite).add(EntityType.MAGMA_CUBE);
    // compat cognitive
    tag(MetalIds.silver).addTag(MetalbornTags.Entities.SILVERFISH);
    tag(MetalIds.cadmium).addTag(MetalbornTags.Entities.SILVERFISH);
    tag(MetalIds.electrum).addTag(MetalbornTags.Entities.GUARDIANS);
    // compat hybrid
    tag(MetalIds.zinc).add(EntityType.DROWNED);
    tag(MetalIds.nickel).add(EntityType.DROWNED);
    tag(MetalIds.brass).addTag(MetalbornTags.Entities.BLAZES);
    tag(MetalIds.constantan).addTag(MetalbornTags.Entities.BLAZES);
  }

  /** Creates a tag for a metal */
  public IntrinsicTagAppender<EntityType<?>> tag(MetalId metal) {
    return tag(MetalId.getTargetTag(metal));
  }
}

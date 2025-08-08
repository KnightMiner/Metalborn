package knightminer.metalborn.data.tag;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.data.MetalIds;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
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
    tag(MetalbornTags.Entities.ZOMBIES).add(EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK);
    tag(MetalbornTags.Entities.PIGLINS).add(EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.ZOMBIFIED_PIGLIN);
    tag(MetalbornTags.Entities.ILLAGERS).add(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.ILLUSIONER, EntityType.WITCH);

    // physical
    tag(MetalIds.iron).add(EntityType.HOGLIN, EntityType.RAVAGER);
    tag(MetalIds.steel).add(EntityType.SPIDER, EntityType.CAVE_SPIDER);
    tag(MetalIds.tin).addTag(EntityTypeTags.SKELETONS);
    tag(MetalIds.pewter).add(EntityType.CREEPER);
    // cognitive
    tag(MetalIds.copper).add(EntityType.SLIME).addOptionalTag(Mantle.commonResource("slimes"));
    tag(MetalIds.bronze).add(EntityType.ENDERMAN);
    // hybrid
    tag(MetalIds.gold).addTag(MetalbornTags.Entities.PIGLINS);
    tag(MetalIds.roseGold).addTag(MetalbornTags.Entities.ZOMBIES);
    tag(MetalIds.bendalloy).addTag(MetalbornTags.Entities.ZOMBIES);
    // spiritual
    tag(MetalIds.netherite).addTag(MetalbornTags.Entities.ILLAGERS);
    tag(MetalIds.chromium).addTag(MetalbornTags.Entities.ILLAGERS);
    // compat cognitive
    tag(MetalIds.silver).add(EntityType.SILVERFISH);
    tag(MetalIds.cadmium).add(EntityType.SILVERFISH);
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

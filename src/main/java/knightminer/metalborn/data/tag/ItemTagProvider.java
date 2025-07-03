package knightminer.metalborn.data.tag;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.data.MetalIds;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.util.CastItemObject;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.registration.object.MetalItemObject;

import java.util.concurrent.CompletableFuture;

import static slimeknights.mantle.Mantle.commonResource;

/** Registers item tags for Metalborn */
@SuppressWarnings("unchecked")
public class ItemTagProvider extends ItemTagsProvider {
  public ItemTagProvider(PackOutput packOutput, CompletableFuture<Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
    super(packOutput, lookupProvider, blockTags, Metalborn.MOD_ID, existingFileHelper);
  }

  @Override
  protected void addTags(Provider pProvider) {
    copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);
    copy(Tags.Blocks.ORES, Tags.Items.ORES);

    // inventory
    tag(Registration.BRACERS).add(Registration.BRACER.get(), Registration.INVESTITURE_BRACER.get());
    tag(Registration.RINGS).add(Registration.RING.get(), Registration.INVESTITURE_RING.get(), Registration.UNSEALED_RING.get());
    tag(Registration.SPIKES).add(Registration.SPIKE.get());

    metal(Registration.TIN);
    metal(Registration.PEWTER);
    metal(Registration.STEEL);
    metal(Registration.BRONZE);
    metal(Registration.ROSE_GOLD);
    tag(MetalbornTags.Items.COPPER_NUGGETS).add(Registration.COPPER_NUGGET.get());
    tag(MetalbornTags.Items.NETHERITE_NUGGETS).add(Registration.NETHERITE_NUGGET.get());
    tag(Tags.Items.NUGGETS).addTags(MetalbornTags.Items.COPPER_NUGGETS, MetalbornTags.Items.NETHERITE_NUGGETS);

    // tin ore
    copy(MetalbornTags.Blocks.TIN_ORE, MetalbornTags.Items.TIN_ORE);
    copy(MetalbornTags.Blocks.RAW_TIN_BLOCK, MetalbornTags.Items.RAW_TIN_BLOCK);
    tag(MetalbornTags.Items.RAW_TIN).add(Registration.RAW_TIN.get());
    tag(Tags.Items.RAW_MATERIALS).addTag(MetalbornTags.Items.RAW_TIN);

    // alloy inputs
    // base metals
    addIngotLike(MetalIds.iron, true);
    addIngotLike(MetalIds.copper, true);
    addIngotLike(MetalIds.gold, true);
    addIngotLike(MetalIds.tin, true);
    // compat
    addIngotLike(MetalIds.zinc, false);
    addIngotLike("nickel", false);
    addIngotLike(MetalIds.silver, false);
    addIngotLike(MetalIds.cobalt, false);

    // netherite scrap is weird as its not fully tagged, but we can deal with that
    tag(MetalbornTags.Items.SCRAP_LIKE).add(Items.NETHERITE_SCRAP).addTag(Tags.Items.ORES_NETHERITE_SCRAP);
    tag(MetalbornTags.Items.QUARTZ_LIKE).add(Items.QUARTZ, Items.NETHER_QUARTZ_ORE);

    // tinkers compat - casts
    addCast(Registration.RING_CAST);
    addCast(Registration.BRACER_CAST);
    addCast(Registration.SPIKE_CAST);
  }

  /** Adds all relevant tags to a metal */
  private void metal(MetalItemObject metal) {
    copy(metal.getBlockTag(), metal.getBlockItemTag());
    tag(metal.getIngotTag()).add(metal.getIngot());
    tag(metal.getNuggetTag()).add(metal.getNugget());
    tag(Tags.Items.INGOTS).addTag(metal.getIngotTag());
    tag(Tags.Items.NUGGETS).addTag(metal.getNuggetTag());
  }

  /** Creates a tag for an ingot-like input, used in alloying */
  @SuppressWarnings("SameParameterValue")
  private void addIngotLike(MetalId metal, boolean required) {
    addIngotLike(metal.getPath(), required);
  }

  /** Creates a tag for an ingot-like input, used in alloying */
  private void addIngotLike(String path, boolean required) {
    ResourceLocation ingot = commonResource("ingots/" + path);
    ResourceLocation rawOre = commonResource("raw_materials/" + path);
    ResourceLocation ore = commonResource("ores/" + path);
    TagKey<Item> tag = ItemTags.create(Metalborn.resource("ingot_like/" + path));
    if (required) {
      tag(tag).addTags(
        ItemTags.create(ingot),
        ItemTags.create(rawOre),
        ItemTags.create(ore)
      );
    } else {
      tag(tag)
        .addOptionalTag(ingot)
        .addOptionalTag(rawOre)
        .addOptionalTag(ore);
    }
  }

  /** Adds all tags for a cast */
  private void addCast(CastItemObject cast) {
    // material tags
    tag(MetalbornTags.Items.GOLD_CASTS).add(cast.get());
    tag(MetalbornTags.Items.SAND_CASTS).add(cast.getSand());
    tag(MetalbornTags.Items.RED_SAND_CASTS).add(cast.getRedSand());
    // type tags
    tag(MetalbornTags.Items.SINGLE_USE_CASTS).addTag(cast.getSingleUseTag());
    tag(MetalbornTags.Items.MULTI_USE_CASTS).addTag(cast.getMultiUseTag());
    // local tags
    tag(cast.getSingleUseTag()).add(cast.getSand(), cast.getRedSand());
    tag(cast.getMultiUseTag()).add(cast.get());
  }
}

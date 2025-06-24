package knightminer.metalborn.data.tag;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.registration.object.MetalItemObject;

import java.util.concurrent.CompletableFuture;

/** Registers any relevant tags for the mod */
public class BlockTagProvider extends BlockTagsProvider {
  public BlockTagProvider(PackOutput output, CompletableFuture<Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
    super(output, lookupProvider, Metalborn.MOD_ID, existingFileHelper);
  }

  @Override
  protected void addTags(Provider pProvider) {
    metal(Registration.TIN, BlockTags.NEEDS_STONE_TOOL, true);
    metal(Registration.PEWTER, BlockTags.NEEDS_IRON_TOOL, true);
    metal(Registration.STEEL, BlockTags.NEEDS_IRON_TOOL, true);
    metal(Registration.BRONZE, BlockTags.NEEDS_IRON_TOOL, false);
    metal(Registration.ROSE_GOLD, BlockTags.NEEDS_IRON_TOOL, false);
  }

  /** Adds a metal block */
  private void metal(MetalItemObject metal, TagKey<Block> tier, boolean beacon) {
    Block block = metal.get();
    tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
    tag(metal.getBlockTag()).add(block);
    tag(Tags.Blocks.STORAGE_BLOCKS).addTag(metal.getBlockTag());
    tag(tier).add(block);
    if (beacon) {
      tag(BlockTags.BEACON_BASE_BLOCKS).add(block);
    }
  }
}

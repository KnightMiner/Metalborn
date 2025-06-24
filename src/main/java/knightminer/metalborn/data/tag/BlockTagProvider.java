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
import java.util.function.Supplier;

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

    // tin ore
    pickaxe(Registration.TIN_ORE, BlockTags.NEEDS_STONE_TOOL);
    pickaxe(Registration.DEEPSLATE_TIN_ORE, BlockTags.NEEDS_STONE_TOOL);
    tag(MetalbornTags.Blocks.TIN_ORE).add(Registration.TIN_ORE.get(), Registration.DEEPSLATE_TIN_ORE.get());
    tag(Tags.Blocks.ORE_RATES_SINGULAR).add(Registration.TIN_ORE.get(), Registration.DEEPSLATE_TIN_ORE.get());
    tag(Tags.Blocks.ORES_IN_GROUND_STONE).add(Registration.TIN_ORE.get());
    tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).add(Registration.DEEPSLATE_TIN_ORE.get());
    tag(Tags.Blocks.ORE_RATES_SINGULAR).add(Registration.TIN_ORE.get(), Registration.DEEPSLATE_TIN_ORE.get());
    tag(Tags.Blocks.ORES).addTag(MetalbornTags.Blocks.TIN_ORE);
    // raw tin block
    pickaxe(Registration.RAW_TIN_BLOCK, BlockTags.NEEDS_STONE_TOOL);
    tag(MetalbornTags.Blocks.RAW_TIN_BLOCK).add(Registration.RAW_TIN_BLOCK.get());
    tag(Tags.Blocks.STORAGE_BLOCKS).addTag(MetalbornTags.Blocks.RAW_TIN_BLOCK);
  }

  /** Adds a pickaxe block */
  private void pickaxe(Supplier<? extends Block> object, TagKey<Block> tier) {
    Block block = object.get();
    tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
    tag(tier).add(block);
  }

  /** Adds a metal block */
  private void metal(MetalItemObject metal, TagKey<Block> tier, boolean beacon) {
    pickaxe(metal, tier);
    Block block = metal.get();
    tag(metal.getBlockTag()).add(block);
    tag(Tags.Blocks.STORAGE_BLOCKS).addTag(metal.getBlockTag());
    if (beacon) {
      tag(BlockTags.BEACON_BASE_BLOCKS).add(block);
    }
  }
}

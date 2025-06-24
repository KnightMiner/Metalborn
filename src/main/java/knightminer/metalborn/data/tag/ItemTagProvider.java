package knightminer.metalborn.data.tag;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.registration.object.MetalItemObject;

import java.util.concurrent.CompletableFuture;

/** Registers item tags for Metalborn */
public class ItemTagProvider extends ItemTagsProvider {
  public ItemTagProvider(PackOutput p_275204_, CompletableFuture<Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
    super(p_275204_, lookupProvider, blockTags, Metalborn.MOD_ID, existingFileHelper);
  }

  @Override
  protected void addTags(Provider pProvider) {
    copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);
    metal(Registration.TIN);
    metal(Registration.PEWTER);
    metal(Registration.STEEL);
    metal(Registration.BRONZE);
    metal(Registration.ROSE_GOLD);
    tag(MetalbornTags.Items.COPPER_NUGGETS).add(Registration.COPPER_NUGGET.get());
    tag(Tags.Items.NUGGETS).addTag(MetalbornTags.Items.COPPER_NUGGETS);
  }

  /** Adds all relevant tags to a metal */
  private void metal(MetalItemObject metal) {
    copy(metal.getBlockTag(), metal.getBlockItemTag());
    tag(metal.getIngotTag()).add(metal.getIngot());
    tag(metal.getNuggetTag()).add(metal.getNugget());
    tag(Tags.Items.INGOTS).addTag(metal.getIngotTag());
    tag(Tags.Items.NUGGETS).addTag(metal.getNuggetTag());
  }
}

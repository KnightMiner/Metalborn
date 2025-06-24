package knightminer.metalborn.data.client;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.data.MetalIds;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.mantle.registration.object.ItemObject;

/** Provider for block models in Metalborn */
public class BlockModelProvider extends BlockStateProvider {
  public BlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
    super(output, Metalborn.MOD_ID, existingFileHelper);
  }

  @Override
  protected void registerStatesAndModels() {
    storage(Registration.TIN, MetalIds.tin);
    storage(Registration.PEWTER, MetalIds.pewter);
    storage(Registration.STEEL, MetalIds.steel);
    storage(Registration.BRONZE, MetalIds.bronze);
    storage(Registration.ROSE_GOLD, MetalIds.roseGold);
  }

  /** Adds a storage block with models under storage */
  private void storage(ItemObject<? extends Block> block, MetalId metal) {
    String path = metal.getPath();
    ModelFile model = models().cubeAll(path, Metalborn.resource(ModelProvider.BLOCK_FOLDER + '/' + path));
    simpleBlock(block.get(), model);
    simpleBlockItem(block.get(), model);
  }
}

package knightminer.metalborn.data.client;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.block.ForgeBlock;
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

import static knightminer.metalborn.Metalborn.resource;

/** Provider for block models in Metalborn */
public class BlockModelProvider extends BlockStateProvider {
  public BlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
    super(output, Metalborn.MOD_ID, existingFileHelper);
  }

  @Override
  protected void registerStatesAndModels() {
    basic(Registration.TIN_ORE, "tin_ore");
    basic(Registration.DEEPSLATE_TIN_ORE, "deepslate_tin_ore");
    basic(Registration.RAW_TIN_BLOCK, "raw_tin");
    storage(Registration.TIN, MetalIds.tin);
    storage(Registration.PEWTER, MetalIds.pewter);
    storage(Registration.STEEL, MetalIds.steel);
    storage(Registration.BRONZE, MetalIds.bronze);
    storage(Registration.ROSE_GOLD, MetalIds.roseGold);
    storage(Registration.NICROSIL, MetalIds.nicrosil);
    ModelFile forge = models().orientableWithBottom(
      "forge",
      resource("block/forge/side"),
      resource("block/forge/front"),
      resource("block/forge/bottom"),
      resource("block/forge/top")
    );
    ModelFile forgeLit = models()
      .getBuilder("forge_lit")
      .parent(forge)
      .texture("front", resource("block/forge/front_on"));
    horizontalBlock(Registration.FORGE.get(), state -> state.getValue(ForgeBlock.LIT) ? forgeLit : forge);
    simpleBlockItem(Registration.FORGE.get(), forge);
  }

  /** Adds a storage block with models under storage */
  private void basic(ItemObject<? extends Block> block, String path) {
    ModelFile model = models().cubeAll(path, resource(ModelProvider.BLOCK_FOLDER + '/' + path));
    simpleBlock(block.get(), model);
    simpleBlockItem(block.get(), model);
  }

  /** Adds a storage block with models under storage */
  private void storage(ItemObject<? extends Block> block, MetalId metal) {
    basic(block, metal.getPath());
  }
}

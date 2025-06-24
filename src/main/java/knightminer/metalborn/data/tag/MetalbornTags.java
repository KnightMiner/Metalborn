package knightminer.metalborn.data.tag;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static slimeknights.mantle.Mantle.commonResource;

/** Any tags defined by this mod that don't exist elsewhere */
public class MetalbornTags {
  public static class Blocks {
    // tin ore
    public static final TagKey<Block> TIN_ORE = common("ores/tin");
    public static final TagKey<Block> RAW_TIN_BLOCK = common("storage_blocks/raw_tin");

    private static TagKey<Block> common(String name) {
      return BlockTags.create(commonResource(name));
    }
  }

  public static class Items {
    public static final TagKey<Item> COPPER_NUGGETS = common("nuggets/copper");
    // tin ore
    public static final TagKey<Item> TIN_ORE = common("ores/tin");
    public static final TagKey<Item> RAW_TIN = common("raw_materials/tin");
    public static final TagKey<Item> RAW_TIN_BLOCK = common("storage_blocks/raw_tin");

    private static TagKey<Item> common(String name) {
      return ItemTags.create(commonResource(name));
    }
  }
}

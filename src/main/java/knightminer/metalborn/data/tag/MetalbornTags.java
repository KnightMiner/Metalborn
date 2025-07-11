package knightminer.metalborn.data.tag;

import knightminer.metalborn.Metalborn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static knightminer.metalborn.Metalborn.resource;
import static slimeknights.mantle.Mantle.commonResource;

/** Any tags defined by this mod that are used only in datagen */
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
    public static final TagKey<Item> BOOKS = common("books");
    public static final TagKey<Item> GUIDEBOOKS = common("books/guide");

    public static final TagKey<Item> COPPER_NUGGETS = common("nuggets/copper");
    public static final TagKey<Item> NETHERITE_NUGGETS = common("nuggets/netherite");
    // tin ore
    public static final TagKey<Item> TIN_ORE = common("ores/tin");
    public static final TagKey<Item> RAW_TIN = common("raw_materials/tin");
    public static final TagKey<Item> RAW_TIN_BLOCK = common("storage_blocks/raw_tin");

    // ingot-like tags - contains ingots, raw ores, and ore blocks
    public static final TagKey<Item> SCRAP_LIKE = local("ingot_like/netherite_scrap");
    public static final TagKey<Item> QUARTZ_LIKE = local("ingot_like/quartz");

    // Tinkers' cast tags
    public static final TagKey<Item> GOLD_CASTS = tinkers("casts/gold");
    public static final TagKey<Item> SAND_CASTS = tinkers("casts/sand");
    public static final TagKey<Item> RED_SAND_CASTS = tinkers("casts/red_sand");
    public static final TagKey<Item> SINGLE_USE_CASTS = tinkers("casts/single_use");
    public static final TagKey<Item> MULTI_USE_CASTS = tinkers("casts/multi_use");

    /** Creates a metalborn tag */
    private static TagKey<Item> local(String name) {
      return ItemTags.create(resource(name));
    }

    /** Creates a common namespace tag */
    private static TagKey<Item> common(String name) {
      return ItemTags.create(commonResource(name));
    }

    /** Creates a Tinkers' Construct compat tag. Used to avoid a datagen dependency */
    private static TagKey<Item> tinkers(String name) {
      return ItemTags.create(new ResourceLocation(Metalborn.TINKERS, name));
    }
  }
}

package knightminer.metalborn.data.tag;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static slimeknights.mantle.Mantle.commonResource;

/** Any tags defined by this mod that don't exist elsewhere */
public class MetalbornTags {
  public static class Items {
    public static final TagKey<Item> COPPER_NUGGETS = ItemTags.create(commonResource("nuggets/copper"));
  }
}

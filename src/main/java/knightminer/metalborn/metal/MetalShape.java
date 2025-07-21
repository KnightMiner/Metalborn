package knightminer.metalborn.metal;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;

/**
 * Helper to handle different crafting shapes of metals.
 * @see MetalPower#tag(MetalShape)
 */
public enum MetalShape {
  INGOT(Tags.Items.INGOTS),
  NUGGET(Tags.Items.NUGGETS);

  public static final EnumLoadable<MetalShape> LOADABLE = new EnumLoadable<>(MetalShape.class);

  private final TagKey<Item> tag;
  MetalShape(TagKey<Item> tag) {
    this.tag = tag;
  }

  /** Gets the root item tag for this shape */
  public TagKey<Item> getTag() {
    return tag;
  }
}

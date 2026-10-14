package knightminer.metalborn.metal;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.network.codec.MantleStreamCodecs;

import java.util.Locale;

/**
 * Helper to handle different crafting shapes of metals.
 * @see MetalPower#tag(MetalShape)
 */
public enum MetalShape implements StringRepresentable {
  INGOT(Tags.Items.INGOTS),
  NUGGET(Tags.Items.NUGGETS);

  public static final EnumLoadable<MetalShape> LOADABLE = new EnumLoadable<>(MetalShape.class);
  @SuppressWarnings("deprecation")  // then why is it new?
  public static final StringRepresentable.EnumCodec<MetalShape> CODEC = StringRepresentable.fromEnum(MetalShape::values);
  public static final StreamCodec<ByteBuf, MetalShape> STREAM_CODEC = MantleStreamCodecs.enumCodec(MetalShape.class, OutOfBoundsStrategy.WRAP);

  private final String name = name().toLowerCase(Locale.ROOT);
  private final TagKey<Item> tag;

  MetalShape(TagKey<Item> tag) {
    this.tag = tag;
  }

  /** Gets the root item tag for this shape */
  public TagKey<Item> getTag() {
    return tag;
  }


  @Override
  public String getSerializedName() {
    return name;
  }
}

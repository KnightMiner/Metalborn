package knightminer.metalborn.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.mantle.registration.object.MultiObject;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Compatability object for Tinkers' Construct support */
public class CastItemObject extends ItemObject<Item> implements MultiObject<Item> {
  private final ResourceLocation name;
  private final Supplier<? extends Item> sand;
  private final Supplier<? extends Item> redSand;
  private final TagKey<Item> singleUseTag;
  private final TagKey<Item> multiUseTag;

  public CastItemObject(ResourceLocation name, ItemObject<? extends Item> gold, Supplier<? extends Item> sand, Supplier<? extends Item> redSand) {
    super(gold);
    this.name = name;
    this.sand = sand;
    this.redSand = redSand;
    this.singleUseTag = makeTag("single_use");
    this.multiUseTag = makeTag("multi_use");
  }

  @Override
  public ResourceLocation getId() {
    return name;
  }

  /** Gets the tag for using this in a single use recipe */
  public TagKey<Item> getSingleUseTag() {
    return singleUseTag;
  }

  /** Gets the tag for using this in a multiuse recipe */
  public TagKey<Item> getMultiUseTag() {
    return multiUseTag;
  }

  /**
   * Gets the single use tag for this object
   * @return  Single use tag
   */
  protected TagKey<Item> makeTag(String type) {
    return TagKey.create(Registries.ITEM, new ResourceLocation(name.getNamespace(), "casts/" + name.getPath() + '_' + type));
  }

  /**
   * Gets the yellow sand variant
   * @return  Yellow sand variant
   */
  public Item getSand() {
    return Objects.requireNonNull(this.sand.get(), "CastItemObject missing sand");
  }

  /**
   * Gets the red sand variant
   * @return  Red sand variant
   */
  public Item getRedSand() {
    return Objects.requireNonNull(this.redSand.get(), "CastItemObject missing red sand");
  }

  @Override
  public List<Item> values() {
    return Arrays.asList(this.get(), this.getSand(), this.getRedSand());
  }

  @Override
  public void forEach(Consumer<? super Item> consumer) {
    consumer.accept(get());
    consumer.accept(getSand());
    consumer.accept(getRedSand());
  }
}

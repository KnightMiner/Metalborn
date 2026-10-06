package knightminer.metalborn.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.mantle.registration.object.MultiObject;

import java.util.Collection;
import java.util.List;

/** Compatability object for Tinkers' Construct support */
@SuppressWarnings("unused")
public class CastItemObject extends ItemObject<Item,Item> implements MultiObject.Holders<Item> {
  private final ResourceLocation name;
  private final Holder<Item> sand;
  private final Holder<Item> redSand;
  private final TagKey<Item> singleUseTag;
  private final TagKey<Item> multiUseTag;

  public CastItemObject(ResourceLocation name, DeferredItem<Item> gold, Holder<Item> sand, Holder<Item> redSand) {
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
    return TagKey.create(Registries.ITEM, name.withPath("casts/" + name.getPath() + '_' + type));
  }

  /** {@return yellow sand variant} */
  public Item getSand() {
    return sand.value();
  }

  /** {@return yellow sand holder} */
  public Holder<Item> getSandHolder() {
    return sand;
  }

  /** {@return red sand variant} */
  public Item getRedSand() {
    return redSand.value();
  }

  /** {@return red sand holder} */
  public Holder<Item> getRedSandHolder() {
    return redSand;
  }

  @Override
  public Collection<Holder<Item>> holders() {
    return List.of(holder, sand, redSand);
  }
}

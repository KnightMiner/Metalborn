package knightminer.metalborn.metal;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import knightminer.metalborn.Metalborn;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.primitive.ResourceLocationLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;

/** Represents the ID of a metal. */
public class MetalId extends ResourceLocation {
  /** Metal ID for indicating something has no metal */
  public static final MetalId NONE = new MetalId(Metalborn.MOD_ID, "none");
  /** Loadable instance for parsing */
  public static final StringLoadable<MetalId> LOADABLE = ResourceLocationLoadable.DEFAULT.mapChecked((loc, error) -> new MetalId(loc), (id, error) -> id);
  // codecs
  public static final Codec<MetalId> CODEC = ResourceLocation.CODEC.xmap(MetalId::new, id -> id);
  public static final StreamCodec<ByteBuf,MetalId> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(MetalId::new, id -> id);


  /** Non-validating constructor */
  private MetalId(String namespace, String path) {
    super(namespace, path);
  }

  /** Copy constructor */
  public MetalId(ResourceLocation location) {
    super(location.getNamespace(), location.getPath());
  }

  /** Constructor which validates from a namespace and path */
  public static MetalId fromNamespaceAndPath(String namespace, String path) {
    return new MetalId(assertValidNamespace(namespace, path), assertValidPath(namespace, path));
  }

  /** Parses the given location using the given separator */
  public static MetalId bySeparator(String location, char seperator) {
    return new MetalId(ResourceLocation.bySeparator(location, seperator));
  }

  /** Attempts to parse a metal ID from a string with the given separator, returning {@code null} if invalid. */
  @Nullable
  public static MetalId tryBySeparator(String location, char seperator) {
    ResourceLocation parsed = ResourceLocation.tryBySeparator(location, seperator);
    return parsed == null ? null : new MetalId(parsed);
  }

  /** Attempts to parse a metal ID from a string, throwing {@link net.minecraft.ResourceLocationException} if invalid. */
  public static MetalId parse(String location) {
    return bySeparator(location, ':');
  }

  /** Attempts to parse a metal ID from a string, returning {@code null} if invalid. */
  @Nullable
  public static MetalId tryParse(String location) {
    return tryBySeparator(location, ':');
  }

  @Nullable
  public static MetalId tryBuild(String namespace, String path) {
    return isValidNamespace(namespace) && isValidPath(path) ? new MetalId(namespace, path) : null;
  }


  /** Checks if this metal ID is not equal to {@link #NONE} */
  public boolean isPresent() {
    // directly comparing the two strings as it saves an instanceof checks
    return this != NONE && !(getPath().equals(NONE.getPath()) && getNamespace().equals(NONE.getNamespace()));
  }


  /** Gets the translation key for this metal */
  public String getTranslationKey() {
    return Util.makeDescriptionId("metal_power", this);
  }

  /** Gets the display name for this ID */
  public MutableComponent getName() {
    return Component.translatable(this.getTranslationKey());
  }

  /** Gets the display name what is stored with feruchemy */
  public MutableComponent getStores() {
    return Component.translatable(this.getTranslationKey() + ".stores");
  }

  /** Gets the display name what entites are targeted by this spike */
  public MutableComponent getTarget() {
    return Component.translatable(this.getTranslationKey() + ".target");
  }

  /** Gets the display name for the ferring */
  public MutableComponent getFerring() {
    return Component.translatable(this.getTranslationKey() + ".ferring");
  }

  /** Creates a target tag for the given metal ID */
  public static TagKey<EntityType<?>> getTargetTag(ResourceLocation metal) {
    return TagKey.create(Registries.ENTITY_TYPE, metal.withPrefix("targets/"));
  }
}

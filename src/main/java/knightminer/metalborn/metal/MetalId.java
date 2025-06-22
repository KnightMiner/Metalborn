package knightminer.metalborn.metal;

import knightminer.metalborn.Metalborn;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;

/** Represents the ID of a metal. */
public class MetalId extends ResourceLocation {
  /** Metal ID for indicating something has no metal */
  public static final MetalId NONE = new MetalId(Metalborn.MOD_ID, "none");
  /** Loadable instance for parsing */
  public static final StringLoadable<MetalId> LOADABLE = Loadables.RESOURCE_LOCATION.xmap((loc, error) -> new MetalId(loc), (id, error) -> id);

  /** Non-validating constructor */
  private MetalId(String namespace, String path, @Nullable ResourceLocation.Dummy dummy) {
    super(namespace, path, dummy);
  }

  /** Constructor which validates from a namespace and path */
  public MetalId(String namespace, String path) {
    super(namespace, path);
  }

  /** Constructor which validates from a raw string location */
  public MetalId(String location) {
    super(location);
  }

  /** Copy constructor from a resource location */
  public MetalId(ResourceLocation location) {
    super(location.getNamespace(), location.getPath(), null);
  }

  /** Gets the translation key for this metal */
  public String getTranslationKey() {
    return Util.makeDescriptionId("metal_power", this);
  }

  /** Gets the display name for this ID */
  public Component getName() {
    return Component.translatable(this.getTranslationKey());
  }


  /** Attempts to parse the given string ID as a metal ID, returning null if invalid. */
  @Nullable
  public static MetalId tryParse(String string) {
    String[] parts = decompose(string, ':');
    return tryBuild(parts[0], parts[1]);
  }

  /** Attempts to parse the given namespace and path as a metal ID, returning null if invalid. */
  @Nullable
  public static MetalId tryBuild(String namespace, String path) {
    if (isValidNamespace(namespace) && isValidPath(path)) {
      return new MetalId(namespace, path, null);
    }
    return null;
  }
}

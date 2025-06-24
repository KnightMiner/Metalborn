package knightminer.metalborn.client;

import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.CompositeModel;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.ItemLayerPixels;
import slimeknights.mantle.util.LogicHelper;
import slimeknights.mantle.util.ReversedListBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Item model that permutes  */
public class PalettedItemModel implements IUnbakedGeometry<PalettedItemModel> {
  /** Loader instance for this model */
  public static final IGeometryLoader<PalettedItemModel> LOADER = (json, context) -> new PalettedItemModel(PermutationData.LIST_LOADABLE.getIfPresent(json, "palette"));

  private final List<PermutationData> permutations;
  private PalettedItemModel(List<PermutationData> permutations) {
    this.permutations = permutations;
  }

  @Override
  public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
    // fetch all textures needed for this model
    ResourceManager manager = Minecraft.getInstance().getResourceManager();
    List<BakedPermutationData> textures = new ArrayList<>();
    for (int i = 0; context.hasMaterial("layer" + i); i++) {
      textures.add(LogicHelper.getOrDefault(permutations, i, PermutationData.EMPTY).bake(manager, context.getMaterial("layer" + i), spriteGetter));
    }
    textures = List.copyOf(textures);

    // setup baking
    modelTransform = MantleItemLayerModel.applyTransform(modelTransform, context.getRootTransform());
    RenderTypeGroup renderTypes = MantleItemLayerModel.getDefaultRenderType(context);
    PermutatedItemOverrides permutatedOverrides = new PermutatedItemOverrides(context, textures, modelTransform, renderTypes);

    // bake final model
    return permutatedOverrides.bake(permutatedOverrides, List.of());
  }

  /** Converts the given location to a path, discarding the minecraft namespace. */
  public static String toSuffix(ResourceLocation location) {
    String namespace = location.getNamespace();
    String path = location.getPath();
    if ("minecraft".equals(namespace)) {
      return path;
    }
    return namespace + '_' + path;
  }

  /**
   * Data for the permuted textures
   * @param list     List of all value textures, for caching
   * @param key  Key in item NBT containing the key for textures
   */
  public record PermutationData(@Nullable ResourceLocation list, String key) {
    private static final ResourceLocation UNKNOWN = new ResourceLocation("unknown");
    public static final PermutationData EMPTY = new PermutationData(null, "");
    public static final RecordLoadable<PermutationData> LOADABLE = RecordLoadable.create(
      Loadables.RESOURCE_LOCATION.nullableField("list", PermutationData::list),
      StringLoadable.DEFAULT.defaultField("key", "", PermutationData::key),
      PermutationData::new);
    public static final Loadable<List<PermutationData>> LIST_LOADABLE = LOADABLE.list(ArrayLoadable.COMPACT);

    /** Appends the given location to the texture name */
    private static Material append(Material material, ResourceLocation variant) {
      return new Material(material.atlasLocation(), material.texture().withSuffix('_' + toSuffix(variant)));
    }

    /** Fetches all permutation data for the baked model */
    private BakedPermutationData bake(ResourceManager manager, Material material, Function<Material, TextureAtlasSprite> spriteGetter) {
      // if this texture is not permuted, just cache the sprite
      if (list == null || key.isEmpty()) {
        return new BakedPermutationData("", spriteGetter.apply(material), Map.of());
      }
      List<ResourceLocation> permutationList = PaletteListManager.INSTANCE.getPermutationList(manager, list);
      Map<String,TextureAtlasSprite> textures = new HashMap<>();
      for (ResourceLocation variant : permutationList) {
        textures.put(variant.toString(), spriteGetter.apply(append(material, variant)));
      }
      return new BakedPermutationData(key, spriteGetter.apply(append(material, UNKNOWN)), Map.copyOf(textures));
    }
  }

  /** List of all textures being used for the given variant */
  private record BakedPermutationData(String key, TextureAtlasSprite defaultTexture, Map<String,TextureAtlasSprite> textures) {
    /** Gets the texture for the given variant */
    public TextureAtlasSprite getTexture(String variant) {
      if (variant.isEmpty()) {
        return defaultTexture;
      }
      return textures.getOrDefault(variant, defaultTexture);
    }
  }

  /** Override handler and model baker for the permutation item model */
  private static class PermutatedItemOverrides extends ItemOverrides {
    /** Model variant cache */
    private final Map<String,BakedModel> cache = new HashMap<>();
    /** List of textures to use */
    private final IGeometryBakingContext context;
    private final List<BakedPermutationData> permutations;
    private final ModelState modelTransform;
    private final RenderTypeGroup renderTypes;

    private PermutatedItemOverrides(IGeometryBakingContext context, List<BakedPermutationData> permutations, ModelState modelTransform, RenderTypeGroup renderTypes) {
      this.context = context;
      this.permutations = permutations;
      this.modelTransform = modelTransform;
      this.renderTypes = renderTypes;
    }

    // TODO: do we need nested overrides at all?

    /** Bakes a model with the given item data */
    private BakedModel bake(ItemOverrides overrides, List<String> itemData) {
      record QuadGroup(RenderTypeGroup renderType, Collection<BakedQuad> quads) {}
      ReversedListBuilder<QuadGroup> quadBuilder = new ReversedListBuilder<>();
      ItemLayerPixels pixels = permutations.size() == 1 ? null : new ItemLayerPixels();

      // setup render types

      // skip the pixel tracking if using a single texture only
      TextureAtlasSprite particle = permutations.get(0).defaultTexture;
      Transformation transform = modelTransform.getRotation();
      for (int i = permutations.size() - 1; i >= 0; i--) {
        BakedPermutationData data = permutations.get(i);
        TextureAtlasSprite sprite = data.getTexture(LogicHelper.getOrDefault(itemData, i, ""));
        if (i == 0) {
          particle = sprite;
        }
        quadBuilder.add(new QuadGroup(renderTypes, MantleItemLayerModel.getQuadsForSprite(-1, data.key.isEmpty() ? -1 : i, sprite, transform, 0, pixels)));
      }

      // build final model
      CompositeModel.Baked.Builder modelBuilder = CompositeModel.Baked.builder(context, particle, overrides, context.getTransforms());
      quadBuilder.build(quadGroup -> modelBuilder.addQuads(quadGroup.renderType, quadGroup.quads));
      return modelBuilder.build();
    }

    @Nullable
    @Override
    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
      CompoundTag tag = stack.getTag();
      if (tag != null) {
        // find variant info from NBT. Will use it for a cache key and a list of variants for the builder
        String cacheKey = "";
        List<String> values = new ArrayList<>(permutations.size());
        for (BakedPermutationData permutation : permutations) {
          String key = permutation.key;
          if (!key.isEmpty()) {
            String value = tag.getString(key);
            if (!value.isEmpty()) {
              values.add(value);
              if (!cacheKey.isEmpty()) {
                //noinspection StringConcatenationInLoop  practically we will most often have just 1 so string is faster
                cacheKey += '\\' + value;
              } else {
                cacheKey = value;
              }
            }
            continue;
          }
          values.add("");
        }
        // empty cache key means we found no NBT, so default model
        if (!cacheKey.isEmpty()) {
          BakedModel cached = cache.get(cacheKey);
          if (cached == null) {
            cached = bake(ItemOverrides.EMPTY, values);
            cache.put(cacheKey, cached);
          }
          return cached;
        }
      }

      return model;
    }
  }
}

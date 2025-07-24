package knightminer.metalborn.client.model;

import knightminer.metalborn.client.model.PalettedItemModel.BakedPermutationData;
import knightminer.metalborn.client.model.PalettedItemModel.PermutatedItemOverrides;
import knightminer.metalborn.client.model.PalettedItemModel.PermutationData;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import knightminer.metalborn.metal.MetalShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemModelShaper;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.recipe.helper.TagPreference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/** Items model which redirects to tag models based on variants in NBT */
public class MetalShapeModel implements IUnbakedGeometry<MetalShapeModel> {
  /** Loader instance for this model */
  public static final IGeometryLoader<MetalShapeModel> LOADER = (json, context) -> new MetalShapeModel(
    MetalShape.LOADABLE.getIfPresent(json, "shape"),
    StringLoadable.DEFAULT.getOrDefault(json, "key", MetalItem.TAG_METAL),
    Loadables.RESOURCE_LOCATION.getIfPresent(json, "palette_list"));

  /** Resource shape to display */
  private final MetalShape shape;
  /** Location of the metal in NBT */
  private final String key;
  /** Metals for the fallback layer */
  private final ResourceLocation paletteList;

  private MetalShapeModel(MetalShape shape, String key, ResourceLocation paletteList) {
    this.shape = shape;
    this.key = key;
    this.paletteList = paletteList;
  }

  @Override
  public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
    // fetch our texture
    ResourceManager manager = Minecraft.getInstance().getResourceManager();
    BakedPermutationData texture = new PermutationData(paletteList, key).bake(manager, context.getMaterial("texture"), spriteGetter);

    // setup baking
    modelTransform = MantleItemLayerModel.applyTransform(modelTransform, context.getRootTransform());
    RenderTypeGroup renderTypes = MantleItemLayerModel.getDefaultRenderType(context);
    ShapeOverrides shapeOverrides = new ShapeOverrides(context, texture, modelTransform, renderTypes, shape, key);

    // bake final model
    return shapeOverrides.bake(shapeOverrides, List.of());
  }

  /** Handles selecting the model based item NBT */
  private static class ShapeOverrides extends PermutatedItemOverrides {
    private final ItemModelShaper modelShaper = Minecraft.getInstance().getItemRenderer().getItemModelShaper();
    private final MetalShape shape;
    private final String key;
    private final Map<String,BakedModel> variants = new HashMap<>();

    private ShapeOverrides(IGeometryBakingContext context, BakedPermutationData data, ModelState modelTransform, RenderTypeGroup renderTypes, MetalShape shape, String key) {
      super(context, List.of(data), modelTransform, renderTypes);
      this.shape = shape;
      this.key = key;
    }

    @Override
    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
      // must have the key in NBT
      CompoundTag tag = stack.getTag();
      if (tag != null) {
        String value = tag.getString(key);
        if (!value.isEmpty()) {
          // if we have a cached model, use that
          BakedModel override = variants.get(value);
          if (override != null) {
            // TODO: worth doing overrides on the found model?
            return override;
          }
          // no cache? time to find the model
          MetalId id = MetalId.tryParse(value);
          if (id != null) {
            MetalPower power = MetalManager.INSTANCE.get(id);
            if (power != MetalPower.DEFAULT) {
              Optional<Item> item = TagPreference.getPreference(power.tag(shape));
              if (item.isPresent()) {
                // fetch item model by name
                override = modelShaper.getItemModel(item.get());
              }
            }
          }
          // no tag preference found? generate a fallback from our nugget textures
          if (override == null) {
            override = bake(ItemOverrides.EMPTY, List.of(value));
          }

          // cache what we found for next time; if we found nothing this caches the default variant
          variants.put(value, override);
          return override;
        }
      }
      return model;
    }
  }
}

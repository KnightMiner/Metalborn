package knightminer.metalborn.client.model;

import com.google.gson.JsonObject;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.metal.MetalShape;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

/** Builder for {@link MetalShapeModel} */
public class MetalShapeModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
  @Nullable
  private MetalShape shape;
  private String key = MetalItem.TAG_METAL;
  @Nullable
  private ResourceLocation list;
  public MetalShapeModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
    super(Metalborn.resource("metal_shape"), parent, existingFileHelper);
  }

  /** Sets the metal shape to display */
  public MetalShapeModelBuilder<T> shape(MetalShape shape) {
    this.shape = shape;
    return this;
  }

  /** Sets the key in NBT */
  public MetalShapeModelBuilder<T> key(String key) {
    this.key = key;
    return this;
  }

  /** Sets the fallback paletted list */
  public MetalShapeModelBuilder<T> paletteList(ResourceLocation list) {
    this.list = list;
    return this;
  }

  @Override
  public JsonObject toJson(JsonObject json) {
    if (shape == null) {
      throw new IllegalStateException("Shape has not been set");
    }
    super.toJson(json);
    json.add("shape", MetalShape.LOADABLE.serialize(shape));
    json.addProperty("key", key);
    if (list != null) {
      json.addProperty("palette_list", list.toString());
    }
    return json;
  }
}

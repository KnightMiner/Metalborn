package knightminer.metalborn.client.model;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.JsonObject;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.MetalShape;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

/** Builder for {@link MetalShapeModel} */
public class MetalShapeModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
  @Nullable
  private MetalShape shape;
  @Nullable
  private ResourceLocation list;
  public MetalShapeModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
    super(Metalborn.resource("metal_shape"), parent, existingFileHelper, false);
  }

  /** Sets the metal shape to display */
  @CanIgnoreReturnValue
  public MetalShapeModelBuilder<T> shape(MetalShape shape) {
    this.shape = shape;
    return this;
  }

  /** Sets the fallback paletted list */
  @CanIgnoreReturnValue
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
    if (list != null) {
      json.addProperty("palette_list", list.toString());
    }
    return json;
  }
}

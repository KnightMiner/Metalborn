package knightminer.metalborn.client.model;

import com.google.gson.JsonObject;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.client.model.PalettedItemModel.PermutationData;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import slimeknights.mantle.client.model.ItemKey;

import java.util.ArrayList;
import java.util.List;

/** Builder for {@link PalettedItemModel} */
public class PalettedModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
  private final List<PermutationData> data = new ArrayList<>();
  public PalettedModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
    super(Metalborn.resource("paletted"), parent, existingFileHelper, false);
  }

  /** Adds a permutated texture */
  public PalettedModelBuilder<T> paletted(ResourceLocation list, ItemKey key) {
    data.add(new PermutationData(list, key));
    return this;
  }

  /** Adds a standard texture */
  public PalettedModelBuilder<T> normal() {
    data.add(PermutationData.EMPTY);
    return this;
  }

  @Override
  public JsonObject toJson(JsonObject json) {
    super.toJson(json);
    json.add("palette", PermutationData.LIST_LOADABLE.serialize(data));
    return json;
  }
}

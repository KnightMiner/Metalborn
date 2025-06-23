package knightminer.metalborn.data;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.client.PalettedModelBuilder;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.MetalItem;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.mantle.registration.object.ItemObject;

import static knightminer.metalborn.Metalborn.resource;

/** Data generator for all models in this mod */
public class ItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider {
  private final UncheckedModelFile GENERATED = new UncheckedModelFile("item/generated");
  public static final ResourceLocation FERUCHEMY_METALS = resource("feruchemy_metals");

  public ItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
    super(output, Metalborn.MOD_ID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    basicItem(Registration.LERASIUM_NUGGET, "materials/lerasium_nugget");
    // lerasium alloy wants to have a mixture of the two colors so it's not mistaken for a normal nugget, so part is untinted
    customModel(Registration.LERASIUM_ALLOY_NUGGET)
      .texture("layer0", "item/materials/lerasium_nugget")
      .texture("layer1", "item/materials/lerasium_nugget_overlay")
      .customLoader(PalettedModelBuilder::new)
      .normal()
      .paletted(FERUCHEMY_METALS, MetalItem.TAG_METAL);
  }


  /** Generated item with a set texture */
  @SuppressWarnings("SameParameterValue")
  private void basicItem(ItemObject<?> item, String texture) {
    getBuilder(item.getId().getPath()).parent(GENERATED).texture("layer0", "item/" + texture);
  }

  /** Creates a part model with the given texture */
  private ItemModelBuilder customModel(ItemObject<?> item) {
    return withExistingParent(item.getId().getPath(), "forge:item/default");
  }

  /** Creates a part model with the given texture */
  @SuppressWarnings("SameParameterValue")
  private PalettedModelBuilder<ItemModelBuilder> metal(ItemObject<?> item, String texture) {
    return customModel(item)
      .texture("layer0", resource("item/" + texture))
      .customLoader(PalettedModelBuilder::new)
      .paletted(FERUCHEMY_METALS, MetalItem.TAG_METAL);
  }
}

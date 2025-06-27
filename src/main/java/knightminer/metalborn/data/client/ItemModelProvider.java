package knightminer.metalborn.data.client;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.client.PalettedModelBuilder;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.data.MetalIds;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.registration.object.IdAwareObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.mantle.registration.object.MetalItemObject;

import static knightminer.metalborn.Metalborn.resource;
import static knightminer.metalborn.client.PalettedItemModel.toSuffix;

/** Data generator for all item models in this mod */
public class ItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider {
  private final UncheckedModelFile GENERATED = new UncheckedModelFile("item/generated");
  public static final ResourceLocation FERUCHEMY_METALS = resource("metals/feruchemy");

  public ItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
    super(output, Metalborn.MOD_ID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    // ores
    basicItem(Registration.RAW_TIN, "raw_tin");
    // ingots and nuggets
    metal(Registration.TIN, MetalIds.tin);
    metal(Registration.PEWTER, MetalIds.pewter);
    metal(Registration.STEEL, MetalIds.steel);
    metal(Registration.BRONZE, MetalIds.bronze);
    metal(Registration.ROSE_GOLD, MetalIds.roseGold);
    nugget(Registration.COPPER_NUGGET, MetalIds.copper);
    basicItem(Registration.NETHERITE_NUGGET, "netherite_nugget");
    basicItem(Registration.LERASIUM_NUGGET, "lerasium_nugget");
    // lerasium alloy wants to have a mixture of the two colors so it's not mistaken for a normal nugget, so part is untinted
    customModel(Registration.LERASIUM_ALLOY_NUGGET, false)
      .texture("layer0", "item/lerasium_nugget")
      .texture("layer1", "metal/item/nugget_overlay")
      .customLoader(PalettedModelBuilder::new)
      .normal()
      .paletted(FERUCHEMY_METALS, MetalItem.TAG_METAL);
    metal(Registration.BRACER, "metal/item/bracer", true);
    metal(Registration.RING, "metal/item/ring", false);
    // spikes we want to rotate the in hand model 180 degrees so it points out
    metal(Registration.SPIKE, "metal/item/spike", true).end().transforms()
      .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, -90, -25).translation(1.13f, 3.2f, 1.13f).scale(0.68f, 0.68f, 0.68f).end()
      .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 90, 25).translation(1.13f, 3.2f, 1.13f).scale(0.68f, 0.68f, 0.68f).end();
  }


  /** Gets the path for an item */
  private static String path(ItemLike item) {
    return Loadables.ITEM.getKey(item.asItem()).getPath();
  }

  /** Generated item with a set texture */
  @SuppressWarnings("SameParameterValue")
  private void basicItem(ItemObject<?> item, String texture) {
    String name = item.getId().getPath();
    getBuilder(name).parent(GENERATED).texture("layer0", "item/" + texture);
  }

  /** Adds a basic metal item */
  private void metalItem(String path, String name, MetalId metal) {
    // metal items are generated textures, so just mark as existing so the provider is happy
    String texture = "metal/item/" + name + '_' + toSuffix(metal);
    existingFileHelper.trackGenerated(Metalborn.resource(texture), ModelProvider.TEXTURE);
    getBuilder(path).parent(GENERATED).texture("layer0", texture);
  }

  /** Adds a nugget model */
  private void nugget(IdAwareObject item, MetalId metal) {
    metalItem(item.getId().getPath(), "nugget", metal);
  }

  /** Generated ingots and nuggets for the given metal object */
  @SuppressWarnings("SameParameterValue")
  private void metal(MetalItemObject object, MetalId metal) {
    metalItem(path(object.getIngot()), "ingot", metal);
    metalItem(path(object.getNugget()), "nugget", metal);
  }

  /** Creates a part model with the given texture */
  private ItemModelBuilder customModel(ItemObject<?> item, boolean tool) {
    return withExistingParent(item.getId().getPath(), tool ? "forge:item/default-tool" : "forge:item/default");
  }

  /** Creates a part model with the given texture */
  @SuppressWarnings("UnusedReturnValue")
  private PalettedModelBuilder<ItemModelBuilder> metal(ItemObject<?> item, String texture, boolean tool) {
    return customModel(item, tool)
      .texture("layer0", resource(texture))
      .customLoader(PalettedModelBuilder::new)
      .paletted(FERUCHEMY_METALS, MetalItem.TAG_METAL);
  }
}

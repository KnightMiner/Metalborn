package knightminer.metalborn.data.client;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.client.model.MetalShapeModelBuilder;
import knightminer.metalborn.client.model.PalettedModelBuilder;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.data.MetalIds;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.item.metalmind.IdentityMetalmindItem;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalShape;
import knightminer.metalborn.util.CastItemObject;
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
import static knightminer.metalborn.client.model.PalettedItemModel.toSuffix;

/** Data generator for all item models in this mod */
public class ItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider {
  private final UncheckedModelFile GENERATED = new UncheckedModelFile("item/generated");
  public static final ResourceLocation FERUCHEMY_METALS = resource("metals/feruchemy");

  public ItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
    super(output, Metalborn.MOD_ID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    basicItem(Registration.METALLIC_ARTS, "metallic_arts");
    // ores
    basicItem(Registration.RAW_TIN, "raw_tin");
    // ingots and nuggets
    metal(Registration.TIN, MetalIds.tin);
    metal(Registration.PEWTER, MetalIds.pewter);
    metal(Registration.STEEL, MetalIds.steel);
    metal(Registration.BRONZE, MetalIds.bronze);
    metal(Registration.ROSE_GOLD, MetalIds.roseGold);
    metal(Registration.NICROSIL, MetalIds.nicrosil);
    nugget(Registration.COPPER_NUGGET, MetalIds.copper);
    nugget(Registration.NETHERITE_NUGGET, MetalIds.netherite);

    // ferring nuggets
    nugget(Registration.RANDOM_FERRING, MetalIds.nicrosil);
    customModel(Registration.CHANGE_FERRING, false)
      .texture("texture", "metal/item/nugget")
      .customLoader(MetalShapeModelBuilder::new)
      .shape(MetalShape.NUGGET)
      .paletteList(resource("metals/nuggets"));

    // metalminds
    metal(Registration.BRACER, "metal/item/bracer", true);
    metal(Registration.RING, "metal/item/ring", false);
    metal(Registration.UNSEALED_RING, "metal/item/ring", false).end().texture("layer1", "item/unsealed_ring_gem");
    // investiture items just use nicrosil directly
    metalItem(Registration.INVESTITURE_BRACER, "bracer", MetalIds.nicrosil);
    metalItem(Registration.INVESTITURE_RING, "ring", MetalIds.nicrosil);
    metalItem(Registration.IDENTITY_BRACER, "bracer", IdentityMetalmindItem.QUARTZ);
    metalItem(Registration.IDENTITY_RING, "ring", IdentityMetalmindItem.QUARTZ);
    // spikes we want to rotate the in hand model 180 degrees so it points out
    metal(Registration.SPIKE, "metal/item/spike", true).end().transforms()
      .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, -90, -25).translation(1.13f, 3.2f, 1.13f).scale(0.68f, 0.68f, 0.68f).end()
      .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 90, 25).translation(1.13f, 3.2f, 1.13f).scale(0.68f, 0.68f, 0.68f).end();
    existingFileHelper.trackGenerated(Metalborn.resource("metal/item/spike_metalborn_nicrosil"), ModelProvider.TEXTURE);
    // can't use metal item as we want the handheld transforms
    withExistingParent(Registration.INVESTITURE_SPIKE.getId().getPath(), "item/handheld")
      .texture("layer0", "metal/item/spike_metalborn_nicrosil");

    // tinkers' compat
    cast(Registration.RING_CAST);
    cast(Registration.BRACER_CAST);
    cast(Registration.SPIKE_CAST);
  }


  /** Gets the path for an item */
  private static String path(ItemLike item) {
    return Loadables.ITEM.getKey(item.asItem()).getPath();
  }

  /** Generated item with a set texture */
  @SuppressWarnings("SameParameterValue")
  private void basicItem(String name, String texture) {
    getBuilder(name).parent(GENERATED).texture("layer0", "item/" + texture);
  }

  /** Generated item with a set texture */
  @SuppressWarnings("SameParameterValue")
  private void basicItem(ItemObject<?> item, String texture) {
    basicItem(item.getId().getPath(), texture);
  }

  /** Adds a basic metal item */
  private void metalItem(String path, String name, MetalId metal) {
    // metal items are generated textures, so just mark as existing so the provider is happy
    String texture = "metal/item/" + name + '_' + toSuffix(metal);
    existingFileHelper.trackGenerated(Metalborn.resource(texture), ModelProvider.TEXTURE);
    getBuilder(path).parent(GENERATED).texture("layer0", texture);
  }

  /** Adds a basic metal item */
  private void metalItem(IdAwareObject item, String name, MetalId metal) {
    metalItem(item.getId().getPath(), name, metal);
  }

  /** Adds a nugget model */
  private void nugget(IdAwareObject item, MetalId metal) {
    metalItem(item, "nugget", metal);
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

  /** Creates models for the given cast object */
  private void cast(CastItemObject cast) {
    String name = cast.getId().getPath();
    basicItem(path(cast.get()), "cast/" + name + "_gold");
    basicItem(path(cast.getSand()), "cast/" + name + "_sand");
    basicItem(path(cast.getRedSand()), "cast/" + name + "_red_sand");
  }
}

package knightminer.metalborn.data.client;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.client.MetalbornClient;
import knightminer.metalborn.client.model.MetalShapeModelBuilder;
import knightminer.metalborn.client.model.PalettedModelBuilder;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.data.MetalIds;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.item.SatchelItem.SatchelType;
import knightminer.metalborn.item.metalmind.IdentityMetalmindItem;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalShape;
import knightminer.metalborn.util.CastItemObject;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
  /** Standard item transforms */
  private static final String ITEM = "forge:item/default";
  /** Item held like a pickaxe or sword */
  private static final String TOOL = "forge:item/default-tool";
  /** Item held like a blaze rod or stick */
  private static final String ROD = "metalborn:item/default_rod";
  /** Parent for generated item models */
  private final UncheckedModelFile GENERATED = new UncheckedModelFile("item/generated");
  /** File for anything repalette as feruchey metals used on rings and bracers */
  public static final ResourceLocation FERUCHEMY_METALS = resource("metals/feruchemy");

  public ItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
    super(output, Metalborn.MOD_ID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    basicItem(Registration.METALLIC_ARTS);
    // ores
    basicItem(Registration.RAW_TIN);
    // ingots and nuggets
    metal(Registration.TIN, MetalIds.tin);
    metal(Registration.PEWTER, MetalIds.pewter);
    metal(Registration.STEEL, MetalIds.steel);
    metal(Registration.BRONZE, MetalIds.bronze);
    metal(Registration.ROSE_GOLD, MetalIds.roseGold);
    metal(Registration.NICROSIL, MetalIds.nicrosil);
    nugget(Registration.COPPER_NUGGET, MetalIds.copper);
    nugget(Registration.NETHERITE_SHARD, MetalIds.netherite);
    basicItem(Registration.NETHERITE_NUGGET, "netherite_nugget");

    // ferring nuggets
    nugget(Registration.RANDOM_FERRING, MetalIds.nicrosil);
    customModel(Registration.CHANGE_FERRING, ITEM)
      .texture("texture", "metal/item/nugget")
      .customLoader(MetalShapeModelBuilder::new)
      .shape(MetalShape.NUGGET)
      .paletteList(resource("metals/nuggets"));

    // metalminds
    metal(Registration.BRACER, "metal/item/bracer", TOOL);
    metal(Registration.RING, "metal/item/ring", ITEM);
    metal(Registration.UNSEALED_RING, "metal/item/ring", ITEM).end().texture("layer1", "item/unsealed_ring_gem");
    metal(Registration.SOULBOUND_RING, "metal/item/ring", ITEM).end().texture("layer1", "item/soulbound_ring_gem");
    // investiture items just use nicrosil directly
    metalItem(Registration.INVESTITURE_BRACER, "bracer", MetalIds.nicrosil);
    metalItem(Registration.INVESTITURE_RING, "ring", MetalIds.nicrosil);
    metalItem(Registration.IDENTITY_BRACER, "bracer", IdentityMetalmindItem.QUARTZ);
    metalItem(Registration.IDENTITY_RING, "ring", IdentityMetalmindItem.QUARTZ);
    // spikes we want to rotate the in hand model 180 degrees so it points out
    metal(Registration.SPIKE, "metal/item/spike", ROD);
    existingFileHelper.trackGenerated(Metalborn.resource("metal/item/spike_metalborn_nicrosil"), ModelProvider.TEXTURE);
    // if no metal, use a pure nicrosil spike
    metal(Registration.INVESTITURE_SPIKE, "metal/item/spike", ROD).end()
      .texture("layer1", "item/nicrosil_spike_overlay")
        .override().predicate(MetalbornClient.NO_METAL, 1)
        // can't use metal item as we want the handheld transforms, don't currently need elsewhere so no helper
        .model(withExistingParent("investiture_spike_empty", "item/handheld_rod")
          .texture("layer0", "metal/item/spike_metalborn_nicrosil"));

    // satchels
    Registration.SATCHEL.forEach(this::satchel);

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

  /** Generated item with a set texture */
  @SuppressWarnings("SameParameterValue")
  private void basicItem(ItemObject<?> item) {
    basicItem(item.getId().getPath(), item.getId().getPath());
  }

  /** Creates the model for a satchel */
  private void satchel(SatchelType type, Item item) {
    String variant = type.getSerializedName();
    // model when dyed
    String texture = "item/satchel_" + variant;
    ItemModelBuilder dyed = getBuilder("item/dyed_satchel/" + variant).parent(GENERATED)
      .texture("layer0", texture)
      .texture("layer1", "item/satchel_dyed");
    // model when undyed
    getBuilder(Loadables.ITEM.getKey(item).getPath()).parent(GENERATED)
      .texture("layer0", texture)
      .override().model(dyed).predicate(MetalbornClient.DYED, 1);
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
  @SuppressWarnings("SameParameterValue")
  private ItemModelBuilder customModel(ItemObject<?> item, String parent) {
    return withExistingParent(item.getId().getPath(), parent);
  }

  /** Creates a part model with the given texture */
  @SuppressWarnings("UnusedReturnValue")
  private PalettedModelBuilder<ItemModelBuilder> metal(String path, String texture, String parent) {
    return withExistingParent(path, parent)
      .texture("layer0", resource(texture))
      .customLoader(PalettedModelBuilder::new)
      .paletted(FERUCHEMY_METALS, MetalItem.TAG_METAL);
  }


  /** Creates a part model with the given texture */
  @SuppressWarnings("UnusedReturnValue")
  private PalettedModelBuilder<ItemModelBuilder> metal(ItemObject<?> item, String texture, String parent) {
    return metal(item.getId().getPath(), texture, parent);
  }

  /** Creates models for the given cast object */
  private void cast(CastItemObject cast) {
    String name = cast.getId().getPath();
    basicItem(path(cast.get()), "cast/" + name + "_gold");
    basicItem(path(cast.getSand()), "cast/" + name + "_sand");
    basicItem(path(cast.getRedSand()), "cast/" + name + "_red_sand");
  }
}

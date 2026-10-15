package knightminer.metalborn.data;

import knightminer.metalborn.core.Registration;
import knightminer.metalborn.json.ingredient.FillableIngredient;
import knightminer.metalborn.json.ingredient.IngredientWithMetal.MetalFilter;
import knightminer.metalborn.json.ingredient.MetalItemIngredient;
import knightminer.metalborn.plugin.tinkers.MetalCastingRecipeBuilder;
import knightminer.metalborn.plugin.tinkers.MetalMeltingRecipeBuilder;
import knightminer.metalborn.plugin.tinkers.TinkersMockRecipeBuilder;
import knightminer.metalborn.util.CastItemObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredItem;
import slimeknights.mantle.recipe.data.mock.MockRecipeConsumer;
import slimeknights.mantle.registration.object.IdAwareObject;
import slimeknights.mantle.util.JsonHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static slimeknights.mantle.Mantle.commonResource;

/** Adds recipe JSON without depending on another mod's recipe builders. */
public class MockRecipeProvider extends slimeknights.mantle.recipe.data.mock.MockRecipeProvider {
  public MockRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    super(output, registries);
  }

  @Override
  protected void buildRecipes(MockRecipeConsumer output, HolderLookup.Provider registries) {
    String tinkersFolder = "tinkers/";

    // casting
    int ingot = 90;
    int nugget = 10;
    // standard metal items
    metalMeltingCasting(output, Registration.RING,   List.of(Registration.INVESTITURE_RING,   Registration.IDENTITY_RING),   Registration.RING_CAST,   MetalFilter.METALMIND, nugget * 4, tinkersFolder);
    metalMeltingCasting(output, Registration.BRACER, List.of(Registration.INVESTITURE_BRACER, Registration.IDENTITY_BRACER), Registration.BRACER_CAST, MetalFilter.METALMIND, ingot * 4,  tinkersFolder);
    metalMeltingCasting(output, Registration.SPIKE,  List.of(Registration.INVESTITURE_SPIKE),                                Registration.SPIKE_CAST,  MetalFilter.SPIKE,     ingot * 2,  tinkersFolder);
    // special metalminds
    TagKey<Fluid> nicrosil = FluidTags.create(commonResource("molten_nicrosil"));
    int nicrosilTemperature = 1100;
    TinkersMockRecipeBuilder.meltingCasting(output, Registration.INVESTITURE_RING,   Registration.RING_CAST,   nicrosil, nugget * 4, nicrosilTemperature, tinkersFolder + "ring/investiture/");
    TinkersMockRecipeBuilder.meltingCasting(output, Registration.INVESTITURE_BRACER, Registration.BRACER_CAST, nicrosil, ingot * 4,  nicrosilTemperature, tinkersFolder + "bracer/investiture/");
    TinkersMockRecipeBuilder.meltingCasting(output, Registration.INVESTITURE_SPIKE,  Registration.SPIKE_CAST,  nicrosil, ingot * 2,  nicrosilTemperature, tinkersFolder + "spike/investiture/");
    // identity may be quartz and may be aluminum based on loaded content
    TinkersMockRecipeBuilder.identityMeltingCasting(output, Registration.IDENTITY_RING,   Registration.RING_CAST,   nugget * 4, 100, tinkersFolder + "ring/identity/");
    TinkersMockRecipeBuilder.identityMeltingCasting(output, Registration.IDENTITY_BRACER, Registration.BRACER_CAST, ingot * 4,  400, tinkersFolder + "bracer/identity/");

    // nuggets to change ferring type
    TagKey<Fluid> netherite = FluidTags.create(commonResource("molten_netherite"));
    int netheriteTemperature = 1250;
    TinkersMockRecipeBuilder.casting(output, Registration.RANDOM_FERRING, false, FillableIngredient.filled(MetalItemIngredient.of(Registration.SPIKE, MetalFilter.SPIKE)), nicrosil,  nugget, nicrosilTemperature,  tinkersFolder + "random_ferring_casting");
    TinkersMockRecipeBuilder.casting(output, Registration.CHANGE_FERRING, true,  FillableIngredient.filled(MetalItemIngredient.of(Registration.SPIKE, MetalFilter.SPIKE)), netherite, nugget, netheriteTemperature, tinkersFolder + "change_ferring_casting");
  }

  /** Adds a recipe for casting a metal object, like a metalmind */
  private <T extends ItemLike & IdAwareObject> void metalMeltingCasting(MockRecipeConsumer consumer, DeferredItem<?> result, List<ItemLike> otherItems, CastItemObject cast, MetalFilter filter, int amount, String folder) {
    // casting
    ResourceLocation id = result.getId();
    MetalCastingRecipeBuilder.table(result)
      .setCast(Ingredient.of(cast.getMultiUseTag()), false)
      .setFilter(filter)
      .setAmount(amount)
      .save(consumer, JsonHelper.wrap(id, folder, "/casting_gold_cast"));
    MetalCastingRecipeBuilder.table(result)
      .setCast(Ingredient.of(cast.getSingleUseTag()), true)
      .setFilter(filter)
      .setAmount(amount)
      .save(consumer, JsonHelper.wrap(id, folder, "/casting_sand_cast"));

    // melting
    MetalMeltingRecipeBuilder.melting(MetalItemIngredient.of(result, filter), amount).save(consumer, JsonHelper.wrap(id, folder, "/melting"));

    // cast creation
    TinkersMockRecipeBuilder.castRecipes(consumer, result, otherItems, cast, filter, Math.max(1, amount / 90), folder);
  }

  @Override
  public String getName() {
    return "Metalborn Mock Recipe Provider";
  }
}

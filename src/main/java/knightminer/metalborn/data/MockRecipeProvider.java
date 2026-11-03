package knightminer.metalborn.data;

import knightminer.metalborn.core.Registration;
import knightminer.metalborn.json.ingredient.FillableIngredient;
import knightminer.metalborn.json.ingredient.MetalItemIngredient;
import knightminer.metalborn.json.predicate.MetalPredicate;
import knightminer.metalborn.metal.MetalPower;
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
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.recipe.data.mock.MockRecipeConsumer;
import slimeknights.mantle.util.JsonHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static slimeknights.mantle.Mantle.commonResource;

/** Adds recipe JSON without depending on another mod's recipe builders. */
public class MockRecipeProvider extends slimeknights.mantle.recipe.data.mock.MockRecipeProvider {
  private static final int INGOT = 90;
  private static final int NUGGET = 10;
  private static final int GEM = 100;

  public MockRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    super(output, registries);
  }

  @Override
  protected void buildRecipes(MockRecipeConsumer output, HolderLookup.Provider registries) {
    String tinkersFolder = "tinkers/";

    // casting
    // standard metal items
    metalMeltingCasting(output, Registration.RING,   List.of(Registration.INVESTITURE_RING,   Registration.IDENTITY_RING),   Registration.RING_CAST,   MetalPredicate.METALMIND, 1, tinkersFolder);
    metalMeltingCasting(output, Registration.BRACER, List.of(Registration.INVESTITURE_BRACER, Registration.IDENTITY_BRACER), Registration.BRACER_CAST, MetalPredicate.METALMIND, 4,  tinkersFolder);
    metalMeltingCasting(output, Registration.SPIKE,  List.of(Registration.INVESTITURE_SPIKE),                                Registration.SPIKE_CAST,  MetalPredicate.SPIKE,     2,  tinkersFolder);
    // special metalminds
    TagKey<Fluid> nicrosil = FluidTags.create(commonResource("molten_nicrosil"));
    int nicrosilTemperature = 1100;
    TinkersMockRecipeBuilder.meltingCasting(output, Registration.INVESTITURE_RING,   Registration.RING_CAST,   nicrosil, NUGGET * 4, nicrosilTemperature, tinkersFolder + "ring/investiture/");
    TinkersMockRecipeBuilder.meltingCasting(output, Registration.INVESTITURE_BRACER, Registration.BRACER_CAST, nicrosil, INGOT * 4,  nicrosilTemperature, tinkersFolder + "bracer/investiture/");
    TinkersMockRecipeBuilder.meltingCasting(output, Registration.INVESTITURE_SPIKE,  Registration.SPIKE_CAST,  nicrosil, INGOT * 2,  nicrosilTemperature, tinkersFolder + "spike/investiture/");
    // identity may be quartz and may be aluminum based on loaded content
    TinkersMockRecipeBuilder.identityMeltingCasting(output, Registration.IDENTITY_RING,   Registration.RING_CAST,   NUGGET * 4, GEM,    tinkersFolder + "ring/identity/");
    TinkersMockRecipeBuilder.identityMeltingCasting(output, Registration.IDENTITY_BRACER, Registration.BRACER_CAST, INGOT * 4, GEM * 4, tinkersFolder + "bracer/identity/");

    // nuggets to change ferring type
    TagKey<Fluid> netherite = FluidTags.create(commonResource("molten_netherite"));
    int netheriteTemperature = 1250;
    TinkersMockRecipeBuilder.casting(output, Registration.RANDOM_FERRING, false, FillableIngredient.filled(MetalItemIngredient.of(Registration.SPIKE, MetalPredicate.SPIKE)), nicrosil, NUGGET, nicrosilTemperature,  tinkersFolder + "random_ferring_casting");
    TinkersMockRecipeBuilder.casting(output, Registration.CHANGE_FERRING, true,  FillableIngredient.filled(MetalItemIngredient.of(Registration.SPIKE, MetalPredicate.SPIKE)), netherite, NUGGET, netheriteTemperature, tinkersFolder + "change_ferring_casting");
  }

  /** Adds casting recipes for the given metal */
  private void metalCasting(MockRecipeConsumer consumer, DeferredItem<?> result, CastItemObject cast, IJsonPredicate<MetalPower> filter, int amount, String folder, String suffix) {
    ResourceLocation id = result.getId();
    MetalCastingRecipeBuilder.table(result)
      .setCast(Ingredient.of(cast.getMultiUseTag()), false)
      .setFilter(filter)
      .setAmount(amount)
      .save(consumer, JsonHelper.wrap(id, folder, "/casting_gold_cast_" + suffix));
    MetalCastingRecipeBuilder.table(result)
      .setCast(Ingredient.of(cast.getSingleUseTag()), true)
      .setFilter(filter)
      .setAmount(amount)
      .save(consumer, JsonHelper.wrap(id, folder, "/casting_sand_cast_" + suffix));
  }

  /** Adds a recipe for casting a metal object, like a metalmind */
  private void metalMeltingCasting(MockRecipeConsumer consumer, DeferredItem<?> result, List<ItemLike> otherItems, CastItemObject cast, IJsonPredicate<MetalPower> filter, int cost, String folder) {
    IJsonPredicate<MetalPower> metal = MetalPredicate.and(filter, MetalPredicate.IS_GEM.inverted());
    IJsonPredicate<MetalPower> gem = MetalPredicate.and(filter, MetalPredicate.IS_GEM);
    int metalAmount = cost == 1 ? NUGGET * 4 : INGOT * cost;
    int gemAmount = GEM * cost;
    // casting
    metalCasting(consumer, result, cast, metal, metalAmount, folder, "metal");
    metalCasting(consumer, result, cast, gem,   gemAmount,   folder, "gem");
    // melting
    ResourceLocation id = result.getId();
    MetalMeltingRecipeBuilder.melting(MetalItemIngredient.of(result, metal), metalAmount).save(consumer, JsonHelper.wrap(id, folder, "/melting_metal"));
    MetalMeltingRecipeBuilder.melting(MetalItemIngredient.of(result, gem),   gemAmount  ).save(consumer, JsonHelper.wrap(id, folder, "/melting_gem"));

    // cast creation
    TinkersMockRecipeBuilder.castRecipes(consumer, result, otherItems, cast, filter, cost, folder);
  }

  @Override
  public String getName() {
    return "Metalborn Mock Recipe Provider";
  }
}

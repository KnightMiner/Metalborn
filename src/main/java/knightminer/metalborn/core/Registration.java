package knightminer.metalborn.core;


import knightminer.metalborn.Metalborn;
import knightminer.metalborn.block.ForgeBlock;
import knightminer.metalborn.block.ForgeBlockEntity;
import knightminer.metalborn.item.ChangeFerringItem;
import knightminer.metalborn.item.InvestitureSpikeItem;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.item.MetalbornBookItem;
import knightminer.metalborn.item.RandomFerringItem;
import knightminer.metalborn.item.SpikeItem;
import knightminer.metalborn.item.metalmind.IdentityMetalmindItem;
import knightminer.metalborn.item.metalmind.InvestitureMetalmindItem;
import knightminer.metalborn.item.metalmind.Metalmind;
import knightminer.metalborn.item.metalmind.MetalmindItem;
import knightminer.metalborn.item.metalmind.PowerMetalmindItem;
import knightminer.metalborn.item.metalmind.UnsealedMetalmindItem;
import knightminer.metalborn.json.ConfigEnabledCondition;
import knightminer.metalborn.json.ingredient.FillableIngredient;
import knightminer.metalborn.json.ingredient.MetalItemIngredient;
import knightminer.metalborn.json.ingredient.MetalShapeIngredient;
import knightminer.metalborn.json.loot.ApplyDropChanceLootModifier;
import knightminer.metalborn.json.loot.HasLootContextSetCondition;
import knightminer.metalborn.json.recipe.cooking.BlastingMetalRecyclingRecipe;
import knightminer.metalborn.json.recipe.cooking.BlastingResultRecipe;
import knightminer.metalborn.json.recipe.cooking.SmeltingMetalRecyclingRecipe;
import knightminer.metalborn.json.recipe.cooking.SmeltingResultRecipe;
import knightminer.metalborn.json.recipe.forge.ForgeRecipe;
import knightminer.metalborn.json.recipe.forge.MetalShapedForgeRecipe;
import knightminer.metalborn.json.recipe.forge.MetalShapelessForgeRecipe;
import knightminer.metalborn.json.recipe.forge.ShapedForgeRecipe;
import knightminer.metalborn.json.recipe.forge.ShapelessForgeRecipe;
import knightminer.metalborn.menu.ForgeMenu;
import knightminer.metalborn.menu.MetalbornMenu;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.effects.MetalEffect;
import knightminer.metalborn.metal.effects.general.AttributeMetalEffect;
import knightminer.metalborn.metal.effects.general.MobEffectMetalEffect;
import knightminer.metalborn.metal.effects.nesting.CappedMetalEffect;
import knightminer.metalborn.metal.effects.nesting.OffsetMetalEffect;
import knightminer.metalborn.metal.effects.nesting.StoringMetalEffect;
import knightminer.metalborn.metal.effects.nesting.TappingMetalEffect;
import knightminer.metalborn.metal.effects.specialized.EnergyMetalEffect;
import knightminer.metalborn.metal.effects.specialized.ExperienceMetalEffect;
import knightminer.metalborn.metal.effects.specialized.HealMetalEffect;
import knightminer.metalborn.metal.effects.specialized.UpdateHealthEffect;
import knightminer.metalborn.metal.effects.specialized.WarmthMetalEffect;
import knightminer.metalborn.util.AttributeDeferredRegister;
import knightminer.metalborn.util.CastItemObject;
import knightminer.metalborn.util.ItemDeferredRegisterExtension;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.registration.deferred.BlockDeferredRegister;
import slimeknights.mantle.registration.deferred.BlockEntityTypeDeferredRegister;
import slimeknights.mantle.registration.deferred.MenuTypeDeferredRegister;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.mantle.registration.object.MetalItemObject;
import slimeknights.mantle.registration.object.MultiObject;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static knightminer.metalborn.Metalborn.MOD_ID;
import static knightminer.metalborn.Metalborn.resource;

/** Handles any in code registrations */
public class Registration {
  /** Damage type for metal damaging the player, used in gold's effect */
  private static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(Metalborn.MOD_ID);
  private static final ItemDeferredRegisterExtension ITEMS = new ItemDeferredRegisterExtension(Metalborn.MOD_ID);
  private static final MenuTypeDeferredRegister MENUS = new MenuTypeDeferredRegister(Metalborn.MOD_ID);
  private static final BlockEntityTypeDeferredRegister BLOCK_ENTITIES = new BlockEntityTypeDeferredRegister(Metalborn.MOD_ID);
  private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Metalborn.MOD_ID);
  private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Metalborn.MOD_ID);
  private static final AttributeDeferredRegister ATTRIBUTES = new AttributeDeferredRegister(Metalborn.MOD_ID);
  private static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MOD_ID);

  private static final Item.Properties PROPS = new Item.Properties();
  private static final Function<Block, BlockItem> BLOCK_ITEM = block -> new BlockItem(block, PROPS);

  private Registration() {}

  /** Initializes the registration event buses */
  @Internal
  public static void init() {
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(Registration::registerMisc);
    bus.addListener(Registration::addAttributes);
    MinecraftForge.EVENT_BUS.addListener(Registration::missingMappings);
    BLOCKS.register(bus);
    BLOCK_ENTITIES.register(bus);
    ITEMS.register(bus);
    MENUS.register(bus);
    RECIPE_TYPES.register(bus);
    RECIPES.register(bus);
    ATTRIBUTES.register(bus);
    LOOT_CONDITIONS.register(bus);
  }

  public static final ItemObject<Item> METALLIC_ARTS = ITEMS.register("metallic_arts", () -> new MetalbornBookItem(new Properties().stacksTo(1)));

  // metals
  public static final ItemObject<Item> COPPER_NUGGET = ITEMS.register("copper_nugget");
  public static final ItemObject<Item> NETHERITE_NUGGET = ITEMS.register("netherite_nugget");
  public static final ItemObject<RandomFerringItem> RANDOM_FERRING = ITEMS.register("random_ferring", () -> new RandomFerringItem(new Item.Properties()));
  public static final ItemObject<ChangeFerringItem> CHANGE_FERRING = ITEMS.register("change_ferring", () -> new ChangeFerringItem(new Item.Properties()));

  // unique
  public static final MetalItemObject TIN       = BLOCKS.registerMetal("tin",       metalBuilder(MapColor.ICE), BLOCK_ITEM, PROPS);
  public static final MetalItemObject PEWTER    = BLOCKS.registerMetal("pewter",    metalBuilder(MapColor.TERRACOTTA_GREEN), BLOCK_ITEM, PROPS);
  public static final MetalItemObject STEEL     = BLOCKS.registerMetal("steel",     metalBuilder(MapColor.STONE), BLOCK_ITEM, PROPS);
  public static final MetalItemObject BRONZE    = BLOCKS.registerMetal("bronze",    metalBuilder(MapColor.WOOD), BLOCK_ITEM, PROPS);
  public static final MetalItemObject ROSE_GOLD = BLOCKS.registerMetal("rose_gold", metalBuilder(MapColor.TERRACOTTA_WHITE), BLOCK_ITEM, PROPS);
  public static final MetalItemObject NICROSIL  = BLOCKS.registerMetal("nicrosil",  metalBuilder(MapColor.SNOW), BLOCK_ITEM, PROPS);

  // ores
  public static final ItemObject<Block> TIN_ORE = BLOCKS.register("tin_ore", BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F), BLOCK_ITEM);
  public static final ItemObject<Block> DEEPSLATE_TIN_ORE = BLOCKS.register("deepslate_tin_ore", BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE), BLOCK_ITEM);
  public static final ItemObject<Block> RAW_TIN_BLOCK = BLOCKS.register("raw_tin_block", BlockBehaviour.Properties.of().mapColor(MapColor.RAW_IRON).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F), BLOCK_ITEM);
  public static final ItemObject<Item> RAW_TIN = ITEMS.register("raw_tin");

  // metal item tags
  /** List of metalminds to display in the book, should implement {@link MetalItem} */
  public static final TagKey<Item> METALMINDS = itemTag("metalminds");
  /** Valid items for the bracer slot. Should implement {@link Metalmind} */
  public static final TagKey<Item> BRACERS = itemTag("bracers");
  /** Valid items for the ring slots. Should implement {@link Metalmind} */
  public static final TagKey<Item> RINGS = itemTag("rings");
  /** Valid items for the ring slots. Should implement {@link knightminer.metalborn.item.Spike} */
  public static final TagKey<Item> SPIKES = itemTag("spikes");

  // metal items
  // metalminds
  public static final ItemObject<PowerMetalmindItem> BRACER = ITEMS.register("bracer", () -> new PowerMetalmindItem(new Item.Properties().stacksTo(8), 10));
  public static final ItemObject<PowerMetalmindItem> RING = ITEMS.register("ring", () -> new PowerMetalmindItem(new Item.Properties().stacksTo(8), 1));
  public static final ItemObject<PowerMetalmindItem> UNSEALED_RING = ITEMS.register("unsealed_ring", () -> new UnsealedMetalmindItem(new Item.Properties().stacksTo(8), 1));
  // investiture metalminds
  public static final ItemObject<InvestitureMetalmindItem> INVESTITURE_BRACER = ITEMS.register("investiture_bracer", () -> new InvestitureMetalmindItem(new Item.Properties().stacksTo(8), 10));
  public static final ItemObject<InvestitureMetalmindItem> INVESTITURE_RING = ITEMS.register("investiture_ring", () -> new InvestitureMetalmindItem(new Item.Properties().stacksTo(8), 1));
  // identity metalminds
  public static final ItemObject<IdentityMetalmindItem> IDENTITY_BRACER = ITEMS.register("identity_bracer", () -> new IdentityMetalmindItem(new Item.Properties().stacksTo(8), 10));
  public static final ItemObject<IdentityMetalmindItem> IDENTITY_RING = ITEMS.register("identity_ring", () -> new IdentityMetalmindItem(new Item.Properties().stacksTo(8), 1));
  // spikes
  public static final ItemObject<SpikeItem> SPIKE = ITEMS.register("spike", () -> new SpikeItem(new Item.Properties().stacksTo(1)));
  public static final ItemObject<InvestitureSpikeItem> INVESTITURE_SPIKE = ITEMS.register("investiture_spike", () -> new InvestitureSpikeItem(new Item.Properties().stacksTo(1)));

  // metal forge
  public static final ItemObject<ForgeBlock> FORGE = BLOCKS.register("forge", () -> new ForgeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F).lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 13 : 0)), BLOCK_ITEM);
  public static final RegistryObject<BlockEntityType<ForgeBlockEntity>> FORGE_BLOCK_ENTITY = BLOCK_ENTITIES.register("forge", ForgeBlockEntity::new, FORGE);

  /** Damage type for metal damaging the player, used in gold's effect */
  public static final ResourceKey<DamageType> METAL_HURT = ResourceKey.create(Registries.DAMAGE_TYPE, resource("metal_hurt"));
  /** Damage type when using a spike */
  public static final ResourceKey<DamageType> ADD_SPIKE = ResourceKey.create(Registries.DAMAGE_TYPE, resource("add_spike"));
  /** Damage type when making a spike from yourself */
  public static final ResourceKey<DamageType> MAKE_SPIKE = ResourceKey.create(Registries.DAMAGE_TYPE, resource("make_spike"));
  /** Bonus heat damage dealt to the target */
  public static final ResourceKey<DamageType> MELEE_HEAT = ResourceKey.create(Registries.DAMAGE_TYPE, resource("melee_heat"));

  // menus
  public static final RegistryObject<MenuType<MetalbornMenu>> METALBORN_MENU = MENUS.register("metalborn", MetalbornMenu::forClient);
  public static final RegistryObject<MenuType<ForgeMenu>> FORGE_MENU = MENUS.register("forge", ForgeMenu::new);

  // recipe
  public static final RegistryObject<RecipeType<ForgeRecipe>> FORGE_RECIPE = RECIPE_TYPES.register("forge", () -> RecipeType.simple(Metalborn.resource("forge")));
  public static final RegistryObject<RecipeSerializer<ShapelessForgeRecipe>> SHAPELESS_FORGE = RECIPES.register("shapeless_forge", () -> LoadableRecipeSerializer.of(ShapelessForgeRecipe.LOADABLE));
  public static final RegistryObject<RecipeSerializer<ShapedForgeRecipe>> SHAPED_FORGE = RECIPES.register("shaped_forge", () -> new ShapedForgeRecipe.Serializer<>(ShapedForgeRecipe::new));
  public static final RegistryObject<RecipeSerializer<MetalShapelessForgeRecipe>> METAL_SHAPELESS_FORGE = RECIPES.register("metal_shapeless_forge", () -> LoadableRecipeSerializer.of(MetalShapelessForgeRecipe.LOADABLE));
  public static final RegistryObject<RecipeSerializer<MetalShapedForgeRecipe>> METAL_SHAPED_FORGE = RECIPES.register("metal_shaped_forge", () -> new ShapedForgeRecipe.Serializer<>(MetalShapedForgeRecipe::new));
  // furnace
  public static final RegistryObject<RecipeSerializer<SmeltingResultRecipe>> SMELTING = RECIPES.register("smelting", () -> LoadableRecipeSerializer.of(SmeltingResultRecipe.LOADABLE));
  public static final RegistryObject<RecipeSerializer<BlastingResultRecipe>> BLASTING = RECIPES.register("blasting", () -> LoadableRecipeSerializer.of(BlastingResultRecipe.LOADABLE));
  public static final RegistryObject<RecipeSerializer<SmeltingMetalRecyclingRecipe>> SMELTING_METAL_RECYCLING = RECIPES.register("smelting_metal_recycling", () -> LoadableRecipeSerializer.of(SmeltingMetalRecyclingRecipe.LOADABLE));
  public static final RegistryObject<RecipeSerializer<BlastingMetalRecyclingRecipe>> BLASTING_METAL_RECYCLING = RECIPES.register("blasting_metal_recycling", () -> LoadableRecipeSerializer.of(BlastingMetalRecyclingRecipe.LOADABLE));

  // Tinkers' compat
  /** Serializer ID for the metal basin casting recipe, added via the Tinkers' Construct plugin */
  public static final RegistryObject<RecipeSerializer<?>> METAL_CASTING_BASIN = RegistryObject.create(resource("metal_casting_basin"), ForgeRegistries.RECIPE_SERIALIZERS);
  /** Serializer ID for the metal table casting recipe, added via the Tinkers' Construct plugin */
  public static final RegistryObject<RecipeSerializer<?>> METAL_CASTING_TABLE = RegistryObject.create(resource("metal_casting_table"), ForgeRegistries.RECIPE_SERIALIZERS);
  /** Serializer ID for the metal melting recipe, added via the Tinkers' Construct plugin */
  public static final RegistryObject<RecipeSerializer<?>> METAL_MELTING = RegistryObject.create(resource("metal_melting"), ForgeRegistries.RECIPE_SERIALIZERS);
  /** Serializer ID for the copy metal basin casting recipe, added via the Tinkers' Construct plugin */
  public static final RegistryObject<RecipeSerializer<?>> COPY_METAL_BASIN = RegistryObject.create(resource("copy_metal_basin"), ForgeRegistries.RECIPE_SERIALIZERS);
  /** Serializer ID for the copy metal table casting recipe, added via the Tinkers' Construct plugin */
  public static final RegistryObject<RecipeSerializer<?>> COPY_METAL_TABLE = RegistryObject.create(resource("copy_metal_table"), ForgeRegistries.RECIPE_SERIALIZERS);

  // attributes
  /** Multiplier on the distance you can safely fall without damage */
  public static final RegistryObject<Attribute> FALL_DISTANCE_MULTIPLIER = ATTRIBUTES.registerMultiplier("fall_distance_multiplier", true);
  /** Multiplier for knockback this entity takes. Similar to {@link net.minecraft.world.entity.ai.attributes.Attributes#KNOCKBACK_RESISTANCE} but can be used to increase knockback */
  public static final RegistryObject<Attribute> KNOCKBACK_MULTIPLIER = ATTRIBUTES.registerMultiplier("knockback_multiplier", true);
  /** Player modifier data key for mining speed multiplier as an additive percentage boost on mining speed. */
  public static final RegistryObject<Attribute> MINING_SPEED_MULTIPLIER = ATTRIBUTES.registerMultiplier("mining_speed_multiplier", true);
  /** Chance that loot in a loot table will drop. */
  public static final RegistryObject<Attribute> DROP_CHANCE = ATTRIBUTES.registerPercent("drop_chance", 1, false);
  /** Boost to level of looting in loot tables. */
  public static final RegistryObject<Attribute> LOOTING_BOOST = ATTRIBUTES.register("looting_boost", 0, -10, 10, false);
  /** Multiplier to amount of experience gained. */
  public static final RegistryObject<Attribute> EXPERIENCE_MULTIPLIER = ATTRIBUTES.registerMultiplier("experience_multiplier", false);
  /** Changes the amount of damage taken. */
  public static final RegistryObject<Attribute> DETERMINATION  = ATTRIBUTES.register("determination", 1, 0, 2, true);
  /** Above 1, reduces cold damage and increases heat damage. Below 1, reduces heat damage and increases cold damage. */
  public static final RegistryObject<Attribute> WARMTH  = ATTRIBUTES.register("warmth", 1, 0, 2, true);
  /** Applies bonus damage to melee attacks in the form of a fiery attack. */
  public static final RegistryObject<Attribute> HEAT_DAMAGE  = ATTRIBUTES.register("heat_damage", 0, 0, 1024, true);
  /** Changes visibility as seen by monsters. */
  public static final RegistryObject<Attribute> VISIBILITY_MULTIPLIER = ATTRIBUTES.registerMultiplier("visibility_multiplier", false);
  /** Makes it easier or harder for the entity to breath. */
  public static final RegistryObject<Attribute> RESPIRATION = ATTRIBUTES.register("respiration", 0, -10, 10, true);

  // Tinkers' Construct compat
  public static final CastItemObject BRACER_CAST = ITEMS.registerCast("bracer");
  public static final CastItemObject RING_CAST = ITEMS.registerCast("ring");
  public static final CastItemObject SPIKE_CAST = ITEMS.registerCast("spike");

  // loot
  public static final RegistryObject<LootItemConditionType> CONFIG = LOOT_CONDITIONS.register("config", () -> new LootItemConditionType(ConfigEnabledCondition.Serializer.INSTANCE));
  public static final RegistryObject<LootItemConditionType> HAS_LOOT_CONTEXT_SET = LOOT_CONDITIONS.register("has_context_set", () -> new LootItemConditionType(new HasLootContextSetCondition.Serializer()));


  /** Registers any relevant static entries */
  private static void registerMisc(RegisterEvent event) {
    ResourceKey<? extends Registry<?>> key = event.getRegistryKey();
    if (key == Registries.RECIPE_SERIALIZER) {
      // nesting
      MetalEffect.REGISTRY.register(resource("tapping"), TappingMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("storing"), StoringMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("offset"), OffsetMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("capped"), CappedMetalEffect.LOADER);
      // general
      MetalEffect.REGISTRY.register(resource("attribute"), AttributeMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("storing_status_effect"), MobEffectMetalEffect.StoringEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("tapping_status_effect"), MobEffectMetalEffect.TappingEffect.LOADER);
      // specific
      MetalEffect.REGISTRY.register(resource("heal"), HealMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("experience"), ExperienceMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("update_health"), UpdateHealthEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("energy"), EnergyMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("warmth"), WarmthMetalEffect.LOADER);
      // ingredients
      CraftingHelper.register(MetalShapeIngredient.ID, MetalShapeIngredient.SERIALIZER);
      CraftingHelper.register(MetalItemIngredient.ID, MetalItemIngredient.SERIALIZER);
      CraftingHelper.register(FillableIngredient.ID, FillableIngredient.SERIALIZER);
      CraftingHelper.register(ConfigEnabledCondition.Serializer.INSTANCE);

    // creative tabs
    } else if (key == Registries.CREATIVE_MODE_TAB) {
      Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, resource(Metalborn.MOD_ID),
        CreativeModeTab.builder()
          .title(Component.translatable("creative_tab.metalborn"))
          .icon(() -> {
            ItemStack stack = MetalItem.setMetal(new ItemStack(RING), new MetalId(MOD_ID, "pewter"));
            stack.getOrCreateTag().putInt(MetalmindItem.TAG_AMOUNT, 20 * 60 * 2); // about half full
            return stack;
          })
          .displayItems(Registration::addTabItems)
          .build());

    // loot modifiers
    } else if (key == ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS) {
      ForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.get().register(resource("apply_drop_chance"), ApplyDropChanceLootModifier.CODEC);
    }
  }

  /** Adds new attributes to entities */
  private static void addAttributes(EntityAttributeModificationEvent event) {
    event.add(EntityType.PLAYER, MINING_SPEED_MULTIPLIER.get());
    event.add(EntityType.PLAYER, LOOTING_BOOST.get());
    event.add(EntityType.PLAYER, EXPERIENCE_MULTIPLIER.get());
    event.add(EntityType.PLAYER, RESPIRATION.get());
    // general attributes
    addToAll(event, KNOCKBACK_MULTIPLIER);
    addToAll(event, FALL_DISTANCE_MULTIPLIER);
    addToAll(event, DROP_CHANCE);
    addToAll(event, DETERMINATION);
    addToAll(event, WARMTH);
    addToAll(event, HEAT_DAMAGE);
    addToAll(event, VISIBILITY_MULTIPLIER);
  }

  /** Adds an attribute to all entities */
  private static void addToAll(EntityAttributeModificationEvent event, RegistryObject<Attribute> attribute) {
    Attribute attr = attribute.get();
    for (EntityType<? extends LivingEntity> entity : event.getTypes()) {
      event.add(entity, attr, attr.getDefaultValue());
    }
  }

  /** Handles registry renames */
  private static void missingMappings(MissingMappingsEvent event) {
    RegistrationHelper.handleMissingMappings(event, MOD_ID, Registries.ITEM, name -> switch (name) {
      case "lerasium_alloy_nugget" -> CHANGE_FERRING.get();
      case "lerasium_nicrosil_nugget" -> RANDOM_FERRING.get();
      default -> null;
    });
  }

  /** Adds all items to the creative tab */
  private static void addTabItems(ItemDisplayParameters itemDisplayParameters, Output output) {
    Consumer<ItemStack> consumer = output::accept;

    output.accept(METALLIC_ARTS);
    output.accept(FORGE);

    // metals
    output.accept(COPPER_NUGGET);
    output.accept(NETHERITE_NUGGET);
    accept(output, TIN);
    accept(output, PEWTER);
    accept(output, STEEL);
    accept(output, BRONZE);
    accept(output, ROSE_GOLD);
    accept(output, NICROSIL);
    // ores
    output.accept(TIN_ORE);
    output.accept(DEEPSLATE_TIN_ORE);
    output.accept(RAW_TIN);
    output.accept(RAW_TIN_BLOCK);
    // metalminds
    accept(consumer, RING);
    output.accept(IDENTITY_RING);
    accept(consumer, INVESTITURE_RING);
    accept(consumer, BRACER);
    output.accept(IDENTITY_BRACER);
    accept(consumer, INVESTITURE_BRACER);
    accept(consumer, SPIKE);
    accept(consumer, INVESTITURE_SPIKE);
    // unsealed metalminds
    accept(consumer, UNSEALED_RING);
    // ferring nuggets
    output.accept(RANDOM_FERRING);
    accept(consumer, CHANGE_FERRING);

    // Tinkers' Construct compat
    if (ModList.get().isLoaded(Metalborn.TINKERS)) {
      addCasts(output, CastItemObject::get);
      addCasts(output, CastItemObject::getSand);
      addCasts(output, CastItemObject::getRedSand);
    }
  }

  /** Adds all members of an metal object to the given creative tab */
  @SuppressWarnings("SameParameterValue")
  private static void accept(Consumer<ItemStack> consumer, Supplier<? extends MetalItem> item) {
    item.get().addVariants(consumer);
  }

  /** Adds all members of a multi object to the given tab */
  @SuppressWarnings("SameParameterValue")
  private static void accept(Output output, MultiObject<? extends ItemLike> object) {
    object.forEach(output::accept);
  }

  /** Adds adds all casts of the given type to the tab */
  private static void addCasts(CreativeModeTab.Output output, Function<CastItemObject,ItemLike> getter) {
    output.accept(getter.apply(RING_CAST));
    output.accept(getter.apply(BRACER_CAST));
    output.accept(getter.apply(SPIKE_CAST));
  }


  /** Builder that pre-supplies metal properties */
  private static BlockBehaviour.Properties metalBuilder(MapColor color) {
    return BlockBehaviour.Properties.of().mapColor(color).sound(SoundType.METAL).instrument(NoteBlockInstrument.IRON_XYLOPHONE).requiresCorrectToolForDrops().strength(5.0f);
  }

  /** Creates a local item tag */
  private static TagKey<Item> itemTag(String name) {
    return ItemTags.create(resource(name));
  }
}

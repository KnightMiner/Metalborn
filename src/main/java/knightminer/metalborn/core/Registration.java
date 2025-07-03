package knightminer.metalborn.core;


import knightminer.metalborn.Metalborn;
import knightminer.metalborn.block.ForgeBlock;
import knightminer.metalborn.block.ForgeBlockEntity;
import knightminer.metalborn.item.InvestitureMetalmindItem;
import knightminer.metalborn.item.LerasiumAlloyNuggetItem;
import knightminer.metalborn.item.LerasiumNuggetItem;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.item.MetalmindItem;
import knightminer.metalborn.item.PowerMetalmindItem;
import knightminer.metalborn.item.SpikeItem;
import knightminer.metalborn.menu.ForgeMenu;
import knightminer.metalborn.menu.MetalbornMenu;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.effects.AttributeMetalEffect;
import knightminer.metalborn.metal.effects.EnergyMetalEffect;
import knightminer.metalborn.metal.effects.ExperienceMetalEffect;
import knightminer.metalborn.metal.effects.HealMetalEffect;
import knightminer.metalborn.metal.effects.MetalEffect;
import knightminer.metalborn.metal.effects.StoringMetalEffect;
import knightminer.metalborn.metal.effects.TappingMetalEffect;
import knightminer.metalborn.metal.effects.UpdateHealthEffect;
import knightminer.metalborn.recipe.ForgeRecipe;
import knightminer.metalborn.recipe.MetalIngredient;
import knightminer.metalborn.recipe.MetalItemIngredient;
import knightminer.metalborn.recipe.MetalShapedForgeRecipe;
import knightminer.metalborn.recipe.MetalShapelessForgeRecipe;
import knightminer.metalborn.recipe.ShapedForgeRecipe;
import knightminer.metalborn.recipe.ShapelessForgeRecipe;
import knightminer.metalborn.util.AttributeDeferredRegister;
import knightminer.metalborn.util.CastItemObject;
import knightminer.metalborn.util.ItemDeferredRegisterExtension;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
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

  private static final Item.Properties PROPS = new Item.Properties();
  private static final Function<Block, BlockItem> BLOCK_ITEM = block -> new BlockItem(block, PROPS);

  private Registration() {}

  /** Initializes the registration event buses */
  @Internal
  public static void init() {
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(Registration::registerMisc);
    bus.addListener(Registration::addAttributes);
    BLOCKS.register(bus);
    BLOCK_ENTITIES.register(bus);
    ITEMS.register(bus);
    MENUS.register(bus);
    RECIPE_TYPES.register(bus);
    RECIPES.register(bus);
    ATTRIBUTES.register(bus);

    // creative tab is simple enough, just do it inline
    DeferredRegister<CreativeModeTab> creativeTabs = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Metalborn.MOD_ID);
    creativeTabs.register(bus);
    creativeTabs.register(Metalborn.MOD_ID, () ->
      CreativeModeTab.builder()
        .title(Component.translatable("creative_tab.metalborn"))
        .icon(() -> {
          ItemStack stack = MetalItem.setMetal(new ItemStack(RING), new MetalId(MOD_ID, "pewter"));
          stack.getOrCreateTag().putInt(MetalmindItem.TAG_AMOUNT, 20 * 60 * 2); // about half full
          return stack;
        })
        .displayItems(Registration::addTabItems)
        .build());
  }

  // metals
  public static final ItemObject<Item> COPPER_NUGGET = ITEMS.register("copper_nugget");
  public static final ItemObject<Item> NETHERITE_NUGGET = ITEMS.register("netherite_nugget");
  public static final ItemObject<LerasiumNuggetItem> LERASIUM_NUGGET = ITEMS.register("lerasium_nugget", () -> new LerasiumNuggetItem(new Item.Properties()));
  public static final ItemObject<LerasiumAlloyNuggetItem> LERASIUM_ALLOY_NUGGET = ITEMS.register("lerasium_alloy_nugget", () -> new LerasiumAlloyNuggetItem(new Item.Properties()));

  // unique
  public static final MetalItemObject TIN       = BLOCKS.registerMetal("tin",       metalBuilder(MapColor.ICE), BLOCK_ITEM, PROPS);
  public static final MetalItemObject PEWTER    = BLOCKS.registerMetal("pewter",    metalBuilder(MapColor.TERRACOTTA_GREEN), BLOCK_ITEM, PROPS);
  public static final MetalItemObject STEEL     = BLOCKS.registerMetal("steel",     metalBuilder(MapColor.STONE), BLOCK_ITEM, PROPS);
  public static final MetalItemObject BRONZE    = BLOCKS.registerMetal("bronze",    metalBuilder(MapColor.WOOD), BLOCK_ITEM, PROPS);
  public static final MetalItemObject ROSE_GOLD = BLOCKS.registerMetal("rose_gold", metalBuilder(MapColor.TERRACOTTA_WHITE), BLOCK_ITEM, PROPS);

  // ores
  public static final ItemObject<Block> TIN_ORE = BLOCKS.register("tin_ore", BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F), BLOCK_ITEM);
  public static final ItemObject<Block> DEEPSLATE_TIN_ORE = BLOCKS.register("deepslate_tin_ore", BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE), BLOCK_ITEM);
  public static final ItemObject<Block> RAW_TIN_BLOCK = BLOCKS.register("raw_tin_block", BlockBehaviour.Properties.of().mapColor(MapColor.RAW_IRON).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F), BLOCK_ITEM);
  public static final ItemObject<Item> RAW_TIN = ITEMS.register("raw_tin");

  // metal item tags
  public static final TagKey<Item> BRACERS = itemTag("bracers");
  public static final TagKey<Item> RINGS = itemTag("rings");
  public static final TagKey<Item> SPIKES = itemTag("spikes");

  // metal items
  // metalminds
  public static final ItemObject<PowerMetalmindItem> BRACER = ITEMS.register("bracer", () -> new PowerMetalmindItem(new Item.Properties().stacksTo(8), 10));
  public static final ItemObject<PowerMetalmindItem> RING = ITEMS.register("ring", () -> new PowerMetalmindItem(new Item.Properties().stacksTo(8), 1));
  // special metalminds
  public static final ItemObject<InvestitureMetalmindItem> INVESTITURE_BRACER = ITEMS.register("investiture_bracer", () -> new InvestitureMetalmindItem(new Item.Properties().stacksTo(8), 10));
  public static final ItemObject<InvestitureMetalmindItem> INVESTITURE_RING = ITEMS.register("investiture_ring", () -> new InvestitureMetalmindItem(new Item.Properties().stacksTo(8), 1));
  // spikes
  public static final ItemObject<SpikeItem> SPIKE = ITEMS.register("spike", () -> new SpikeItem(new Item.Properties().stacksTo(1)));

  // metal forge
  public static final ItemObject<ForgeBlock> FORGE = BLOCKS.register("forge", () -> new ForgeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F).lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 13 : 0)), BLOCK_ITEM);
  public static final RegistryObject<BlockEntityType<ForgeBlockEntity>> FORGE_BLOCK_ENTITY = BLOCK_ENTITIES.register("forge", ForgeBlockEntity::new, FORGE);

  /** Damage type for metal damaging the player, used in gold's effect */
  public static final ResourceKey<DamageType> METAL_HURT = ResourceKey.create(Registries.DAMAGE_TYPE, resource("metal_hurt"));
  /** Damage type when using a spike */
  public static final ResourceKey<DamageType> ADD_SPIKE = ResourceKey.create(Registries.DAMAGE_TYPE, resource("add_spike"));
  /** Damage type when making a spike from yourself */
  public static final ResourceKey<DamageType> MAKE_SPIKE = ResourceKey.create(Registries.DAMAGE_TYPE, resource("make_spike"));

  // menus
  public static final RegistryObject<MenuType<MetalbornMenu>> METALBORN_MENU = MENUS.register("metalborn", MetalbornMenu::forClient);
  public static final RegistryObject<MenuType<ForgeMenu>> FORGE_MENU = MENUS.register("forge", ForgeMenu::new);

  // recipe
  public static final RegistryObject<RecipeType<ForgeRecipe>> FORGE_RECIPE = RECIPE_TYPES.register("forge", () -> RecipeType.simple(Metalborn.resource("forge")));
  public static final RegistryObject<RecipeSerializer<ShapelessForgeRecipe>> SHAPELESS_FORGE = RECIPES.register("shapeless_forge", () -> LoadableRecipeSerializer.of(ShapelessForgeRecipe.LOADABLE));
  public static final RegistryObject<RecipeSerializer<ShapedForgeRecipe>> SHAPED_FORGE = RECIPES.register("shaped_forge", () -> new ShapedForgeRecipe.Serializer<>(ShapedForgeRecipe::new));
  public static final RegistryObject<RecipeSerializer<MetalShapelessForgeRecipe>> METAL_SHAPELESS_FORGE = RECIPES.register("metal_shapeless_forge", () -> LoadableRecipeSerializer.of(MetalShapelessForgeRecipe.LOADABLE));
  public static final RegistryObject<RecipeSerializer<MetalShapedForgeRecipe>> METAL_SHAPED_FORGE = RECIPES.register("metal_shaped_forge", () -> new ShapedForgeRecipe.Serializer<>(MetalShapedForgeRecipe::new));
  // Tinkers' compat
  /** Serializer ID for the metal basin casting recipe, added via the Tinkers' Construct plugin */
  public static final RegistryObject<RecipeSerializer<?>> METAL_CASTING_BASIN = RegistryObject.create(resource("metal_casting_basin"), ForgeRegistries.RECIPE_SERIALIZERS);
  /** Serializer ID for the metal table casting recipe, added via the Tinkers' Construct plugin */
  public static final RegistryObject<RecipeSerializer<?>> METAL_CASTING_TABLE = RegistryObject.create(resource("metal_casting_table"), ForgeRegistries.RECIPE_SERIALIZERS);
  /** Serializer ID for the metal melting recipe, added via the Tinkers' Construct plugin */
  public static final RegistryObject<RecipeSerializer<?>> METAL_MELTING = RegistryObject.create(resource("metal_melting"), ForgeRegistries.RECIPE_SERIALIZERS);

  // attributes
  /** Multiplier on the distance you can safely fall without damage */
  public static final RegistryObject<Attribute> FALL_DISTANCE_MULTIPLIER = ATTRIBUTES.registerMultiplier("fall_distance_multiplier", true);
  /** Multiplier for knockback this entity takes. Similar to {@link net.minecraft.world.entity.ai.attributes.Attributes#KNOCKBACK_RESISTANCE} but can be used to increase knockback */
  public static final RegistryObject<Attribute> KNOCKBACK_MULTIPLIER = ATTRIBUTES.registerMultiplier("knockback_multiplier", true);
  /** Player modifier data key for mining speed multiplier as an additive percentage boost on mining speed. */
  public static final RegistryObject<Attribute> MINING_SPEED_MULTIPLIER = ATTRIBUTES.registerMultiplier("mining_speed_multiplier", true);

  // Tinkers' Construct compat
  public static final CastItemObject BRACER_CAST = ITEMS.registerCast("bracer");
  public static final CastItemObject RING_CAST = ITEMS.registerCast("ring");
  public static final CastItemObject SPIKE_CAST = ITEMS.registerCast("spike");


  /** Registers any relevant static entries */
  private static void registerMisc(RegisterEvent event) {
    if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
      MetalEffect.REGISTRY.register(resource("tapping"), TappingMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("storing"), StoringMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("attribute"), AttributeMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("heal"), HealMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("experience"), ExperienceMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("update_health"), UpdateHealthEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("energy"), EnergyMetalEffect.LOADER);
      // ingredients
      CraftingHelper.register(MetalIngredient.ID, MetalIngredient.SERIALIZER);
      CraftingHelper.register(MetalItemIngredient.ID, MetalItemIngredient.SERIALIZER);
    }
  }

  /** Adds new attributes to entities */
  private static void addAttributes(EntityAttributeModificationEvent event) {
    event.add(EntityType.PLAYER, MINING_SPEED_MULTIPLIER.get());
    // general attributes
    addToAll(event, KNOCKBACK_MULTIPLIER);
    addToAll(event, FALL_DISTANCE_MULTIPLIER);
  }

  /** Adds an attribute to all entities */
  private static void addToAll(EntityAttributeModificationEvent event, RegistryObject<Attribute> attribute) {
    Attribute attr = attribute.get();
    for (EntityType<? extends LivingEntity> entity : event.getTypes()) {
      event.add(entity, attr, attr.getDefaultValue());
    }
  }

  /** Adds all items to the creative tab */
  private static void addTabItems(ItemDisplayParameters itemDisplayParameters, Output output) {
    Consumer<ItemStack> consumer = output::accept;

    output.accept(FORGE);

    // metals
    output.accept(COPPER_NUGGET);
    output.accept(NETHERITE_NUGGET);
    accept(output, TIN);
    accept(output, PEWTER);
    accept(output, STEEL);
    accept(output, BRONZE);
    accept(output, ROSE_GOLD);
    // ores
    output.accept(TIN_ORE);
    output.accept(DEEPSLATE_TIN_ORE);
    output.accept(RAW_TIN);
    output.accept(RAW_TIN_BLOCK);
    // metalminds
    accept(consumer, RING);
    output.accept(INVESTITURE_RING);
    accept(consumer, BRACER);
    output.accept(INVESTITURE_BRACER);
    accept(consumer, SPIKE);
    // lerasium
    output.accept(LERASIUM_NUGGET);
    accept(consumer, LERASIUM_ALLOY_NUGGET);

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

  /** Makes a damage source from the given key */
  public static DamageSource makeSource(Level level, ResourceKey<DamageType> key) {
    return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key));
  }

  /** Creates a local item tag */
  private static TagKey<Item> itemTag(String name) {
    return ItemTags.create(resource(name));
  }
}

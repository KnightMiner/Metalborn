package knightminer.metalborn.core;


import com.mojang.serialization.Codec;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.block.ForgeBlock;
import knightminer.metalborn.block.ForgeBlockEntity;
import knightminer.metalborn.item.ChangeFerringItem;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.item.MetalbornBookItem;
import knightminer.metalborn.item.RandomFerringItem;
import knightminer.metalborn.item.SatchelItem;
import knightminer.metalborn.item.SatchelItem.SatchelType;
import knightminer.metalborn.item.metalmind.IdentityMetalmindItem;
import knightminer.metalborn.item.metalmind.InvestitureMetalmindItem;
import knightminer.metalborn.item.metalmind.Metalmind;
import knightminer.metalborn.item.metalmind.PowerMetalmindItem;
import knightminer.metalborn.item.metalmind.UnsealedMetalmindItem;
import knightminer.metalborn.item.spike.InvestitureSpikeItem;
import knightminer.metalborn.item.spike.MonsterSpikeItem;
import knightminer.metalborn.item.spike.SpikeItem;
import knightminer.metalborn.json.ConfigEnabledCondition;
import knightminer.metalborn.json.ingredient.FillableIngredient;
import knightminer.metalborn.json.ingredient.MetalItemIngredient;
import knightminer.metalborn.json.ingredient.MetalShapeIngredient;
import knightminer.metalborn.json.loot.ApplyDropChanceLootModifier;
import knightminer.metalborn.json.recipe.cooking.BlastingMetalRecyclingRecipe;
import knightminer.metalborn.json.recipe.cooking.SmeltingMetalRecyclingRecipe;
import knightminer.metalborn.json.recipe.forge.ForgeRecipe;
import knightminer.metalborn.json.recipe.forge.MetalShapedForgeRecipe;
import knightminer.metalborn.json.recipe.forge.MetalShapelessForgeRecipe;
import knightminer.metalborn.json.recipe.forge.ShapedForgeRecipe;
import knightminer.metalborn.json.recipe.forge.ShapelessForgeRecipe;
import knightminer.metalborn.menu.ForgeMenu;
import knightminer.metalborn.menu.MetalbornMenu;
import knightminer.metalborn.metal.MetalHolder;
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
import knightminer.metalborn.util.CastItemObject;
import knightminer.metalborn.util.ItemDeferredRegisterExtension;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.registration.deferred.AttachmentTypeDeferredRegister;
import slimeknights.mantle.registration.deferred.AttributeDeferredRegister;
import slimeknights.mantle.registration.deferred.BlockDeferredRegister;
import slimeknights.mantle.registration.deferred.BlockEntityTypeDeferredRegister;
import slimeknights.mantle.registration.deferred.DataComponentTypeDeferredRegister;
import slimeknights.mantle.registration.deferred.IngredientDeferredRegister;
import slimeknights.mantle.registration.deferred.LootConditionDeferredRegister;
import slimeknights.mantle.registration.deferred.MenuTypeDeferredRegister;
import slimeknights.mantle.registration.deferred.RecipeSerializerDeferredRegister;
import slimeknights.mantle.registration.deferred.RecipeTypeDeferredRegister;
import slimeknights.mantle.registration.holder.DeferredAttachment;
import slimeknights.mantle.registration.holder.DeferredAttribute;
import slimeknights.mantle.registration.holder.DeferredBlockEntity;
import slimeknights.mantle.registration.holder.DeferredDataComponent;
import slimeknights.mantle.registration.holder.DeferredIngredient;
import slimeknights.mantle.registration.holder.DeferredMenu;
import slimeknights.mantle.registration.holder.DeferredRecipeSerializer;
import slimeknights.mantle.registration.holder.DeferredRecipeType;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.MetalItemObject;
import slimeknights.mantle.registration.object.MultiObject;

import java.util.UUID;
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
  private static final RecipeTypeDeferredRegister RECIPE_TYPES = new RecipeTypeDeferredRegister(Metalborn.MOD_ID);
  private static final RecipeSerializerDeferredRegister RECIPES = new RecipeSerializerDeferredRegister(Metalborn.MOD_ID);
  private static final IngredientDeferredRegister INGREDIENTS = new IngredientDeferredRegister(Metalborn.MOD_ID);
  private static final AttributeDeferredRegister ATTRIBUTES = new AttributeDeferredRegister(Metalborn.MOD_ID);
  private static final LootConditionDeferredRegister LOOT_CONDITIONS = new LootConditionDeferredRegister(MOD_ID);
  private static final DataComponentTypeDeferredRegister DATA_COMPONENTS = new DataComponentTypeDeferredRegister(Registries.DATA_COMPONENT_TYPE, MOD_ID);
  private static final AttachmentTypeDeferredRegister ATTACHMENTS = new AttachmentTypeDeferredRegister(MOD_ID);

  private static final Item.Properties PROPS = new Item.Properties();
  private static final Function<Block, BlockItem> BLOCK_ITEM = block -> new BlockItem(block, PROPS);

  private Registration() {}

  /** Initializes the registration event buses */
  @Internal
  public static void init(IEventBus bus) {
    bus.addListener(Registration::registerCapabilities);
    bus.addListener(Registration::registerMisc);
    bus.addListener(Registration::addAttributes);
    BLOCKS.register(bus);
    BLOCK_ENTITIES.register(bus);
    ITEMS.register(bus);
    MENUS.register(bus);
    RECIPE_TYPES.register(bus);
    RECIPES.register(bus);
    INGREDIENTS.register(bus);
    ATTRIBUTES.register(bus);
    LOOT_CONDITIONS.register(bus);
    DATA_COMPONENTS.register(bus);
    ATTACHMENTS.register(bus);
  }

  public static final DeferredItem<Item> METALLIC_ARTS = ITEMS.register("metallic_arts", () -> new MetalbornBookItem(new Properties().stacksTo(1)));

  // metals
  public static final DeferredItem<Item> COPPER_NUGGET = ITEMS.register("copper_nugget");
  public static final DeferredItem<Item> NETHERITE_SHARD, NETHERITE_NUGGET;
  static {
    Item.Properties props = new Item.Properties().fireResistant();
    NETHERITE_SHARD = ITEMS.register("netherite_shard", props);
    NETHERITE_NUGGET = ITEMS.register("netherite_nugget", props);
  }
  public static final DeferredItem<RandomFerringItem> RANDOM_FERRING = ITEMS.register("random_ferring", () -> new RandomFerringItem(new Item.Properties()));
  public static final DeferredItem<ChangeFerringItem> CHANGE_FERRING = ITEMS.register("change_ferring", () -> new ChangeFerringItem(new Item.Properties().fireResistant()));

  // unique
  public static final MetalItemObject TIN       = BLOCKS.registerMetal("tin").block(metalBuilder(MapColor.ICE));
  public static final MetalItemObject PEWTER    = BLOCKS.registerMetal("pewter").block(metalBuilder(MapColor.TERRACOTTA_GREEN));
  public static final MetalItemObject STEEL     = BLOCKS.registerMetal("steel").block(metalBuilder(MapColor.STONE));
  public static final MetalItemObject BRONZE    = BLOCKS.registerMetal("bronze").block(metalBuilder(MapColor.WOOD));
  public static final MetalItemObject ROSE_GOLD = BLOCKS.registerMetal("rose_gold").block(metalBuilder(MapColor.TERRACOTTA_WHITE));
  public static final MetalItemObject NICROSIL  = BLOCKS.registerMetal("nicrosil").block(metalBuilder(MapColor.SNOW));

  // ores
  public static final DeferredBlock<Block> TIN_ORE = BLOCKS.register("tin_ore", BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F), BLOCK_ITEM);
  public static final DeferredBlock<Block> DEEPSLATE_TIN_ORE = BLOCKS.register("deepslate_tin_ore", BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE), BLOCK_ITEM);
  public static final DeferredBlock<Block> RAW_TIN_BLOCK = BLOCKS.register("raw_tin_block", BlockBehaviour.Properties.of().mapColor(MapColor.RAW_IRON).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F), BLOCK_ITEM);
  public static final DeferredItem<Item> RAW_TIN = ITEMS.register("raw_tin");

  // metal item tags
  /** List of metalminds to display in the book, should implement {@link MetalItem} */
  public static final TagKey<Item> METALMINDS = itemTag("metalminds");
  /** Valid items for the bracer slot. Should implement {@link Metalmind} */
  public static final TagKey<Item> BRACERS = itemTag("bracers");
  /** Valid items for the ring slots. Should implement {@link Metalmind} */
  public static final TagKey<Item> RINGS = itemTag("rings");
  /** Valid items for the ring slots. Should implement {@link knightminer.metalborn.item.spike.Spike} */
  public static final TagKey<Item> SPIKES = itemTag("spikes");
  /** Whitelist of items allowed in the satchel. Only used if {@link Config#LIMITED_SATCHELS} is true (not default). */
  public static final TagKey<Item> SATCHEL_ITEMS = itemTag("satchel_items");

  // metal items
  // metalminds
  public static final DeferredItem<PowerMetalmindItem> BRACER = ITEMS.register("bracer", () -> new PowerMetalmindItem(new Item.Properties().stacksTo(8), 10));
  public static final DeferredItem<PowerMetalmindItem> RING = ITEMS.register("ring", () -> new PowerMetalmindItem(new Item.Properties().stacksTo(8), 1));
  public static final DeferredItem<PowerMetalmindItem> UNSEALED_RING = ITEMS.register("unsealed_ring", () -> new UnsealedMetalmindItem(new Item.Properties().stacksTo(8).fireResistant(), 1));
  public static final DeferredItem<PowerMetalmindItem> SOULBOUND_RING = ITEMS.register("soulbound_ring", () -> new PowerMetalmindItem(new Item.Properties().stacksTo(8), 1));
  // investiture metalminds
  public static final DeferredItem<InvestitureMetalmindItem> INVESTITURE_BRACER = ITEMS.register("investiture_bracer", () -> new InvestitureMetalmindItem(new Item.Properties().stacksTo(8), 10));
  public static final DeferredItem<InvestitureMetalmindItem> INVESTITURE_RING = ITEMS.register("investiture_ring", () -> new InvestitureMetalmindItem(new Item.Properties().stacksTo(8), 1));
  public static final DeferredItem<InvestitureMetalmindItem> SOULBOUND_INVESTITURE_RING = ITEMS.register("soulbound_investiture_ring", () -> new InvestitureMetalmindItem(new Item.Properties().stacksTo(8), 1));
  // identity metalminds
  public static final DeferredItem<IdentityMetalmindItem> IDENTITY_BRACER = ITEMS.register("identity_bracer", () -> new IdentityMetalmindItem(new Item.Properties().stacksTo(8), 10));
  public static final DeferredItem<IdentityMetalmindItem> IDENTITY_RING = ITEMS.register("identity_ring", () -> new IdentityMetalmindItem(new Item.Properties().stacksTo(8), 1));
  public static final DeferredItem<IdentityMetalmindItem> SOULBOUND_IDENTITY_RING = ITEMS.register("soulbound_identity_ring", () -> new IdentityMetalmindItem(new Item.Properties().stacksTo(8), 1));
  // spikes
  public static final DeferredItem<MonsterSpikeItem> SPIKE;
  public static final DeferredItem<InvestitureSpikeItem> INVESTITURE_SPIKE;
  public static final DeferredItem<SpikeItem> SOULBOUND_SPIKE;
  static {
    Item.Properties spikeProperties = new Item.Properties().stacksTo(1).attributes(SpikeItem.createAttributes());
    SPIKE = ITEMS.register("spike", () -> new MonsterSpikeItem(spikeProperties));
    INVESTITURE_SPIKE = ITEMS.register("investiture_spike", () -> new InvestitureSpikeItem(spikeProperties));
    SOULBOUND_SPIKE = ITEMS.register("soulbound_spike", () -> new SpikeItem(spikeProperties));
  }

  // metal forge
  public static final DeferredBlock<ForgeBlock> FORGE = BLOCKS.register("forge", () -> new ForgeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F).lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 13 : 0)), BLOCK_ITEM);
  public static final DeferredBlockEntity<ForgeBlockEntity> FORGE_BLOCK_ENTITY = BLOCK_ENTITIES.register("forge", ForgeBlockEntity::new, FORGE);

  // satchels
  public static final EnumObject<SatchelType,Item> SATCHEL = ITEMS.registerEnum(SatchelType.values(), "satchel", type -> {
    Item.Properties props = new Item.Properties().stacksTo(1);
    if (type == SatchelType.STEEL) {
      props.fireResistant();
    }
    return new SatchelItem(props, type);
  });

  // data components
  /** Metal type for a {@link MetalItem} */
  public static final DeferredDataComponent<MetalHolder> METAL = DATA_COMPONENTS.registerPersistent("metal", MetalHolder.CODEC, MetalHolder.STREAM_CODEC);
  /** Current amount for a {@link Metalmind} or spike. Max is determined by the {@link knightminer.metalborn.metal.MetalPower}. */
  public static final DeferredDataComponent<Integer> AMOUNT = DATA_COMPONENTS.registerPersistent("amount", ExtraCodecs.NON_NEGATIVE_INT, ByteBufCodecs.VAR_INT);
  public static final DeferredDataComponent<Boolean> FULL = DATA_COMPONENTS.registerPersistent("full", Codec.BOOL, ByteBufCodecs.BOOL);
  /** Owner UUID for a {@link Metalmind}. Represents a player UUID. */
  public static final DeferredDataComponent<UUID> OWNER = DATA_COMPONENTS.registerPersistent("owner", UUIDUtil.STRING_CODEC, UUIDUtil.STREAM_CODEC);
  /** Display name for {@link #OWNER}. */
  public static final DeferredDataComponent<String> OWNER_NAME = DATA_COMPONENTS.registerPersistent("owner_name", ExtraCodecs.NON_EMPTY_STRING, ByteBufCodecs.STRING_UTF8);

  // data attachments
  public static final DeferredAttachment<MetalbornData> METALBORN_DATA = ATTACHMENTS.registerType("metal", () -> AttachmentType.serializable(MetalbornData::create).copyHandler(MetalbornData.COPY_HANDLER));

  // damage types
  /** Damage type for metal damaging the player, used in gold's effect */
  public static final ResourceKey<DamageType> METAL_HURT = ResourceKey.create(Registries.DAMAGE_TYPE, resource("metal_hurt"));
  /** Damage type when using a spike */
  public static final ResourceKey<DamageType> ADD_SPIKE = ResourceKey.create(Registries.DAMAGE_TYPE, resource("add_spike"));
  /** Damage type when making a spike from yourself */
  public static final ResourceKey<DamageType> MAKE_SPIKE = ResourceKey.create(Registries.DAMAGE_TYPE, resource("make_spike"));
  /** Bonus heat damage dealt to the target */
  public static final ResourceKey<DamageType> MELEE_HEAT = ResourceKey.create(Registries.DAMAGE_TYPE, resource("melee_heat"));
  /** Damage dealt when updating health removes too much */
  public static final ResourceKey<DamageType> UPDATE_HEALTH = ResourceKey.create(Registries.DAMAGE_TYPE, resource("update_health"));

  // menus
  public static final DeferredMenu<MetalbornMenu> METALBORN_MENU = MENUS.register("metalborn", MetalbornMenu::forClient);
  public static final DeferredMenu<ForgeMenu> FORGE_MENU = MENUS.register("forge", ForgeMenu::new);

  // recipe
  public static final DeferredRecipeType<ForgeRecipe> FORGE_RECIPE = RECIPE_TYPES.registerType("forge");
  public static final DeferredRecipeSerializer<ShapelessForgeRecipe> SHAPELESS_FORGE = RECIPES.registerLoadable("shapeless_forge", ShapelessForgeRecipe.LOADABLE);
  public static final DeferredRecipeSerializer<ShapedForgeRecipe> SHAPED_FORGE = RECIPES.registerSimple("shaped_forge", ShapedForgeRecipe.CODEC, ShapedForgeRecipe.STREAM_CODEC);
  public static final DeferredRecipeSerializer<MetalShapelessForgeRecipe> METAL_SHAPELESS_FORGE = RECIPES.registerLoadable("metal_shapeless_forge", MetalShapelessForgeRecipe.LOADABLE);
  public static final DeferredRecipeSerializer<MetalShapedForgeRecipe> METAL_SHAPED_FORGE = RECIPES.registerSimple("metal_shaped_forge", MetalShapedForgeRecipe.CODEC, MetalShapedForgeRecipe.STREAM_CODEC);
  // furnace
  public static final DeferredRecipeSerializer<SmeltingMetalRecyclingRecipe> SMELTING_METAL_RECYCLING = RECIPES.registerLoadable("smelting_metal_recycling", SmeltingMetalRecyclingRecipe.LOADABLE);
  public static final DeferredRecipeSerializer<BlastingMetalRecyclingRecipe> BLASTING_METAL_RECYCLING = RECIPES.registerLoadable("blasting_metal_recycling", BlastingMetalRecyclingRecipe.LOADABLE);
  // ingredient
  public static final DeferredIngredient<FillableIngredient> FILLABLE_INGREDIENT = INGREDIENTS.register("fillable", FillableIngredient.CODEC, FillableIngredient.STREAM_CODEC);
  public static final DeferredIngredient<MetalItemIngredient> METAL_ITEM_INGREDIENT = INGREDIENTS.register("metal_item", MetalItemIngredient.CODEC, MetalItemIngredient.STREAM_CODEC);
  public static final DeferredIngredient<MetalShapeIngredient> METAL_SHAPE_INGREDIENT = INGREDIENTS.register("metal_shape", MetalShapeIngredient.CODEC, MetalShapeIngredient.STREAM_CODEC);

  // Tinkers' compat
  /** Serializer ID for the metal basin casting recipe, added via the Tinkers' Construct plugin */
  public static final DeferredRecipeSerializer<?> METAL_CASTING_BASIN = DeferredRecipeSerializer.createSerializer(resource("metal_casting_basin"));
  /** Serializer ID for the metal table casting recipe, added via the Tinkers' Construct plugin */
  public static final DeferredRecipeSerializer<?> METAL_CASTING_TABLE = DeferredRecipeSerializer.createSerializer(resource("metal_casting_table"));
  /** Serializer ID for the metal melting recipe, added via the Tinkers' Construct plugin */
  public static final DeferredRecipeSerializer<?> METAL_MELTING = DeferredRecipeSerializer.createSerializer(resource("metal_melting"));
  /** Serializer ID for the copy metal basin casting recipe, added via the Tinkers' Construct plugin */
  public static final DeferredRecipeSerializer<?> COPY_METAL_BASIN = DeferredRecipeSerializer.createSerializer(resource("copy_metal_basin"));
  /** Serializer ID for the copy metal table casting recipe, added via the Tinkers' Construct plugin */
  public static final DeferredRecipeSerializer<?> COPY_METAL_TABLE = DeferredRecipeSerializer.createSerializer(resource("copy_metal_table"));

  // attributes
  /** Multiplier on the distance you can safely fall without damage. Similar to {@link net.minecraft.world.entity.ai.attributes.Attributes#FALL_DAMAGE_MULTIPLIER} but applies before safe fall distance. */
  public static final DeferredAttribute<PercentageAttribute> FALL_DISTANCE_MULTIPLIER = ATTRIBUTES.register("fall_distance_multiplier").sync().negative().multiplier();
  /** Chance that loot in a loot table will drop. */
  public static final DeferredAttribute<PercentageAttribute> DROP_CHANCE = ATTRIBUTES.register("drop_chance").percent(1);
  /** Changes the amount of damage taken. */
  public static final DeferredAttribute<RangedAttribute> DETERMINATION  = ATTRIBUTES.register("determination").sync().ranged(1, 0, 2);
  /** Above 1, reduces cold damage and increases heat damage. Below 1, reduces heat damage and increases cold damage. */
  public static final DeferredAttribute<RangedAttribute> WARMTH  = ATTRIBUTES.register("warmth").sync().neutral().ranged(1, 0, 2);
  /** Applies bonus damage to melee attacks in the form of a fiery attack. */
  public static final DeferredAttribute<RangedAttribute> HEAT_DAMAGE  = ATTRIBUTES.register("heat_damage").sync().ranged(0, 0, 1024);
  /** Changes visibility as seen by monsters. */
  public static final DeferredAttribute<PercentageAttribute> VISIBILITY_MULTIPLIER = ATTRIBUTES.register("visibility_multiplier").negative().multiplier();
  /** Makes it easier or harder for the entity to breath. */
  public static final DeferredAttribute<RangedAttribute> RESPIRATION = ATTRIBUTES.register("respiration").sync().ranged(0, -10, 10);

  // Tinkers' Construct compat
  public static final CastItemObject BRACER_CAST = ITEMS.registerCast("bracer");
  public static final CastItemObject RING_CAST = ITEMS.registerCast("ring");
  public static final CastItemObject SPIKE_CAST = ITEMS.registerCast("spike");

  // conditions
  public static final DeferredHolder<LootItemConditionType,LootItemConditionType> CONFIG = LOOT_CONDITIONS.register("config", ConfigEnabledCondition.CODEC);


  /** Registers capabilities for items, blocks and alike */
  private static void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FORGE_BLOCK_ENTITY.get(), ForgeBlockEntity::getItemHandler);
    SATCHEL.forEach((type, item) -> event.registerItem(Capabilities.ItemHandler.ITEM, (stack, context) -> new SatchelItem.SatchelInventory(stack, type), item));
  }

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

    // config conditions
    } else if (key == NeoForgeRegistries.Keys.CONDITION_CODECS) {
      Registry.register(NeoForgeRegistries.CONDITION_SERIALIZERS, CONFIG.getId(), ConfigEnabledCondition.CODEC);

    // creative tabs
    } else if (key == Registries.CREATIVE_MODE_TAB) {
      Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, resource(Metalborn.MOD_ID),
        CreativeModeTab.builder()
          .title(Component.translatable("creative_tab.metalborn"))
          .icon(() -> {
            ItemStack stack = MetalItem.setMetal(RING.toStack(), MetalId.fromNamespaceAndPath(MOD_ID, "pewter"));
            stack.set(AMOUNT, 20 * 60 * 2); // about half full with default metal
            return stack;
          })
          .displayItems(Registration::addTabItems)
          .build());

    // loot modifiers
    } else if (key == NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS) {
      Registry.register(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, resource("apply_drop_chance"), ApplyDropChanceLootModifier.CODEC);
    }
  }

  /** Adds new attributes to entities */
  private static void addAttributes(EntityAttributeModificationEvent event) {
    event.add(EntityType.PLAYER, RESPIRATION);
    // general attributes
    addToAll(event, FALL_DISTANCE_MULTIPLIER);
    addToAll(event, DROP_CHANCE);
    addToAll(event, DETERMINATION);
    addToAll(event, WARMTH);
    addToAll(event, HEAT_DAMAGE);
    addToAll(event, VISIBILITY_MULTIPLIER);
  }

  /** Adds an attribute to all entities */
  private static void addToAll(EntityAttributeModificationEvent event, Holder<Attribute> attribute) {
    for (EntityType<? extends LivingEntity> entity : event.getTypes()) {
      event.add(entity, attribute, attribute.value().getDefaultValue());
    }
  }

  /** Adds all items to the creative tab */
  private static void addTabItems(ItemDisplayParameters itemDisplayParameters, Output output) {
    Consumer<ItemStack> consumer = output::accept;

    output.accept(METALLIC_ARTS);
    output.accept(FORGE);

    // metals
    output.accept(COPPER_NUGGET);
    output.accept(NETHERITE_SHARD);
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
    // satchels
    SATCHEL.forEach((type, item) -> {
      // don't show mist satchels, they are just used on death
      if (type != SatchelType.MIST) {
        output.accept(item);
      }
    });
    // metalminds
    accept(consumer, RING);
    output.accept(IDENTITY_RING);
    accept(consumer, INVESTITURE_RING);
    accept(consumer, BRACER);
    output.accept(IDENTITY_BRACER);
    accept(consumer, INVESTITURE_BRACER);
    accept(consumer, SPIKE);
    accept(consumer, INVESTITURE_SPIKE);
    accept(consumer, SOULBOUND_SPIKE);
    // special metalminds
    accept(consumer, UNSEALED_RING);
    accept(consumer, SOULBOUND_RING);
    output.accept(SOULBOUND_IDENTITY_RING);
    accept(consumer, SOULBOUND_INVESTITURE_RING);
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

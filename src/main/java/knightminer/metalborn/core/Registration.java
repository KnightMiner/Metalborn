package knightminer.metalborn.core;


import knightminer.metalborn.Metalborn;
import knightminer.metalborn.item.LerasiumAlloyNuggetItem;
import knightminer.metalborn.item.LerasiumNuggetItem;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.item.MetalmindItem;
import knightminer.metalborn.item.SpikeItem;
import knightminer.metalborn.metal.effects.AttributeMetalEffect;
import knightminer.metalborn.metal.effects.ExperienceMetalEffect;
import knightminer.metalborn.metal.effects.HealMetalEffect;
import knightminer.metalborn.metal.effects.MetalEffect;
import knightminer.metalborn.metal.effects.RangeMetalEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.registration.deferred.BlockDeferredRegister;
import slimeknights.mantle.registration.deferred.ItemDeferredRegister;
import slimeknights.mantle.registration.deferred.MenuTypeDeferredRegister;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.mantle.registration.object.MetalItemObject;
import slimeknights.mantle.registration.object.MultiObject;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static knightminer.metalborn.Metalborn.resource;

/** Handles any in code registrations */
public class Registration {
  /** Damage type for metal damaging the player, used in gold's effect */
  private static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(Metalborn.MOD_ID);
  private static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(Metalborn.MOD_ID);
  private static final MenuTypeDeferredRegister MENUS = new MenuTypeDeferredRegister(Metalborn.MOD_ID);
  private static final Item.Properties PROPS = new Item.Properties();
  private static final Function<Block, BlockItem> BLOCK_ITEM = block -> new BlockItem(block, PROPS);

  private Registration() {}

  /** Initializes the registration event busses */
  @Internal
  public static void init() {
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(Registration::registerMisc);
    BLOCKS.register(bus);
    ITEMS.register(bus);
    MENUS.register(bus);

    // creative tab is simple enough, just do it inline
    DeferredRegister<CreativeModeTab> creativeTabs = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Metalborn.MOD_ID);
    creativeTabs.register(bus);
    creativeTabs.register(Metalborn.MOD_ID, () ->
      CreativeModeTab.builder()
        .title(Component.translatable("creative_tab.metalborn"))
        .icon(() -> new ItemStack(LERASIUM_NUGGET))
        .displayItems(Registration::addTabItems)
        .build());
  }

  // metals
  public static final ItemObject<Item> COPPER_NUGGET = ITEMS.register("copper_nugget");
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

  // metal items
  public static final ItemObject<MetalmindItem> BRACER = ITEMS.register("bracer", () -> new MetalmindItem(new Item.Properties(), 10));
  public static final ItemObject<MetalmindItem> RING = ITEMS.register("ring", () -> new MetalmindItem(new Item.Properties(), 1));
  public static final ItemObject<SpikeItem> SPIKE = ITEMS.register("spike", () -> new SpikeItem(new Item.Properties()));

  /** Damage type for metal damaging the player, used in gold's effect */
  public static final ResourceKey<DamageType> METAL_HURT = ResourceKey.create(Registries.DAMAGE_TYPE, resource("metal_hurt"));
  /** Damage type when using a spike */
  public static final ResourceKey<DamageType> ADD_SPIKE = ResourceKey.create(Registries.DAMAGE_TYPE, resource("add_spike"));

  // menus
  public static final RegistryObject<MenuType<MetalbornMenu>> MENU = MENUS.register("metalborn", MetalbornMenu::forClient);

  /** Registers any relevant static entries */
  private static void registerMisc(RegisterEvent event) {
    if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
      MetalEffect.REGISTRY.register(resource("range"), RangeMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("attribute"), AttributeMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("heal"), HealMetalEffect.LOADER);
      MetalEffect.REGISTRY.register(resource("experience"), ExperienceMetalEffect.LOADER);
    }
  }

  /** Adds all items to the creative tab */
  private static void addTabItems(ItemDisplayParameters itemDisplayParameters, Output output) {
    Consumer<ItemStack> consumer = output::accept;

    // metals
    output.accept(COPPER_NUGGET);
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
    // lerasium
    output.accept(LERASIUM_NUGGET);
    accept(consumer, LERASIUM_ALLOY_NUGGET);
    // metalminds
    accept(consumer, RING);
    accept(consumer, BRACER);
    accept(consumer, SPIKE);
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


  /** Builder that pre-supplies metal properties */
  private static BlockBehaviour.Properties metalBuilder(MapColor color) {
    return BlockBehaviour.Properties.of().mapColor(color).sound(SoundType.METAL).instrument(NoteBlockInstrument.IRON_XYLOPHONE).requiresCorrectToolForDrops().strength(5.0f);
  }

  /** Makes a damage source from the given key */
  public static DamageSource makeSource(Level level, ResourceKey<DamageType> key) {
    return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(Registration.METAL_HURT));
  }
}

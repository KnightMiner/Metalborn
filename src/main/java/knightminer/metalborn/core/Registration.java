package knightminer.metalborn.core;


import knightminer.metalborn.Metalborn;
import knightminer.metalborn.item.LerasiumAlloyNuggetItem;
import knightminer.metalborn.item.LerasiumNuggetItem;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.metal.effects.AttributeMetalEffect;
import knightminer.metalborn.metal.effects.ExperienceMetalEffect;
import knightminer.metalborn.metal.effects.HealMetalEffect;
import knightminer.metalborn.metal.effects.MetalEffect;
import knightminer.metalborn.metal.effects.RangeMetalEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.registration.deferred.ItemDeferredRegister;
import slimeknights.mantle.registration.object.ItemObject;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static knightminer.metalborn.Metalborn.resource;

/** Handles any in code registrations */
public class Registration {
  private static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(Metalborn.MOD_ID);

  private Registration() {}

  /** Initializes the registration event busses */
  @Internal
  public static void init() {
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(Registration::registerMisc);
    ITEMS.register(bus);

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
  public static final ItemObject<LerasiumNuggetItem> LERASIUM_NUGGET = ITEMS.register("lerasium_nugget", () -> new LerasiumNuggetItem(new Item.Properties()));
  public static final ItemObject<LerasiumAlloyNuggetItem> LERASIUM_ALLOY_NUGGET = ITEMS.register("lerasium_alloy_nugget", () -> new LerasiumAlloyNuggetItem(new Item.Properties()));

  /** Damage type for metal damaging the player, used in gold's effect */
  public static final ResourceKey<DamageType> METAL_HURT = ResourceKey.create(Registries.DAMAGE_TYPE, resource("metal_hurt"));

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

    output.accept(LERASIUM_NUGGET);
    accept(consumer, LERASIUM_ALLOY_NUGGET);
  }

  /** Adds all members of an enum object to the given creative tab */
  @SuppressWarnings("SameParameterValue")
  private static void accept(Consumer<ItemStack> consumer, Supplier<? extends MetalItem> item) {
    item.get().addVariants(consumer);
  }
}

package knightminer.metalborn.util;

import knightminer.metalborn.item.FixedTooltipItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import slimeknights.mantle.registration.deferred.ItemDeferredRegister;

import java.util.function.Supplier;

/** Extension of {@link ItemDeferredRegister} to add custom helpers */
public class ItemDeferredRegisterExtension extends ItemDeferredRegister {
  public ItemDeferredRegisterExtension(String modID) {
    super(modID);
  }

  /**
   * Registers a set of three cast items at once
   * @param name         Base name of cast
   * @return  Object containing casts
   */
  public CastItemObject registerCast(String name) {
    Component tooltip = Component.translatable("item." + getNamespace() + '.' + name + "_cast.tooltip").withStyle(ChatFormatting.GRAY);
    Supplier<Item> constructor = () -> new FixedTooltipItem(new Item.Properties(), tooltip);
    DeferredItem<Item> cast = register(name + "_gold_cast", constructor);
    DeferredItem<Item> sandCast = register(name + "_sand_cast", constructor);
    DeferredItem<Item> redSandCast = register(name + "_red_sand_cast", constructor);
    return new CastItemObject(resource(name), cast, sandCast, redSandCast);
  }
}

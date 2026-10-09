package knightminer.metalborn.item;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.metal.MetalHolder;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.loadable.Loadables;

import java.util.List;
import java.util.function.Consumer;

/** Shared logic for an item with metal variants */
public interface MetalItem extends ItemLike {
  // translation keys
  String KEY_METAL_ID = Metalborn.key("item", "metalmind.metal_id");

  /** Gets the metal contained in this stack */
  static MetalHolder getMetalHolder(ItemStack stack) {
    return stack.getOrDefault(Registration.METAL, MetalHolder.EMPTY);
  }

  /** Gets the metal contained in this stack */
  static MetalId getMetal(ItemStack stack) {
    return getMetalHolder(stack).id();
  }

  /** Gets the metal contained in this stack */
  static MetalPower getMetalPower(ItemStack stack) {
    return getMetalHolder(stack).power();
  }

  /** Sets the metal on the stack */
  static ItemStack setMetal(ItemStack stack, MetalId metal) {
    if (metal == MetalId.NONE) {
      stack.remove(Registration.METAL);
    } else {
      stack.set(Registration.METAL, MetalManager.INSTANCE.getHolder(metal));
    }
    return stack;
  }

  /** Sets the metal on the stack */
  static ItemStack setMetal(ItemStack stack, MetalHolder metal) {
    if (metal == MetalHolder.EMPTY) {
      stack.remove(Registration.METAL);
    } else {
      stack.set(Registration.METAL, metal);
    }
    return stack;
  }

  /**
   * {@return true if the metal is usable by this metal item}
   * @deprecated use {@link #canUseMetal(MetalPower)}
   */
  @Deprecated
  default boolean canUseMetal(MetalId metal) {
    return canUseMetal(MetalManager.INSTANCE.get(metal));
  }

  /** {@return true if the metal is usable by this metal item} */
  default boolean canUseMetal(MetalPower metal) {
    return metal != MetalPower.DEFAULT && !metal.feruchemy().isEmpty();
  }

  /** Creates a stack with the given metal ID */
  default ItemStack withMetal(MetalHolder metal) {
    ItemStack stack = new ItemStack(this);
    setMetal(stack, metal);
    return stack;
  }


  /* Tooltips */

  /** Gets the name with the metal */
  static Component getMetalName(ItemStack stack) {
    String descriptionId = stack.getDescriptionId();
    MetalId metal = getMetal(stack);
    if (metal == MetalId.NONE) {
      return Component.translatable(descriptionId);
    }
    return Component.translatable(descriptionId + ".format", metal.getName());
  }

  /** Adds metal information to the tooltip. Should only be called in advanced tooltips */
  static void appendMetalId(MetalId metal, List<Component> tooltip) {
    tooltip.add(Component.translatable(KEY_METAL_ID, metal.toString()).withStyle(ChatFormatting.DARK_GRAY));
  }

  /** Gets the creator mod ID to show */
  static String getCreatorModId(ItemStack stack) {
    // show metal namespace if present
    MetalId metal = getMetal(stack);
    if (metal != MetalId.NONE) {
      String namespace = metal.getNamespace();
      // skip if it's our namespace, on the chance an addon registers
      if (!Metalborn.MOD_ID.equals(namespace)) {
        return namespace;
      }
    }
    return Loadables.ITEM.getKey(stack.getItem()).getNamespace();
  }


  /* Creative */

  /** Adds all metal variants to the consumer */
  default void addVariants(Consumer<ItemStack> consumer) {
    for (MetalPower power : MetalManager.INSTANCE.getSortedPowers()) {
      if (canUseMetal(power)) {
        consumer.accept(withMetal(power.holder()));
      }
    }
  }
}

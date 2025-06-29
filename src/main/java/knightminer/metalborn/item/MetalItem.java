package knightminer.metalborn.item;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
  // NBT keys
  String TAG_METAL = "metal";

  /** Gets the metal contained in this stack */
  static MetalId getMetal(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    if (tag != null && tag.contains(TAG_METAL, Tag.TAG_STRING)) {
      MetalId id = MetalId.tryParse(tag.getString(TAG_METAL));
      if (id != null) {
        return id;
      }
    }
    return MetalId.NONE;
  }

  /** Sets the metal on the stack */
  static ItemStack setMetal(ItemStack stack, MetalId metal) {
    if (metal != MetalId.NONE) {
      stack.getOrCreateTag().putString(TAG_METAL, metal.toString());
    }
    return stack;
  }

  /** Creates a stack with the given metal ID */
  default ItemStack withMetal(MetalId id) {
    ItemStack stack = new ItemStack(this);
    stack.getOrCreateTag().putString(TAG_METAL, id.toString());
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
      if (!power.feruchemy().isEmpty()) {
        consumer.accept(withMetal(power.id()));
      }
    }
  }
}

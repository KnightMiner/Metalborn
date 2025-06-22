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
import org.jetbrains.annotations.ApiStatus.NonExtendable;

import java.util.List;
import java.util.function.Consumer;

/** Shared logic for an item with metal variants */
public interface MetalItem extends ItemLike {
  // translation keys
  String KEY_METAL_ID = Metalborn.key("item", "metalmind.metal_id");
  // NBT keys
  String TAG_METAL = "metal";

  /** Gets the metal contained in this stack */
  default MetalId getMetal(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    if (tag != null && tag.contains(TAG_METAL, Tag.TAG_STRING)) {
      MetalId id = MetalId.tryParse(tag.getString(TAG_METAL));
      if (id != null) {
        return id;
      }
    }
    return MetalId.NONE;
  }

  /** Creates a stack with the given metal ID */
  default ItemStack withMetal(MetalId id) {
    ItemStack stack = new ItemStack(this);
    stack.getOrCreateTag().putString(TAG_METAL, id.toString());
    return stack;
  }

  /** Adds metal information to the tooltip. Should only be called in advanced tooltips */
  @NonExtendable
  default void appendMetalId(ItemStack stack, List<Component> tooltip) {
    MetalId metal = getMetal(stack);
    if (metal != MetalId.NONE) {
      tooltip.add(Component.translatable(KEY_METAL_ID, metal.toString()).withStyle(ChatFormatting.DARK_GRAY));
    }
  }

  /** Adds all metal variants to the consumer */
  default void addVariants(Consumer<ItemStack> consumer) {
    for (MetalPower power : MetalManager.INSTANCE.getSortedPowers()) {
      if (!power.feruchemy().isEmpty()) {
        consumer.accept(withMetal(power.id()));
      }
    }
  }
}

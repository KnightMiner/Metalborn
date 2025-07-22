package knightminer.metalborn.item.metalmind;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.client.MetalColorManager;
import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static knightminer.metalborn.item.MetalItem.getMetal;

/** Metalmind that grants access to powers instead of granting powers */
public class InvestitureMetalmindItem extends MetalmindItem implements MetalItem {
  public static final MetalId METAL = new MetalId(Metalborn.MOD_ID, "investiture");
  private static final String KEY_INVESTITURE = Metalborn.key("item", "metalmind.investiture");
  private static final Component STORES = Component.translatable(KEY_STORES, METAL.getStores().withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY);

  public InvestitureMetalmindItem(Properties props, int capacityMultiplier) {
    super(props, capacityMultiplier);
  }

  @Override
  public void addVariants(Consumer<ItemStack> consumer) {
    // want the empty item and a spike with each power
    consumer.accept(new ItemStack(this));
    for (MetalPower power : MetalManager.INSTANCE.getSortedPowers()) {
      if (!power.feruchemy().isEmpty() && power.hemalurgyCharge() > 0) {
        ItemStack stack = withMetal(power.id());
        stack.getOrCreateTag().putInt(TAG_AMOUNT, getCapacity(stack));
        consumer.accept(stack);
      }
    }
  }


  /* Metal */

  @Override
  public boolean isSamePower(ItemStack stack1, ItemStack stack2) {
    MetalId metal1 = getMetal(stack1);
    MetalId metal2 = getMetal(stack2);
    return metal1 == MetalId.NONE || metal2 == MetalId.NONE || metal1.equals(metal2);
  }

  @Override
  public Component getStores(ItemStack stack) {
    MetalId metalId = getMetal(stack);
    if (metalId == MetalId.NONE) {
      return METAL.getStores();
    }
    return metalId.getFerring();
  }

  @Override
  public int getCapacity(ItemStack stack) {
    return MetalManager.INSTANCE.get(METAL).capacity() * capacityMultiplier;
  }

  @Override
  public boolean canUse(ItemStack stack, int index, Player player, MetalbornData data) {
    return true;
  }

  @Override
  public boolean onUpdate(ItemStack stack, int index, int newLevel, int oldLevel, Player player, MetalbornData data) {
    MetalId metalId = getMetal(stack);
    // tapping
    if (metalId != MetalId.NONE) {
      // if we were not tapping this power before, grant it
      if (newLevel > 0 && oldLevel <= 0) {
        data.grantPower(metalId, index);
      }
      // if we were are no longer tapping this power, revoke it
      if (newLevel <= 0 && oldLevel > 0) {
        data.revokePower(metalId, index);
      }
    }
    // storing
    // if we were storing ferring type, stop
    if (newLevel >= 0 && oldLevel < 0) {
      data.stopStoringFerring(index);
      return true;
    }
    // store the ferring if the type matches
    if (newLevel < 0 && oldLevel >= 0) {
      MetalId ferringType = data.getFerringType();
      int amount = getAmount(stack);
      // must have a ferring type, and it must match the type we are storing (or we have no current type)
      if (ferringType.isPresent() && (amount == 0 || metalId == MetalId.NONE || ferringType.equals(metalId))) {
        data.storeFerring(index);
        return true;
      }
      return false;
    }
    return true;
  }

  @Override
  protected void emptyMetalmind(CompoundTag tag) {
    super.emptyMetalmind(tag);
    tag.remove(MetalItem.TAG_METAL);
  }

  @Override
  protected void startFillingMetalmind(CompoundTag tag, Player player, MetalbornData data) {
    // onUpdate already ensured this is a valid ferring type, so just store it
    // note we don't store identity for investiture metalminds
    tag.putString(MetalItem.TAG_METAL, data.getFerringType().toString());
  }

  @Override
  protected int fillFrom(ItemStack stack, Player player, ItemStack source, MetalbornData data) {
    int amount = getAmount(source) / stack.getCount();
    if (amount <= 0) {
      return 0;
    }
    int stored = getAmount(stack);
    int capacity = getCapacity(stack);
    // if already full, no work to do. Also prevents us from deleting from an overfilled metalmind
    if (stored >= capacity) {
      return 0;
    }

    // set the metal directly from the source stack; it will always exist if it has amount
    CompoundTag tag = stack.getOrCreateTag();
    if (stored == 0) {
      tag.putString(MetalItem.TAG_METAL, getMetal(source).toString());
    }

    return fill(tag, stored, capacity, amount) * stack.getCount();
  }

  @Override
  protected boolean isTransferrable(ItemStack destination, ItemStack source) {
    // any investiture metalmind is fine, as long as the power being stored matches (which isSamePower handles)
    return source.getItem() instanceof InvestitureMetalmindItem && isSamePower(destination, source);
  }


  /* Tooltip */

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(STORES);

    // active metal
    MetalId metal = getMetal(stack);
    if (metal != MetalId.NONE) {
      tooltip.add(Component.translatable(KEY_INVESTITURE, metal.getFerring().withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
      if (flag.isAdvanced()) {
        MetalItem.appendMetalId(metal, tooltip);
      }
    }

    // amount
    int amount = getAmount(stack);
    appendAmount(METAL, amount, tooltip);
  }
}

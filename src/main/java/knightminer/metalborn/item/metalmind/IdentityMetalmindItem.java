package knightminer.metalborn.item.metalmind;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/** Metalmind that allows storing and tapping identity */
public class IdentityMetalmindItem extends MetalmindItem {
  /** Preferred metal ID for identity metalminds */
  public static final MetalId ALUMINUM = new MetalId(Metalborn.MOD_ID, "aluminum");
  /** Fallback metal ID for identity metalminds */
  public static final MetalId QUARTZ = new MetalId(Metalborn.MOD_ID, "quartz");
  private static final Component STORES = makeStores(QUARTZ);

  public IdentityMetalmindItem(Properties props, int capacityMultiplier) {
    super(props, capacityMultiplier);
  }


  /* Metal */

  @Override
  public boolean isSamePower(ItemStack stack1, ItemStack stack2) {
    // match any metalmind of the same type
    return stack2.getItem() instanceof IdentityMetalmindItem && isSameIdentity(stack1, stack2);
  }

  @Override
  public Component getStores(ItemStack stack) {
    return QUARTZ.getStores();
  }

  /** Gets the power instance for this metalmind */
  private static MetalPower getPower() {
    MetalPower power = MetalManager.INSTANCE.get(ALUMINUM);
    return power != MetalPower.DEFAULT ? power : MetalManager.INSTANCE.get(QUARTZ);
  }

  @Override
  public int getCapacity(ItemStack stack) {
    return getPower().capacity() * capacityMultiplier;
  }

  /** Checks if we can store in the given metalmind */
  private static boolean canStore(ItemStack stack, Player player) {
    return getAmount(stack) == 0 || Objects.equals(getIdentity(stack), player.getUUID());
  }

  @Override
  public Usable canUse(ItemStack stack, int index, Player player, MetalbornData data) {
    // can only tap one metalmind
    // can only store if identity matches or its empty
    return Usable.from(data.canTapIdentity(index), canStore(stack, player));
  }

  @Override
  public boolean onUpdate(ItemStack stack, int index, int newLevel, int oldLevel, Player player, MetalbornData data) {
    // was not tapping identity, now is
    if (newLevel > 0 && oldLevel <= 0) {
      CompoundTag tag = stack.getTag();
      data.updateTappingIdentity(index, getIdentity(stack), tag != null ? tag.getString(TAG_OWNER_NAME) : "");
    }
    // was tapping identity, no longer
    else if (newLevel <= 0 && oldLevel > 0) {
      data.updateTappingIdentity(index, null, "");
    }
    // was storing identity, no longer
    if (newLevel >= 0 && oldLevel < 0) {
      data.stopStoringIdentity(index);
    }
    // was not storing identity, now is
    else if (newLevel < 0 && oldLevel >= 0) {
      if (canStore(stack, player)) {
        data.startStoringIdentity(index);
        return true;
      }
      return false;
    }
    return true;
  }

  /** Called when the metalmind is first filled to set any relevant data */
  @Override
  protected void startFillingMetalmind(CompoundTag tag, Player player, MetalbornData data) {
    tag.putUUID(TAG_OWNER, player.getUUID());
    tag.putString(TAG_OWNER_NAME, player.getGameProfile().getName());
  }

  @Override
  protected boolean isTransferrable(ItemStack destination, ItemStack source) {
    // any identity metalmind is fine, as long as the power being stored matches (which isSamePower handles)
    return source.getItem() instanceof IdentityMetalmindItem && isSameIdentity(destination, source);
  }


  /* Tooltip */

  @Override
  public String getDescriptionId() {
    String id = this.getOrCreateDescriptionId();
    if (MetalManager.INSTANCE.get(ALUMINUM) != MetalPower.DEFAULT) {
      return id + ".aluminum";
    }
    return id + ".quartz";
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag flag) {
    // stores
    tooltip.add(STORES);

    // amount
    int amount = getAmount(stack);
    appendAmount(getPower(), amount, tooltip);

    // owner name
    if (amount > 0) {
      appendOwner(stack, tooltip);
    }
  }
}

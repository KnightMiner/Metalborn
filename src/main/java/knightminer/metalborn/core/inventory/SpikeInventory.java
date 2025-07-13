package knightminer.metalborn.core.inventory;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornCapability;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.core.inventory.SpikeInventory.SpikeStack;
import knightminer.metalborn.item.Spike;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.util.CombatHelper;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/** Inventory of all spikes on the target */
public class SpikeInventory extends MetalInventory<SpikeStack> {
  private static final Component SPIKE_EFFECTS = Metalborn.component("gui", "spikes.effects");
  private static final Component NO_SPIKES = Metalborn.component("gui", "spikes.none").withStyle(ChatFormatting.GRAY);
  private static final String KEY_GRANTS = Metalborn.key("gui", "spikes.grants");
  /** UUID for any attribute debuffs */
  private static final UUID DEBUFF_UUID = UUID.fromString("8c2b195c-dbf0-44a1-9e04-47db33c6bc17");
  /** Health loss per spike */
  private static final int HEALTH_PER_SPIKE = 2;
  /** Language key for the power granting list */

  private final MetalbornCapability data;
  private final LivingEntity entity;
  private final Multiset<MetalId> extraPowers = HashMultiset.create();
  private int lastSize = 0;
  public SpikeInventory(MetalbornCapability data, LivingEntity entity) {
    this.data = data;
    this.entity = entity;
    this.inventory = IntStream.range(0, 4).mapToObj(i -> new SpikeStack()).toList();
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    return stack.isEmpty() || stack.is(Registration.SPIKES);
  }

  /** Checks if the given metal can be used due to spikes */
  public boolean canUse(MetalId metal) {
    return extraPowers.contains(metal);
  }

  /** Updates debuffs related to number of spikes */
  private void updateSpikes() {
    int currentSize = extraPowers.size();
    if (!entity.level().isClientSide && currentSize != lastSize) {
      AttributeInstance instance = entity.getAttribute(Attributes.MAX_HEALTH);
      if (instance == null) {
        Metalborn.LOG.warn("Entity {} does not support attribute {}", entity, Loadables.ATTRIBUTE.getString(Attributes.MAX_HEALTH));
      } else {
        instance.removeModifier(DEBUFF_UUID);
        if (currentSize != 0) {
          instance.addTransientModifier(new AttributeModifier(DEBUFF_UUID, "metalborn.spikes", currentSize * -HEALTH_PER_SPIKE, Operation.ADDITION));
        }
      }
      lastSize = currentSize;
    }
  }

  /** Called when a metal is removed to stop tapping of that metal */
  private void onRemoveMetal(MetalId metal) {
    if (!extraPowers.contains(metal)) {
      data.onRemoved(metal);
    }
  }

  @Override
  public void clear() {
    super.clear();
    extraPowers.clear();
  }

  @Override
  protected void refreshActive() {
    extraPowers.clear();
    for (SpikeStack stack : inventory) {
      if (!stack.stack.isEmpty()) {
        extraPowers.add(stack.metal);
      }
    }
    updateSpikes();
  }

  /** Appends tooltip for all active effects */
  public void getTooltip(List<Component> tooltip) {
    tooltip.add(SPIKE_EFFECTS);
    int health = extraPowers.size() * HEALTH_PER_SPIKE;
    if (health != 0) {
      tooltip.add(Component.translatable(
        "attribute.modifier.take.0",
        ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(health),
      Component.translatable(Attributes.MAX_HEALTH.getDescriptionId())
    ).withStyle(ChatFormatting.RED));
      for (MetalId metal : extraPowers.elementSet()) {
        if (metal != MetalId.NONE) {
          tooltip.add(Component.translatable(KEY_GRANTS, metal.getStores()).withStyle(ChatFormatting.BLUE));
        }
      }
    } else {
      tooltip.add(NO_SPIKES);
    }
  }

  /** Represents a single slot in the inventory */
  class SpikeStack extends StackHolder<SpikeStack> {
    /** Metal granted by the spike. May be none when the spike is not full */
    private MetalId metal = MetalId.NONE;

    @Override
    protected void setStack(ItemStack stack) {
      boolean wasEmpty = this.stack.isEmpty();
      MetalId oldMetal = this.metal;
      if (stack.isEmpty()) {
        // clear previous power
        if (!wasEmpty && extraPowers.remove(oldMetal)) {
          updateSpikes();
          // stop the removed metal from being tapped
          onRemoveMetal(metal);
        }
        // clear stack
        this.stack = ItemStack.EMPTY;
        this.metal = MetalId.NONE;
      } else if (stack.getItem() instanceof Spike spike) {
        this.stack = stack.copy();
        this.metal = spike.isFull(stack) ? spike.getMetal(stack) : MetalId.NONE;
        if (wasEmpty || !oldMetal.equals(this.metal)) {
          // don't remove the old power if it was empty, as we use nones to partially charged spikes
          if (!wasEmpty) {
            extraPowers.remove(oldMetal);
          }
          extraPowers.add(metal);
          updateSpikes();
          onRemoveMetal(oldMetal);
        }
      }
      // you get hurt when adding or removing a spike
      if (!entity.level().isClientSide && (wasEmpty != stack.isEmpty() || !oldMetal.equals(this.metal))) {
        entity.hurt(CombatHelper.damageSource(entity.level(), Registration.ADD_SPIKE), HEALTH_PER_SPIKE);
      }
    }

    @Override
    protected void clear() {
      super.clear();
      metal = MetalId.NONE;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
      super.deserializeNBT(tag);
      metal = stack.getItem() instanceof Spike spike && spike.isFull(stack) ? spike.getMetal(stack) : MetalId.NONE;
    }
  }
}

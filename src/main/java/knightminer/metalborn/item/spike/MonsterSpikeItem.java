package knightminer.metalborn.item.spike;

import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.item.metalmind.MetalmindItem;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static knightminer.metalborn.core.Registration.AMOUNT;
import static knightminer.metalborn.core.Registration.FULL;

/** Represents a hemalurgic spike, which can be filled with power from monsters */
public class MonsterSpikeItem extends SpikeItem implements MetalItem, Spike {

  public MonsterSpikeItem(Properties props) {
    super(props);
  }


  /* Spike */

  /** Gets the amount of charge needed to be full */
  @Override
  public int getMaxCharge(ItemStack stack) {
    return MetalItem.getMetalPower(stack).hemalurgyCharge();
  }

  @Override
  public int setCharge(ItemStack stack, int amount) {
    // zero amount? clean up NBT
    if (amount <= 0) {
      stack.remove(FULL);
      stack.remove(Registration.AMOUNT);
      return 0;
    }

    // if now full, set the full tag
    int max = getMaxCharge(stack);
    if (amount >= getMaxCharge(stack)) {
      stack.set(FULL, true);
      stack.remove(AMOUNT);
      return max;
    }

    // otherwise clear full and set amount
    stack.remove(FULL);
    stack.set(AMOUNT, amount);
    return amount;
  }

  @Override
  public boolean isFull(ItemStack stack) {
    return stack.getOrDefault(FULL, false);
  }

  @Override
  public boolean isEmpty(ItemStack stack) {
    return !stack.getOrDefault(FULL, false) && stack.getOrDefault(AMOUNT, 0) == 0;
  }

  @Override
  public int fill(ItemStack stack, int amount) {
    if (amount <= 0) {
      return 0;
    }
    if (stack.getOrDefault(FULL, false)) {
      return 0;
    }
    int maxCharge = getMaxCharge(stack);
    int stored = MetalmindItem.getAmount(stack);
    int updated = stored + amount;
    if (updated >= maxCharge) {
      stack.set(FULL, true);
      stack.remove(AMOUNT);
      return maxCharge - stored;
    } else {
      stack.set(AMOUNT, updated);
      return amount;
    }
  }

  @Override
  public void addVariants(Consumer<ItemStack> consumer) {
    for (MetalPower power : MetalManager.INSTANCE.getSortedPowers()) {
      if (!power.feruchemy().isEmpty() && power.hemalurgyCharge() > 0) {
        ItemStack stack = withMetal(power.holder());
        consumer.accept(stack.copy());
        stack.set(FULL, true);
        consumer.accept(stack);
      }
    }
  }


  /* Bar */

  @Override
  public boolean isBarVisible(ItemStack stack) {
    return stack.getCount() == 1 && !isFull(stack);
  }

  @Override
  public int getBarWidth(ItemStack stack) {
    if (stack.getOrDefault(FULL, false)) {
      return 13;
    }
    int capacity = getMaxCharge(stack);
    return capacity > 0 ? Math.min(13, MetalmindItem.getAmount(stack) * 13 / capacity) : 0;
  }

  @Override
  public int getBarColor(ItemStack stack) {
    return 0xFF0000;
  }


  /* Stabbing */

  @Override
  public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
    if (stack.getCount() == 1 && !isFull(stack)) {
      MetalPower metal = MetalItem.getMetalPower(stack);
      if (metal.isPresent() && metal == MetalManager.INSTANCE.fromTarget(target.getType())) {
        if (target.isDeadOrDying()) {
          fill(stack, 1);
        }
        return true;
      }
    }
    return false;
  }
}

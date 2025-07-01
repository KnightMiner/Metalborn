package knightminer.metalborn.core.inventory;

import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.core.inventory.MetalmindInventory.MetalmindStack;
import knightminer.metalborn.item.Metalmind;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.stream.IntStream;

/** Inventory of metalminds held on the player */
public class MetalmindInventory extends MetalInventory<MetalmindStack> implements IItemHandlerModifiable, INBTSerializable<ListTag>, ContainerData {
  private final MetalbornData data;
  private final ActiveMetalminds active;
  private final Player player;

  public MetalmindInventory(MetalbornData data, ActiveMetalminds active, Player player) {
    this.data = data;
    this.active = active;
    this.player = player;
    this.inventory = IntStream.range(0, 10).mapToObj(i -> new MetalmindStack()).toList();
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    if (stack.isEmpty()) {
      return true;
    }
    // first two slots are bracers
    if (slot < 2) {
      return stack.is(Registration.BRACER.get());
    }
    // last slot is ring
    return stack.is(Registration.RING.get());
  }

  /** Gets the metalmind slot for the given index */
  public MetalmindStack getSlot(int slot) {
    if (slot >= 0 && slot < inventory.size()) {
      return inventory.get(slot);
    }
    throw new IndexOutOfBoundsException("Slot out of bounds: " + slot);
  }

  @Override
  public void refreshActive() {
    active.clear();
    for (MetalmindStack stack : inventory) {
      stack.refresh();
    }
    active.refresh();
  }

  @Override
  public void clear() {
    super.clear();
    active.clear();
  }

  @Override
  public int get(int i) {
    if (i >= 0 && i < inventory.size()) {
      return inventory.get(i).level;
    }
    return 0;
  }

  @Override
  public void set(int i, int level) {
    if (i >= 0 && i < inventory.size()) {
      inventory.get(i).setLevel(level);
    }
  }

  @Override
  public int getCount() {
    return inventory.size();
  }

  /** Represents a single slot in the metalmind inventory */
  public class MetalmindStack extends StackHolder<MetalmindStack> {
    private Metalmind metalmind = Metalmind.EMPTY;
    private MetalId metal = MetalId.NONE;
    private int level = 0;

    /** Gets the current level */
    public int getLevel() {
      return level;
    }

    /** Gets the current metal */
    public MetalId getMetal() {
      return metal;
    }

    /** Returns true if this metalmind is usable by the player */
    public boolean canUse() {
      return metalmind.canUse(stack, player, data);
    }

    @Override
    protected void setStack(ItemStack stack) {
      if (stack.isEmpty()) {
        setStack(ItemStack.EMPTY, Metalmind.EMPTY);
      } else if (stack.getItem() instanceof Metalmind m) {
        setStack(stack, m);
      }
    }

    /** Updates the item stack */
    private void setStack(ItemStack stack, Metalmind metalmind) {
      // if we are currently tapping or storing, stop as something changed
      if (level != 0 && (this.stack.getItem() != stack.getItem() || !this.metal.equals(metalmind.getMetal(stack)))) {
        active.getMetal(this.metal).update(this, 0);
        level = 0;
      }
      this.stack = stack;
      this.metalmind = metalmind;
      this.metal = this.metalmind.getMetal(stack);
    }

    /** Updates the level on the stack */
    public void setLevel(int newLevel) {
      // ensure the metalmind is actually usable to change the level
      if (!metalmind.canUse(stack, player)) {
        newLevel = 0;
      }
      // if the metalmind is empty, no tapping
      else if (newLevel > 0 && metalmind.isEmpty(stack)) {
        newLevel = 0;
      }
      // if the metalmind is full, no filling
      else if (newLevel < 0 && metalmind.isFull(stack)) {
        newLevel = 0;
      }
      if (newLevel != level) {
        // TODO: let the metalmind choose whether active metals or the power list is used
        active.getMetal(this.metal).update(this, newLevel);
        level = newLevel;
      }
    }

    /**
     * Fills the metalmind by the given amount.
     * @param amount  Amount to add. Always positive.
     * @return Amount that was stored.
     */
    int fill(int amount) {
      if (amount == 0) {
        return 0;
      }
      // can't fill more than our current level
      if (amount > -this.level) {
        amount = -this.level;
      }
      // update the stack
      int used = metalmind.fill(stack, player, amount);
      // if its now empty, stop filling
      if (metalmind.isFull(stack)) {
        level = 0;
      }
      return used;
    }

    /**
     * Updates the amount in the metalmind with the passed value
     * @param amount  Amount to remove. Always positive.
     * @return Amount that was drained.
     */
    int drain(int amount) {
      if (amount == 0) {
        return 0;
      }
      // can't drain more than our current level
      if (amount > this.level) {
        amount = this.level;
      }
      // update the stack
      int used = metalmind.drain(stack, player, amount);
      // if its now empty, stop draining
      if (metalmind.isEmpty(stack)) {
        level = 0;
      }
      return used;
    }

    @Override
    protected void copyFrom(MetalmindStack other) {
      this.stack = other.stack.copy();
      this.metalmind = other.metalmind;
      this.metal = other.metal;
      this.level = other.level;
    }

    /** Refreshes the stack in the active metalmind list */
    private void refresh() {
      if (level != 0 && canUse()) {
        active.getMetal(metal).add(this);
      }
    }

    @Override
    protected void clear() {
      super.clear();
      metalmind = Metalmind.EMPTY;
      metal = MetalId.NONE;
      level = 0;
    }

    @Override
    public CompoundTag serializeNBT() {
      CompoundTag tag = super.serializeNBT();
      tag.putInt("level", level);
      return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
      super.deserializeNBT(tag);
      this.metalmind = stack.getItem() instanceof Metalmind m ? m : Metalmind.EMPTY;
      this.metal = this.metalmind.getMetal(stack);
      if (!stack.isEmpty()) {
        level = tag.getInt("level");
      }
    }
  }
}

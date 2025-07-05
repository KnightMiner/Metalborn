package knightminer.metalborn.core.inventory;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornCapability;
import knightminer.metalborn.core.inventory.MetalmindInventory.MetalmindStack;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntConsumer;

/** Object handling active metalmind effects, both tapping and storing */
public class ActiveMetalminds {
  /** Title for metalmind effect list */
  private static final Component METALMIND_EFFECTS = Metalborn.component("gui", "metalminds.effects");
  /** Used when no effects are active */
  private static final Component NO_METALMINDS = Metalborn.component("gui", "metalminds.none").withStyle(ChatFormatting.GRAY);
  /** Key for tapping a metalmind */
  private static final String KEY_FERRING_TAP = Metalborn.key("gui", "metalminds.investiture.tap");
  /** Key for storing in a metalmind */
  private static final String KEY_FERRING_STORE = Metalborn.key("gui", "metalminds.investiture.store");
  /** Maximum number of unsealed metalminds allowed at once */
  private static final int MAX_UNSEALED = 2;

  /** Index of last reload, so powers know when to refresh. */
  private static int reloadCount = 0;
  /** Reload listener to keep the reload count up to date */
  public static final ResourceManagerReloadListener RELOAD_LISTENER = manager -> reloadCount += 1;

  /** Reference to the parent object */
  private final MetalbornCapability data;

  /** Map of all active effects */
  private final Map<MetalId, ActiveMetalmind> active = new LinkedHashMap<>();
  /** Constructor for adding new effects */
  private final Function<MetalId, ActiveMetalmind> constructor;

  /** Reference to the metalminds currently receiving our ferring power */
  private final List<MetalmindStack> storingFerring = new ArrayList<>();
  /** Last index that received power from our ferring type */
  private int lastFerringIndex = 0;
  /** Indices containing unsealed metalminds */
  private final int[] unsealedIndices = new int[MAX_UNSEALED];

  public ActiveMetalminds(MetalbornCapability data, Player player) {
    this.data = data;
    this.constructor = id -> new ActiveMetalmind(player, id);
    Arrays.fill(unsealedIndices, -1);
  }

  /** Gets the object tracking the given metal */
  public ActiveMetalmind getMetal(MetalId id) {
    return active.computeIfAbsent(id, constructor);
  }

  /** Called when a metal is removed to ensure unusable metalminds are stopped. */
  public void onRemoved(MetalId metal) {
    ActiveMetalmind metalmind = active.get(metal);
    if (metalmind != null) {
      metalmind.validateUsable();
    }
  }


  /* Investiture */

  /** Checks if the ferring power can be stored at the given index */
  public boolean isStoringFerring() {
    return !storingFerring.isEmpty();
  }

  /**
   * Updates the index currently storing ferring power.
   * @param stack  Stack to store
   */
  public void storeFerring(MetalmindStack stack) {
    storingFerring.add(stack);
    data.onRemoved(data.getFerringType());
  }

  /** Stops storing the passed metalmind as a ferring */
  public void stopStoringFerring(MetalmindStack stack) {
    storingFerring.remove(stack);
  }

  /** Stops storing all ferring types */
  public void clearStoringFerring() {
    for (MetalmindStack ferring : storingFerring) {
      ferring.level = 0;
    }
    storingFerring.clear();
  }

  /** Checks if the given metal can be used due to an investiture metalmind */
  public boolean canUse(MetalId metalId) {
    ActiveMetalmind metalmind = active.get(metalId);
    if (metalmind != null) {
      return !metalmind.investitureStacks.isEmpty();
    }
    return false;
  }


  /* Unsealed */

  /** Checks if we can use an unsealed metalmind at the given index */
  public boolean canUseUnsealed(int index) {
    return unsealedIndices[0] == -1 || unsealedIndices[1] == -1
      || unsealedIndices[0] == index || unsealedIndices[1] == index;
  }

  /** Swaps the given value in the unsealed list with the given replacement */
  private void swapUnsealed(int match, int replace) {
    if (unsealedIndices[0] == match) {
      unsealedIndices[0] = replace;
    }
    else if (unsealedIndices[1] == match) {
      unsealedIndices[1] = replace;
    }
  }

  /** Starts using unsealed at the given index */
  public void useUnsealed(int index) {
    swapUnsealed(-1, index);
  }

  /** Stops using unsealed at the given index */
  public void stopUsingUnsealed(int index) {
    swapUnsealed(index, -1);
  }


  /* Updating */

  /** Ticks all active powers */
  public void tick() {
    for (ActiveMetalmind metalmind : active.values()) {
      metalmind.tick();
    }
    // store our ferring power in the next metalmind
    if (!storingFerring.isEmpty()) {
      lastFerringIndex = updateMetalminds(storingFerring, false, 1, lastFerringIndex, NO_ACTION);
    }
  }

  /** Clears all active metalminds */
  public void clear() {
    // practically speaking, when this is called we should have no effects, but better to be safe
    // on the chance there is something here, we will remove it then add it in two calls, which is not the worst case
    for (ActiveMetalmind metalmind : active.values()) {
      metalmind.clear();
    }
    storingFerring.clear();
  }


  /* Menu */

  /** Appends tooltip for all active effects */
  public void getTooltip(List<Component> tooltip) {
    tooltip.add(METALMIND_EFFECTS);
    // storing power to use investiture
    if (!storingFerring.isEmpty()) {
      tooltip.add(Component.translatable(KEY_FERRING_STORE, data.getFerringType().getStores()).withStyle(ChatFormatting.RED));
    }
    // all active powers
    for (ActiveMetalmind metalmind : active.values()) {
      metalmind.getTooltip(tooltip);
    }
    // add empty message if nothing else was added
    if (tooltip.size() == 1) {
      tooltip.add(NO_METALMINDS);
    }
  }

  /** Keeps track of data for a single type of power */
  public class ActiveMetalmind {
    private final Player player;
    /** ID for this effect */
    private final MetalId id;

    /** List of all metalminds being tapped */
    private final List<MetalmindStack> tappingStacks = new ArrayList<>();
    /** List of all metalminds currently storing */
    private final List<MetalmindStack> storingStacks = new ArrayList<>();
    /** List of investiture metalminds granting this power */
    private final List<MetalmindStack> investitureStacks = new ArrayList<>();

    /** Index to resume draining tapping stacks */
    private int resumeTapping = 0;
    /** Index to resume filling storing metalminds */
    private int resumeStoring = 0;

    /** Active power */
    private MetalPower power = null;
    /** Index when the power was last refreshed */
    private int refreshIndex;
    /** Amount of power being tapped */
    private int tapping = 0;
    /** Amount of power being stored */
    private int storing = 0;
    /** Last level used to update attribute effects */
    private int previous = 0;

    /** Consumer for stopping tapping a metalmind */
    private final IntConsumer stopTapping = oldLevel -> tapping -= oldLevel;
    /** Consumer for stopping storing a metalmind */
    private final IntConsumer stopStoring = oldLevel -> storing += oldLevel;

    public ActiveMetalmind(Player player, MetalId id) {
      this.player = player;
      this.id = id;
    }

    /** Fetches the latest power from the metal manager */
    private MetalPower refreshPower() {
      if (power == null || refreshIndex != reloadCount) {
        // worth noting the old effects are not cleared out, so you might need to take some action before it updates
        // could probably clear out the old power and set the new one, but that doesn't seem important
        power = MetalManager.INSTANCE.get(id);
        refreshIndex = reloadCount;
      }
      return power;
    }

    /**
     * Called to update the effect level.
     */
    private void refreshEffect() {
      int current = tapping - storing;
      if (current != previous && !player.level().isClientSide) {
        refreshPower().onChange(player, current, previous);
        previous = current;
      }
    }

    /** Clears all active effects */
    private void clear() {
      tapping = 0;
      storing = 0;
      tappingStacks.clear();
      storingStacks.clear();
      investitureStacks.clear();
      refreshEffect();
    }

    /** Ensures everything in the list is still usable. */
    private static int validateList(List<MetalmindStack> list) {
      int change = 0;
      Iterator<MetalmindStack> iterator = list.iterator();
      while (iterator.hasNext()) {
        MetalmindStack stack = iterator.next();
        if (!stack.canUse()) {
          change += stack.level;
          stack.level = 0;
          iterator.remove();
        }
      }
      return change;
    }

    /** Removes any active powers which are no longer usable. */
    private void validateUsable() {
      tapping -= validateList(tappingStacks);
      storing += validateList(storingStacks);
      refreshEffect();
    }


    /* Activating metalminds */

    /** Updates a metalmind's value in this effect */
    public void update(MetalmindStack stack, int newLevel, int oldLevel) {
      // nothing changed
      if (newLevel == oldLevel) {
        return;
      }
      // update the list placement
      // is tapping, wasn't before
      if (newLevel > 0 && oldLevel <= 0) {
        tappingStacks.add(stack);
      }
      // was tapping, but no longer
      if (newLevel <= 0 && oldLevel > 0) {
        tappingStacks.remove(stack);
      }
      // is storing, wasn't before
      if (newLevel < 0 && oldLevel >= 0) {
        storingStacks.add(stack);
      }
      // was storing, but no longer
      if (newLevel >= 0 && oldLevel < 0) {
        storingStacks.remove(stack);
      }

      // next, update the tapping and storing amounts
      // we split positive from negative to the benefit of ticking,
      // allows you to transfer power from one metalmind to another despite getting no effect
      if (newLevel > 0) {
        tapping += newLevel;
      } else if (newLevel < 0) {
        storing -= newLevel;
      }
      if (oldLevel > 0) {
        tapping -= oldLevel;
      } else if (oldLevel < 0) {
        storing += oldLevel;
      }

      // update level of effects
      refreshEffect();
    }

    /** Called to start granting a power */
    public void grantPower(MetalmindStack stack) {
      investitureStacks.add(stack);
    }

    /** Called to stop granting a power */
    public void revokePower(MetalmindStack stack) {
      investitureStacks.remove(stack);
      if (investitureStacks.isEmpty() && !data.canUse(id)) {
        validateUsable();
      }
    }


    /* Ticking metalminds */

    /** Ticks all metalminds, filling/draining and running tick effects */
    private void tick() {
      if (tapping <= 0 && storing <= 0) {
        return;
      }
      // ensure power is up to date
      refreshPower();

      // if we are active, tick investiture. Doesn't matter if we end up using power as not every power runs every tick
      // TODO: only drain if we have no other source of power
      // TODO: don't drain if the metalminds are all unsealed
      if (!investitureStacks.isEmpty()) {
        // only need 1 power to add this effect, and not currently bothering with round-robin draining; just drain the first one first
        updateMetalminds(investitureStacks, true, 1, 0, NO_ACTION);
      }

      // decide which power to run
      int tapped;
      int stored;
      if (tapping == storing) {
        tapped = tapping;
        stored = storing;
      }
      else if (tapping > storing) {
        // if we are also storing, anything being stored will just move over freely
        tapped = power.onTap(player, tapping - storing) + storing;
        stored = storing;
      } else {
        // if we are also tapping, anything being tapped will just move over freely
        stored = power.onStore(player, storing - tapping) + tapping;
        tapped = tapping;
      }

      // nothing changed? nothing to do
      if (tapped <= 0 && stored <= 0) {
        return;
      }

      // update metalmind amounts
      if (tapped > 0 && !tappingStacks.isEmpty()) {
        resumeTapping = updateMetalminds(tappingStacks, Boolean.TRUE, tapped, resumeTapping, stopTapping);
      }
      if (stored > 0 && !storingStacks.isEmpty()) {
        resumeStoring = updateMetalminds(storingStacks, Boolean.FALSE, stored, resumeStoring, stopStoring);
      }

      // if anything stopped tapping/storing, update the effect
      refreshEffect();
    }

    /** Gets the tooltip to display for this active power */
    private void getTooltip(List<Component> tooltip) {
      // tapping power to use investiture
      if (!investitureStacks.isEmpty()) {
        tooltip.add(Component.translatable(KEY_FERRING_TAP, id.getStores()).withStyle(ChatFormatting.BLUE));
      }
      // active tapping or storing effect
      int level = tapping - storing;
      if (level != 0) {
        refreshPower().getTooltip(player, level, tooltip);
      }
    }
  }

  /** Consumer to perform no action on level remove */
  private static final IntConsumer NO_ACTION = i -> {};

  /** Updates the amount in the list of metalminds */
  private static int updateMetalminds(List<MetalmindStack> metalminds, boolean drain, int amount, int startIndex, IntConsumer onRemove) {
    // if the index is out of bounds, just start from the beginning
    if (startIndex >= metalminds.size()) {
      startIndex = 0;
    }

    ListIterator<MetalmindStack> iterator = metalminds.listIterator(startIndex);
    int resume = 0;
    while (iterator.hasNext()) {
      MetalmindStack stack = iterator.next();
      int oldLevel = stack.level;

      // drain the amount from the stack
      if (drain) {
        amount -= stack.drain(amount);
      } else {
        amount -= stack.fill(amount);
      }

      // if the stack level changed to 0, remove it
      if (stack.level == 0) {
        iterator.remove();
        onRemove.accept(oldLevel);
      }
      // if we ran out of stuff to process, done
      if (amount <= 0) {
        // mark where we left off to resume for next time
        resume = iterator.nextIndex();
        break;
      }
    }

    // if we started from 0 or used everything, we are done
    if (startIndex == 0 || amount <= 0) {
      return resume;
    }

    // if we started from the middle of the list and still have data, cycle back around
    resume = updateMetalminds(metalminds, drain, amount, 0, onRemove);
    // if we managed to cycle the entire list, then we are filling everything, so to make next tick more efficient restart from 0
    if (resume >= startIndex) {
      return 0;
    }
    return resume;
  }
}

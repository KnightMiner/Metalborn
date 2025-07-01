package knightminer.metalborn.core.inventory;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.inventory.MetalmindInventory.MetalmindStack;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Function;

/** Object handling active metalmind effects, both tapping and storing */
public class ActiveMetalminds {
  private static final Component METALMIND_EFFECTS = Metalborn.component("gui", "metalminds.effects");
  private static final Component NO_METALMINDS = Metalborn.component("gui", "metalminds.none").withStyle(ChatFormatting.GRAY);
  /** Index of last reload, so powers know when to refresh. */
  private static int reloadCount = 0;
  /** Reload listener to keep the reload count up to date */
  public static final ResourceManagerReloadListener RELOAD_LISTENER = manager -> reloadCount += 1;

  /** Map of all active effects */
  private final Map<MetalId, ActiveMetalmind> active = new LinkedHashMap<>();
  /** Constructor for adding new effects */
  private final Function<MetalId, ActiveMetalmind> constructor;

  public ActiveMetalminds(Player player) {
    this.constructor = id -> new ActiveMetalmind(player, id);
  }

  /** Gets the object tracking the given metal */
  ActiveMetalmind getMetal(MetalId id) {
    return active.computeIfAbsent(id, constructor);
  }

  /** Ticks all active powers */
  public void tick() {
    for (ActiveMetalmind metalmind : active.values()) {
      metalmind.tick();
    }
  }

  /** Clears all active metalminds */
  public void clear() {
    // practically speaking, when this is called we should have no effects, but better to be safe
    // on the chance there is something here, we will remove it then add it in two calls, which is not the worst case
    for (ActiveMetalmind metalmind : active.values()) {
      metalmind.clear();
    }
  }

  /** Called when a metal is removed to ensure unusable metalminds are stopped. */
  public void onRemoved(MetalId metal) {
    ActiveMetalmind metalmind = active.get(metal);
    if (metalmind != null) {
      metalmind.validateUsable();
    }
  }

  /** Refreshes attributes of all active metalminds */
  void refresh() {
    for (ActiveMetalmind metalmind : active.values()) {
      metalmind.onUpdate(0);
    }
  }

  /** Appends tooltip for all active effects */
  public void getTooltip(List<Component> tooltip) {
    tooltip.add(METALMIND_EFFECTS);
    for (ActiveMetalmind metalmind : active.values()) {
      metalmind.getTooltip(tooltip);
    }
    // add empty message if nothing else was added
    if (tooltip.size() == 1) {
      tooltip.add(NO_METALMINDS);
    }
  }

  /** Keeps track of data for a single type of power */
  static class ActiveMetalmind {
    private final Player player;
    /** ID for this effect */
    private final MetalId id;

    /** List of all metalminds being tapped */
    private final List<MetalmindStack> tappingStacks = new ArrayList<>();
    /** List of all metalminds currently storing */
    private final List<MetalmindStack> storingStacks = new ArrayList<>();

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
     * @param previous  previous value
     */
    private void onUpdate(int previous) {
      int level = tapping - storing;
      if (level != previous && !player.level().isClientSide) {
        refreshPower().onChange(player, level, previous);
      }
    }

    /** Clears all active effects */
    private void clear() {
      int previous = tapping - storing;
      tapping = 0;
      storing = 0;
      tappingStacks.clear();
      storingStacks.clear();
      onUpdate(previous);
    }

    /** Ensures everything in the list is still usable. */
    private static int validateList(List<MetalmindStack> list) {
      int change = 0;
      Iterator<MetalmindStack> iterator = list.iterator();
      while (iterator.hasNext()) {
        MetalmindStack stack = iterator.next();
        if (!stack.canUse()) {
          change += stack.getLevel();
          iterator.remove();
        }
      }
      return change;
    }

    /** Removes any active powers which are no longer usable. */
    private void validateUsable() {
      int previous = tapping - storing;
      tapping -= validateList(tappingStacks);
      storing += validateList(storingStacks);
      onUpdate(previous);
    }

    /** Adds a metalmind stack that was previously absent */
    void add(MetalmindStack stack) {
      int level = stack.getLevel();
      if (level > 0) {
        tapping += level;
        tappingStacks.add(stack);
      } else {
        storing -= level;
        storingStacks.add(stack);
      }
    }

    /** Updates a metalmind's value in this effect */
    void update(MetalmindStack stack, int newLevel) {
      int oldLevel = stack.getLevel();
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
      int previous = tapping - storing;
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

      // finally, update effects
      onUpdate(previous);
    }

    /** Ticks all metalminds, filling/draining and running tick effects */
    private void tick() {
      if (tapping <= 0 && storing <= 0) {
        return;
      }
      // ensure power is up to date
      refreshPower();

      // decide which power to run
      int tapped;
      int stored;
      int previous = tapping - storing;
      if (previous == 0) {
        tapped = tapping;
        stored = storing;
      }
      else if (previous > 0) {
        // if we are also storing, anything being stored will just move over freely
        tapped = power.onTap(player, previous) + storing;
        stored = storing;
      } else {
        // if we are also tapping, anything being tapped will just move over freely
        stored = power.onStore(player, -previous) + tapping;
        tapped = tapping;
      }

      // nothing changed? nothing to do
      if (tapped <= 0 && stored <= 0) {
        return;
      }

      // update metalmind amounts
      if (tapped > 0 && !tappingStacks.isEmpty()) {
        resumeTapping = updateMetalminds(tappingStacks, true, tapped, resumeTapping);
      }
      if (stored > 0 && !storingStacks.isEmpty()) {
        resumeStoring = updateMetalminds(storingStacks, false, stored, resumeStoring);
      }

      // if anything stopped tapping/storing, update the effect
      onUpdate(previous);
    }

    /** Updates the amount in the list of metalminds */
    private int updateMetalminds(List<MetalmindStack> metalminds, boolean drain, int amount, int startIndex) {
      // if the index is out of bounds, just start from the beginning
      if (startIndex >= metalminds.size()) {
        startIndex = 0;
      }

      ListIterator<MetalmindStack> iterator = metalminds.listIterator(startIndex);
      int resume = 0;
      while (iterator.hasNext()) {
        MetalmindStack stack = iterator.next();
        int oldLevel = stack.getLevel();

        // drain the amount from the stack
        if (drain) {
          amount -= stack.drain(amount);
        } else {
          amount -= stack.fill(amount);
        }

        // if the stack level changed to 0, remove it
        if (stack.getLevel() == 0) {
          iterator.remove();
          if (drain) {
            tapping -= oldLevel;
          } else {
            storing += oldLevel;
          }
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
      resume = updateMetalminds(metalminds, drain, amount, 0);
      // if we managed to cycle the entire list, then we are filling everything, so to make next tick more efficient restart from 0
      if (resume >= startIndex) {
        return 0;
      }
      return resume;
    }

    /** Gets the tooltip to display for this active power */
    private void getTooltip(List<Component> tooltip) {
      int level = tapping - storing;
      if (level != 0) {
        refreshPower().getTooltip(player, level, tooltip);
      }
    }
  }
}

package knightminer.metalborn.core;

import knightminer.metalborn.core.MetalmindInventory.MetalmindStack;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import knightminer.metalborn.metal.MetalPower.EffectType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Object handling active metalmind effects, both tapping and storing */
public class ActiveMetalminds {
  /** Index of last reload, so powers know when to refresh. */
  private static int reloadCount = 0;
  /** Reload listener to keep the reload count up to date */
  public static final ResourceManagerReloadListener RELOAD_LISTENER = manager -> reloadCount += 1;

  /** Map of all active effects */
  private final Map<MetalId, ActiveMetalmind> active = new LinkedHashMap<>();
  /** Constructor for adding new effects */
  private final Function<MetalId, ActiveMetalmind> constructor;

  ActiveMetalminds(Player player) {
    this.constructor = id -> new ActiveMetalmind(player, id);
  }

  /** Gets the object tracking the given metal */
  ActiveMetalmind getMetal(MetalId id) {
    return active.computeIfAbsent(id, constructor);
  }

  /** Ticks all active powers */
  void tick() {
    for (ActiveMetalmind metalmind : active.values()) {
      metalmind.tick();
    }
  }

  /** Clears all active metalminds */
  void clear() {
    // practically speaking, when this is called we should have no effects, but better to be safe
    // on the chance there is something here, we will remove it then add it in two calls, which is not the worst case
    for (ActiveMetalmind metalmind : active.values()) {
      metalmind.clear();
    }
  }

  /** Clears active effects from the given metal */
  void clearMetal(MetalId metal) {
    ActiveMetalmind metalmind = active.get(metal);
    if (metalmind != null) {
      metalmind.clear();
    }
  }

  /** Refreshes attributes of all active metalminds */
  void refresh() {
    for (ActiveMetalmind metalmind : active.values()) {
      metalmind.onUpdate(0);
    }
  }

  /** Keeps track of data for a single type of power */
  static class ActiveMetalmind {
    private final Player player;
    /** ID for this effect */
    private final MetalId id;
    /** Mapping of metalminds to each of their respective levels */
    private final List<MetalmindStack> metalminds = new ArrayList<>();

    /** Active power */
    private MetalPower power = null;
    /** Index when the power was last refreshed */
    private int refreshIndex;
    /** Amount of power being tapped, always positive */
    private int tapping = 0;
    /** Amount of power being stored, always negative */
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
      int level = tapping + storing;
      if (level != previous) {
        refreshPower().onChange(EffectType.FERUCHEMY, player, level, previous);
      }
    }

    /** Clears all active effects */
    private void clear() {
      int previous = tapping + storing;
      if (previous != 0) {
        refreshPower().onChange(EffectType.FERUCHEMY, player, 0, previous);
      }
      tapping = 0;
      storing = 0;
      metalminds.clear();
    }

    /** Adds a metalmind stack that was previously absent */
    void add(MetalmindStack stack) {
      int level = stack.getLevel();
      if (level != 0) {
        metalminds.add(stack);
        if (level > 0) {
          tapping += level;
        } else {
          storing += level;
        }
      }
    }

    /** Updates a metalmind's value in this effect */
    void update(MetalmindStack stack, int newLevel) {
      int oldLevel = stack.getLevel();
      // nothing changed
      if (newLevel == oldLevel) {
        return;
      }
      // update metalmind in list
      if (oldLevel == 0) {
        metalminds.add(stack);
      }
      else if (newLevel == 0) {
        metalminds.remove(stack);
      }

      // next, update the tapping and storing amounts
      int previous = tapping + storing;
      // we split positive from negative to the benefit of ticking,
      // allows you to transfer power from one metalmind to another despite getting no effect
      if (newLevel > 0) {
        tapping += newLevel;
      } else if (newLevel < 0) {
        storing += newLevel;
      }
      if (oldLevel > 0) {
        tapping -= oldLevel;
      } else if (oldLevel < 0) {
        storing -= oldLevel;
      }

      // finally, update effects
      onUpdate(previous);
    }

    /** Ticks all metalminds, filling/draining and running tick effects */
    private void tick() {
      if (tapping <= 0 && storing >= 0) {
        return;
      }
      // ensure power is up to date
      refreshPower();

      // tick the effects
      int tapped = 0; // positive number
      int stored = 0; // negative number
      if (tapping > 0) {
        tapped = power.onTick(EffectType.FERUCHEMY, player, tapping);
      }
      if (storing < 0) {
        stored = power.onTick(EffectType.FERUCHEMY, player, storing);
      }
      // nothing changed? nothing to do
      if (tapped == 0 && stored == 0) {
        return;
      }

      // update metalminds based on what effects ran
      int previous = tapping + storing;
      Iterator<MetalmindStack> iterator = metalminds.iterator();
      while (iterator.hasNext()) {
        MetalmindStack stack = iterator.next();
        int level = stack.getLevel();
        // drain the amount from the stack
        if (level > 0) {
          if (tapped > 0) {
            tapped -= stack.drain(tapped);
          }
        } else if (level < 0) {
          if (stored < 0) {
            stored += stack.fill(-stored);
          }
        }
        // if the stack level changed to 0, remove it
        if (stack.getLevel() == 0) {
          iterator.remove();
          if (level > 0) {
            tapping -= level;
          } else {
            storing -= level;
          }
        }
        // if we ran out of stuff to process, also done
        if (tapped <= 0 && stored >= 0) {
          break;
        }
      }
      // should have used up everything
      assert tapped == 0;
      assert stored == 0;

      // if anything stopped tapping/storing, update the effect
      onUpdate(previous);
    }
  }
}

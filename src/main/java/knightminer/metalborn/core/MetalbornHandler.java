package knightminer.metalborn.core;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.inventory.ActiveMetalminds;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingVisibilityEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import slimeknights.mantle.util.CombatHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/** Event handlers for metalborn */
@EventBusSubscriber(modid = Metalborn.MOD_ID, bus = Bus.FORGE)
public class MetalbornHandler {
  /* Identifying ferring type */
  private static final Component CORRECT_METAL = Metalborn.component("item", "ferring.matches");
  private static final Component WRONG_METAL = Metalborn.component("item", "ferring.mismatches");

  /** Called on using an item to inform the player of their metal type */
  @SubscribeEvent
  static void useItem(RightClickItem event) {
    ItemStack stack = event.getItemStack();
    if (!stack.isEmpty()) {
      MetalPower power = MetalManager.INSTANCE.fromIngotOrNugget(stack.getItem());
      if (power.isPresent()) {
        Player player = event.getEntity();
        boolean client = player.level().isClientSide;
        if (!client) {
          if (MetalbornData.getData(player).canUse(power.id())) {
            player.displayClientMessage(CORRECT_METAL, true);
          } else {
            player.displayClientMessage(WRONG_METAL, true);
          }
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(client));
      }
    }
  }

  /** copy caps when the player respawns/returns from the end */
  @SubscribeEvent
  static void playerClone(PlayerEvent.Clone event) {
    Player original = event.getOriginal();
    original.reviveCaps();
    MetalbornData.getData(event.getEntity()).copyFrom(MetalbornData.getData(original), event.isWasDeath());
    original.invalidateCaps();
  }

  /** Handles ticking all active metalminds */
  @SubscribeEvent
  static void playerTick(PlayerTickEvent event) {
    if (event.phase == Phase.START && !event.player.level().isClientSide) {
      MetalbornData.getData(event.player).tick();
    }
  }

  @SubscribeEvent
  static void addReloadListeners(AddReloadListenerEvent event) {
    event.addListener(ActiveMetalminds.RELOAD_LISTENER);
  }

  @SubscribeEvent
  static void onPlayerDrops(LivingDropsEvent event) {
    LivingEntity entity = event.getEntity();
    if (!entity.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && entity instanceof Player && !(entity instanceof FakePlayer)) {
      Collection<ItemEntity> drops = event.getDrops();
      MetalbornData.getData(entity).dropItems(drops);
    }
  }

  @SubscribeEvent
  static void onKnockback(LivingKnockBackEvent event) {
    event.setStrength((float) (event.getStrength() * event.getEntity().getAttributeValue(Registration.KNOCKBACK_MULTIPLIER.get())));
  }

  @SubscribeEvent
  static void onLivingFall(LivingFallEvent event) {
    event.setDistance((float) (event.getDistance() * event.getEntity().getAttributeValue(Registration.FALL_DISTANCE_MULTIPLIER.get())));
  }

  @SubscribeEvent
  static void onBreakSpeed(BreakSpeed event) {
    event.setNewSpeed((float) (event.getNewSpeed() * event.getEntity().getAttributeValue(Registration.MINING_SPEED_MULTIPLIER.get())));
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  static void onLooting(LootingLevelEvent event) {
    // apply looting bonus based on killer
    DamageSource source = event.getDamageSource();
    if (source != null && source.getEntity() instanceof Player killer) {
      double lootingAttribute = killer.getAttributeValue(Registration.LOOTING_BOOST.get());
      // each integer value gives +1 looting
      int lootingBonus = (int)lootingAttribute;
      // partial values are treated as a chance at a higher level
      double higherChance = lootingAttribute % 1;
      if (higherChance > 0 && killer.getRandom().nextFloat() < higherChance) {
        lootingBonus++;
      }
      event.setLootingLevel(Math.max(event.getLootingLevel() + lootingBonus, 0));
    }
  }

  @SubscribeEvent
  static void beforeBlockBreak(BreakEvent event) {
    event.setExpToDrop((int) Math.round(event.getExpToDrop() * event.getPlayer().getAttributeValue(Registration.EXPERIENCE_MULTIPLIER.get())));
  }

  @SubscribeEvent
  static void livingExperienceDrop(LivingExperienceDropEvent event) {
    Player player = event.getAttackingPlayer();
    if (player != null) {
      event.setDroppedExperience((int) Math.round(event.getDroppedExperience() * player.getAttributeValue(Registration.EXPERIENCE_MULTIPLIER.get())));
    }
  }

  @SubscribeEvent
  static void livingAttack(LivingAttackEvent event) {
    LivingEntity target = event.getEntity();
    // heat damage only applies if the target is not currently under iframes
    if (target.invulnerableTime == 0 && !target.fireImmune() && !target.hasEffect(MobEffects.FIRE_RESISTANCE)) {
      DamageSource source = event.getSource();
      // only apply to direct damage (no heating arrows)
      // also skip our damage type to prevent infinite recursion, and require being on fire (not on fire means we are sitting in water and thus avoiding the fire)
      if (!source.isIndirect() && !source.is(Registration.MELEE_HEAT) && source.getEntity() instanceof LivingEntity attacker) {
        double heatDamage = attacker.getAttributeValue(Registration.HEAT_DAMAGE.get());
        if (heatDamage > 0) {
          // if the attacker is not on fire, does less damage
          float finalDamage = (float) heatDamage;
          if (!attacker.isOnFire()) {
            finalDamage /= 2;
          }
          // ignite attacker on attack, downside to heat attacks, but makes future attacks stronger
          attacker.setSecondsOnFire(3);

          // hurt them
          if (target.hurt(CombatHelper.damageSource(Registration.MELEE_HEAT, attacker), finalDamage)) {
            // reset invulnerable time to 0 for the main attack
            target.invulnerableTime = 0;
          }
        }
      }
    }
  }

  @SubscribeEvent
  static void livingHurt(LivingHurtEvent event) {
    LivingEntity entity = event.getEntity();
    // value ranges from 0.05 to 1.95, interpreted as -0.95 to 0.95
    DamageSource source = event.getSource();
    // don't apply to anything bypassing the potion effect or invulnerability
    if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
      float damage = event.getAmount();

      // anything that resistance would block is affected by determination
      if (!source.is(DamageTypeTags.BYPASSES_RESISTANCE) && !source.is(DamageTypeTags.BYPASSES_EFFECTS)) {
        double determination = entity.getAttributeValue(Registration.DETERMINATION.get());
        if (determination != 1) {
          if (determination > 1) {
            // divisor, this leads to less extreme results above 1
            damage = (float) (damage / determination);
          } else {
            // inverse of divisor approach. Leads to more extreme results above 1
            damage = (float) (damage * (2 - determination));
          }
        }
      }

      // warmth will increase fire damage and decrease freezing damage
      if (source.is(DamageTypeTags.IS_FREEZING)) {
        double warmth = entity.getAttributeValue(Registration.WARMTH.get());
        // just use a simple linear scale for freezing damage, means less extreme penalties and more extreme benefits
        if (warmth != 1) {
          damage = (float) (damage * (2 - warmth));
        }
      }
      if (source.is(DamageTypeTags.IS_FIRE)) {
        double warmth = entity.getAttributeValue(Registration.WARMTH.get());
        // just use a simple linear scale for heat damage too
        if (warmth != 1) {
          damage = (float) (damage * warmth);
        }
      }

      event.setAmount(damage);
    }
  }

  @SubscribeEvent
  static void livingVisibility(LivingVisibilityEvent event) {
    double visibility = event.getEntity().getAttributeValue(Registration.VISIBILITY_MULTIPLIER.get());
    if (visibility != 1) {
      event.modifyVisibility(visibility);
    }
  }

  @SubscribeEvent
  static void livingTick(PlayerTickEvent event) {
    if (event.phase == Phase.START) {
      MetalbornData.getData(event.player).setLastWalkDistance(event.player.walkDistO);
    }
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  static void livingBreathe(LivingBreatheEvent event) {
    LivingEntity entity = event.getEntity();
    if (entity.getType() != EntityType.PLAYER) {
      return;
    }
    double respiration = entity.getAttributeValue(Registration.RESPIRATION.get());
    if (respiration == 0) {
      return;
    }
    // not underwater
    if (event.canBreathe()) {
      // if positive, simply increase air supply faster
      if (respiration > 0) {
        event.setRefillAirAmount(Math.min((int) (event.getRefillAirAmount() + respiration), entity.getMaxAirSupply() - entity.getAirSupply()));

      // if walking or running, lose air
      } else if (entity.walkDist - MetalbornData.getData(entity).getLastWalkDistance() > entity.getSpeed()) {
        event.setCanRefillAir(false);
        event.setCanBreathe(false);
        // sprinting is the same above ground and underwater. Just walking is only half as bad as underwater.
        if (!entity.isSprinting()) {
          respiration /= 2;
        }
        // make air last shorter based on level
        if (entity.getRandom().nextFloat() * (1 - respiration) > 1) {
          event.setConsumeAirAmount(event.getConsumeAirAmount() + 1);
        }
      } else {
        // when not moving, can refill air, but it gets slower the more we store breath
        int refill = Math.max(0, (int) (event.getRefillAirAmount() + respiration));
        event.setRefillAirAmount(refill);
        if (refill <= 0) {
          event.setCanRefillAir(false);
        }
      }
    } else {
      // underwater
      if (respiration > 0) {
        // make air last longer
        int decreaseAir = event.getConsumeAirAmount();
        if (decreaseAir > 0 && entity.getRandom().nextFloat() * (respiration + 1) > 1) {
          event.setConsumeAirAmount(decreaseAir - 1);
        }
      } else {
        // make air last shorter
        if (entity.getRandom().nextFloat() * (1 - respiration) > 1) {
          event.setConsumeAirAmount(event.getConsumeAirAmount() + 1);
        }
      }
    }
  }


  /* Soulbound */
  /** NBT key for items to preserve their slot in soulbound */
  private static final String SOULBOUND_SLOT = "metalborn_soulbound";

  /** Called when the player dies to store the slot to return items into */
  @SubscribeEvent
  static void onLivingDeath(LivingDeathEvent event) {
    // this is the latest we can add slot markers to the items so we can return them to slots
    LivingEntity entity = event.getEntity();
    if (!entity.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && entity instanceof Player player && !(player instanceof FakePlayer)) {
      Inventory inventory = player.getInventory();

      // just iterate the whole inventory, no slot specific behavior
      int totalSize = inventory.getContainerSize();
      for (int i = 0; i < totalSize; i++) {
        ItemStack stack = inventory.getItem(i);
        if (!stack.isEmpty() && stack.is(Registration.SOULBOUND)) {
          stack.getOrCreateTag().putInt(SOULBOUND_SLOT, i);
        }
      }
    }
  }

  /** Called when the player dies to store the soulbound items in the original inventory */
  @SubscribeEvent(priority = EventPriority.HIGH)
  static void onPlayerDropItems(LivingDropsEvent event) {
    // TODO: this code is almost entirely duplicated from Tinkers, we should move it to Mantle for 1.21
    // mantle can handle the tag, and tinkers can use the mantle NBT key to implement its modifiers

    // only care about real players with keep inventory off
    LivingEntity entity = event.getEntity();
    if (!entity.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && entity instanceof Player player && !(entity instanceof FakePlayer)) {
      Collection<ItemEntity> drops = event.getDrops();
      Iterator<ItemEntity> iter = drops.iterator();
      Inventory inventory = player.getInventory();
      List<ItemEntity> takenSlot = new ArrayList<>();
      while (iter.hasNext()) {
        ItemEntity itemEntity = iter.next();
        ItemStack stack = itemEntity.getItem();
        // find items with our soulbound tag set and move them back into the inventory, will move them over later
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(SOULBOUND_SLOT, Tag.TAG_ANY_NUMERIC)) {
          int slot = tag.getInt(SOULBOUND_SLOT);
          // return the tool to its requested slot if possible, remove from the drops
          if (inventory.getItem(slot).isEmpty()) {
            inventory.setItem(slot, stack);
          } else {
            // hold off on handling items that did not get the requested slot for now
            // want to make sure they don't get in the way of items that have not yet been seen
            takenSlot.add(itemEntity);
          }
          iter.remove();
          // don't clear the tag yet, we need it one last time for player clone
        }
      }
      // handle items that did not get their requested slot last, to ensure they don't take someone else's slot while being added to a default
      for (ItemEntity itemEntity : takenSlot) {
        ItemStack stack = itemEntity.getItem();
        if (!inventory.add(stack)) {
          // last resort, somehow we just cannot put the stack anywhere, so drop it on the ground
          // this should never happen, but better to be safe
          // ditch the soulbound slot tag, to prevent item stacking issues
          CompoundTag tag = stack.getTag();
          if (tag != null) {
            tag.remove(SOULBOUND_SLOT);
            if (tag.isEmpty()) {
              stack.setTag(null);
            }
          }
          drops.add(itemEntity);
        }
      }
    }
  }

  /** Called when the new player is created to fetch the soulbound item from the old */
  @SubscribeEvent(priority = EventPriority.HIGH)
  static void onPlayerClone(PlayerEvent.Clone event) {
    // TODO: same as above, lots of duplicated code with tinkers, move to Mantle
    if (!event.isWasDeath()) {
      return;
    }
    Player original = event.getOriginal();
    Player clone = event.getEntity();
    // inventory already copied
    if (clone.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || original.isSpectator()) {
      return;
    }
    // find items with the soulbound tag set and move them over
    Inventory originalInv = original.getInventory();
    Inventory cloneInv = clone.getInventory();
    int size = Math.min(originalInv.getContainerSize(), cloneInv.getContainerSize()); // not needed probably, but might as well be safe
    List<ItemStack> takenSlot = new ArrayList<>();
    for(int i = 0; i < size; i++) {
      ItemStack stack = originalInv.getItem(i);
      if (!stack.isEmpty()) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(SOULBOUND_SLOT, Tag.TAG_ANY_NUMERIC)) {
          if (cloneInv.getItem(i).isEmpty()) {
            cloneInv.setItem(i, stack);
          } else {
            takenSlot.add(stack);
          }
          // remove the slot tag, clear the tag if needed
          tag.remove(SOULBOUND_SLOT);
          if (tag.isEmpty()) {
            stack.setTag(null);
          }
        }
      }
    }

    // handle items that did not get their requested slot last, to ensure they don't take someone else's slot while being added to a default
    for (ItemStack stack : takenSlot) {
      if (!cloneInv.add(stack)) {
        // last resort, somehow we just cannot put the stack anywhere, so drop it on the ground
        // this should never happen, but better to be safe
        clone.drop(stack, false);
      }
    }
  }
}

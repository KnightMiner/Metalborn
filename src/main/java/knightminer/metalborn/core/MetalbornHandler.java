package knightminer.metalborn.core;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.inventory.ActiveMetalminds;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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

import java.util.Collection;

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
      if (!source.isIndirect() && !source.is(Registration.MELEE_HEAT) && source.getEntity() instanceof LivingEntity attacker && attacker.isOnFire()) {
        double heatDamage = attacker.getAttributeValue(Registration.HEAT_DAMAGE.get());
        if (heatDamage > 0) {
          boolean fakeFire = false;
          if (!target.isOnFire()) {
            target.setSecondsOnFire(1);
          }
          // hurt them
          if (target.hurt(CombatHelper.damageSource(Registration.MELEE_HEAT, attacker), (float) heatDamage)) {
            // reset invulnerable time to 0 for the main attack
            target.invulnerableTime = 0;
            target.setSecondsOnFire((int) heatDamage);
          } else if (fakeFire) {
            target.setSecondsOnFire(0);
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
}

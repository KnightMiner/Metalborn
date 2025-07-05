package knightminer.metalborn.core;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.inventory.ActiveMetalminds;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
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
    if (source != null && source.getEntity() instanceof LivingEntity killer) {
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
  static void livingHurt(LivingHurtEvent event) {
    LivingEntity entity = event.getEntity();
    // value ranges from 0.05 to 1.95, interpreted as -0.95 to 0.95
    double protection = entity.getAttributeValue(Registration.DETERMINATION.get());
    DamageSource source = event.getSource();
    // don't apply to anything bypassing the potion effect or invulnerability
    if (protection != 1 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.is(DamageTypeTags.BYPASSES_RESISTANCE) && !source.is(DamageTypeTags.BYPASSES_EFFECTS)) {
      // above 1, divisor on damage. Means that +100% is half damage
      if (protection > 1) {
        event.setAmount((float) (event.getAmount() / protection));
      } else {
        // below 1, apply the inverse formula so it doesn't scale up damage too quickly
        // way it works is if you would take half as much for the positive, its twice as much in the negative
        event.setAmount((float) (event.getAmount() * (2 - protection)));
      }
    }
  }
}

package knightminer.metalborn.core;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.inventory.ActiveMetalminds;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
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
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
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
          if (MetalbornCapability.getData(player).canUse(power.id())) {
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

  /** Handles ticking all active metalminds */
  @SubscribeEvent
  static void playerTick(PlayerTickEvent event) {
    if (event.phase == Phase.START && !event.player.level().isClientSide) {
      MetalbornCapability.getData(event.player).tick();
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
      MetalbornCapability.getData(entity).dropItems(drops);
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
}

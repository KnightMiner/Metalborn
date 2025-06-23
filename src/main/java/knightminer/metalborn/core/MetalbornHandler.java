package knightminer.metalborn.core;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

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
}

package knightminer.metalborn.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

/** Item that can be eaten, but is not a food */
public abstract class ConsumableItem extends Item {
  public ConsumableItem(Properties props) {
    super(props);
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    player.startUsingItem(hand);
    return InteractionResultHolder.consume(player.getItemInHand(hand));
  }

  @Override
  public UseAnim getUseAnimation(ItemStack pStack) {
    return UseAnim.EAT;
  }

  @Override
  public int getUseDuration(ItemStack pStack) {
    return 32;
  }

  /** Called when this item is eaten to perform effects */
  protected abstract void onEat(ItemStack stack, LivingEntity entity);

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
    level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), getEatingSound(), SoundSource.NEUTRAL, 1, 1 + (level.random.nextFloat() - level.random.nextFloat()) * 0.4f);
    onEat(stack, entity);
    // players get stats and more sound
    if (entity instanceof Player player) {
      player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
      level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
      if (player instanceof ServerPlayer server) {
        CriteriaTriggers.CONSUME_ITEM.trigger(server, stack);
      }
      // shrink the item
      if (!player.getAbilities().instabuild) {
        stack.shrink(1);
      }
    } else {
      stack.shrink(1);
    }
    entity.gameEvent(GameEvent.EAT);
    return stack;
  }
}

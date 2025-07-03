package knightminer.metalborn.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.item.metalmind.InvestitureMetalmindItem;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.command.MantleCommand;

import java.util.Collection;

/** Command for working with metal items */
public class MetalItemCommand {
  /** Success for getting metal type */
  private static final String GET = Metalborn.key("command", "metal.get");
  /** Success for setting 1 target */
  private static final String SET_SINGLE = Metalborn.key("command", "metal.set.single");
  /** Success for setting multiple targets */
  private static final String SET_MULTIPLE = Metalborn.key("command", "metal.set.multiple");
  /** Failure for not holding a metal item */
  private static final DynamicCommandExceptionType ERROR_NO_METAL = new DynamicCommandExceptionType(entity -> Component.translatable(Metalborn.key("command", "metal.error.not_metal"), entity));
  /** Failure for nothing happening */
  private static final SimpleCommandExceptionType NO_ACTION = new SimpleCommandExceptionType(Metalborn.component("command", "metal.error.no_action"));

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(sender -> sender.hasPermission(MantleCommand.PERMISSION_GAME_COMMANDS))
      // metal get [target]
      .then(Commands.literal("get")
        .executes(context -> getMetal(context, context.getSource().getPlayerOrException()))
        .then(Commands.argument("target", EntityArgument.player())
          .executes(context -> getMetal(context, EntityArgument.getPlayer(context, "target")))))
      // metal set <targets> <metal>
      .then(Commands.literal("set")
        .then(Commands.argument("targets", EntityArgument.players())
          .then(Commands.argument("metal", MetalArgument.metal())
            .executes(MetalItemCommand::setMetal))));
  }

  /**
   * Gets the ferring type for the target.
   * @param context  Command context
   * @return Metal index.
   */
  private static int getMetal(CommandContext<CommandSourceStack> context, Player player) throws CommandSyntaxException {
    ItemStack stack = player.getMainHandItem();
    if (stack.isEmpty()) {
      throw MetalmindCommand.ERROR_NO_ITEM.create(player);
    }
    Item item = stack.getItem();
    if (!(item instanceof MetalItem) && !(item instanceof InvestitureMetalmindItem)) {
      throw ERROR_NO_METAL.create(player);
    }
    MetalId metal = MetalItem.getMetal(stack);
    context.getSource().sendSuccess(() -> Component.translatable(GET, player.getDisplayName(), metal.getName(), metal), true);
    return metal == MetalId.NONE ? 0 : 1;
  }

  /**
   * Sets the ferring type for the target.
   * @param context  Command context
   * @return Number of entities targeted.
   */
  private static int setMetal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    MetalId metal = MetalArgument.getPower(context, "metal").id();
    Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
    int successes = 0;
    boolean single = players.size() == 1;
    for (Player player : players) {
      ItemStack stack = player.getMainHandItem();
      if (!stack.isEmpty()) {
        Item item = stack.getItem();
        if (item instanceof MetalItem || item instanceof InvestitureMetalmindItem) {
          MetalItem.setMetal(stack, metal);
          successes++;
        } else if (single) {
          throw ERROR_NO_METAL.create(player);
        }
      } else if (single) {
        throw MetalmindCommand.ERROR_NO_ITEM.create(player);
      }
    }

    // if nothing happened, error
    if (successes == 0) {
      throw NO_ACTION.create();
    }

    // success message
    CommandSourceStack source = context.getSource();
    if (successes == 1) {
      source.sendSuccess(() -> Component.translatable(SET_SINGLE, players.iterator().next().getDisplayName(), metal.getName(), metal), true);
    } else {
      int successFinal = successes;
      source.sendSuccess(() -> Component.translatable(SET_MULTIPLE, successFinal, metal.getName(), metal), true);
    }
    return successes;
  }
}

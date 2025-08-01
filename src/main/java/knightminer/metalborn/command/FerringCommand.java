package knightminer.metalborn.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.command.MantleCommand;

import java.util.Collection;
import java.util.List;

/** Command for working with metal powers */
public class FerringCommand {
  /** Success for getting ferring type */
  private static final String GET = Metalborn.key("command", "ferring.get");
  /** Success for setting 1 target */
  private static final String SET_SINGLE = Metalborn.key("command", "ferring.set.single");
  /** Success for setting multiple targets */
  private static final String SET_MULTIPLE = Metalborn.key("command", "ferring.set.multiple");
  /** Success for randomizing 1 target */
  private static final String RANDOM_SINGLE = Metalborn.key("command", "ferring.random.single");
  /** Success for randomizing multiple targets */
  private static final String RANDOM_MULTIPLE = Metalborn.key("command", "ferring.random.multiple");

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(sender -> sender.hasPermission(MantleCommand.PERMISSION_GAME_COMMANDS))
      // ferring get [target]
      .then(Commands.literal("get")
        .executes(context -> getFerring(context, context.getSource().getPlayerOrException()))
        .then(Commands.argument("target", EntityArgument.player()).executes(context -> getFerring(context, EntityArgument.getPlayer(context, "target")))))
      // ferring set <targets> [metal]
      .then(Commands.literal("set")
        .then(Commands.argument("targets", EntityArgument.players())
          .executes(context -> setFerring(context, null))
          .then(Commands.argument("metal", MetalArgument.metal())
            .executes(context -> setFerring(context, MetalArgument.getPower(context, "metal").id())))))
      // ferring remove <targets>
      .then(Commands.literal("remove")
        .executes(context -> setFerring(context, MetalId.NONE, List.of(context.getSource().getPlayerOrException())))
        .then(Commands.argument("targets", EntityArgument.players())
          .executes(context -> setFerring(context, MetalId.NONE))));
  }

  /**
   * Gets the ferring type for the target.
   * @param context  Command context
   * @return Metal index.
   */
  private static int getFerring(CommandContext<CommandSourceStack> context, Player player) {
    MetalPower power = MetalManager.INSTANCE.get(MetalbornData.getData(player).getFerringType());
    context.getSource().sendSuccess(() -> {
      MetalId id = power.id();
      return Component.translatable(GET, player.getDisplayName(), id.getFerring(), id);
    }, true);
    return power.index();
  }

  /**
   * Sets the ferring type for the target.
   * @param context  Command context
   * @param metal    Metal to set, if null will randomly set the metal
   * @return Number of entities targeted.
   */
  private static int setFerring(CommandContext<CommandSourceStack> context, @Nullable MetalId metal) throws CommandSyntaxException {
    return setFerring(context, metal, EntityArgument.getPlayers(context, "targets"));
  }

  /**
   * Sets the ferring type for the list of players.
   * @param context  Command context
   * @param metal    Metal to set, if null will randomly set the metal
   * @param players  List of players to target
   * @return Number of entities targeted.
   */
  private static int setFerring(CommandContext<CommandSourceStack> context, @Nullable MetalId metal, Collection<? extends Player> players) throws CommandSyntaxException {
    // set metal for each player
    for (Player player : players) {
      // if null, independently choose type for each
      MetalId targetMetal = metal;
      if (targetMetal == null) {
        targetMetal = MetalManager.INSTANCE.getRandomFerring(player.getRandom()).id();
      }
      MetalbornData.getData(player).setFerringType(targetMetal);
    }

    // success message
    CommandSourceStack source = context.getSource();
    int size = players.size();
    if (size == 1) {
      // null metal with 1 player? just display what we ended up at
      if (metal == null) {
        source.sendSuccess(() -> Component.translatable(RANDOM_SINGLE, players.iterator().next().getDisplayName()), true);
      } else {
        source.sendSuccess(() -> Component.translatable(SET_SINGLE, players.iterator().next().getDisplayName(), metal.getFerring(), metal), true);
      }
    } else {
      if (metal == null) {
        source.sendSuccess(() -> Component.translatable(RANDOM_MULTIPLE, players.size()), true);
      } else {
        source.sendSuccess(() -> Component.translatable(SET_MULTIPLE, players.size(), metal.getFerring(), metal), true);
      }
    }
    return size;
  }
}

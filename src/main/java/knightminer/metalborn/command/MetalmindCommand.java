package knightminer.metalborn.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.item.SpikeItem;
import knightminer.metalborn.item.metalmind.MetalmindItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.command.MantleCommand;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/** Command for filling and draining metalminds and spikes */
public class MetalmindCommand {
  /** Success root key */
  private static final String KEY_PREFIX = Metalborn.key("command", "");
  /** Key part for a single success, with entity name and amount */
  private static final String KEY_SINGLE = "success.single.";
  /** Key part for multiple success, with entity count and amount */
  private static final String KEY_MULTIPLE = "success.multiplie.";
  /** Failure for holding no item */
  static final DynamicCommandExceptionType ERROR_NO_ITEM = new DynamicCommandExceptionType(entity -> Component.translatable(Metalborn.key("command", "error.itemless"), entity));
  /** Failure for not holding a metalmind */
  private static final DynamicCommandExceptionType ERROR_NO_METALMIND = new DynamicCommandExceptionType(entity -> Component.translatable(Metalborn.key("command", "metalmind.error.not_metalmind"), entity));
  /** Failure for not holding a spike */
  private static final DynamicCommandExceptionType ERROR_NO_SPIKE = new DynamicCommandExceptionType(entity -> Component.translatable(Metalborn.key("command", "spike.error.not_spike"), entity));
  // Failure for nothing happening
  private static final SimpleCommandExceptionType NO_ACTION_METALMIND = new SimpleCommandExceptionType(Metalborn.component("command", "metalmind.error.no_action"));
  private static final SimpleCommandExceptionType NO_ACTION_SPIKE = new SimpleCommandExceptionType(Metalborn.component("command", "spike.error.no_action"));

  /**
   * Registers this sub command with the root command
   * @param root  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
    // metalmind fill|drain|set [targets] [amount]
    root.then(Commands.literal("metalmind").requires(sender -> sender.hasPermission(MantleCommand.PERMISSION_GAME_COMMANDS))
      .then(subCommand(MetalType.METALMIND, Operation.FILL))
      .then(subCommand(MetalType.METALMIND, Operation.DRAIN))
      .then(subCommand(MetalType.METALMIND, Operation.SET)));
    // spike fill|set [targets] [amount]
    root.then(Commands.literal("spike").requires(sender -> sender.hasPermission(MantleCommand.PERMISSION_GAME_COMMANDS))
      .then(subCommand(MetalType.SPIKE, Operation.FILL))
      .then(subCommand(MetalType.SPIKE, Operation.SET)));
  }

  /** Adds a subcommand for the given metal type and operation */
  private static ArgumentBuilder<CommandSourceStack,?> subCommand(MetalType metal, Operation operation) {
    return Commands.literal(operation.getName())
      .executes(context -> metal.run(context, List.of(context.getSource().getPlayerOrException()), operation, -1))
      .then(Commands.argument("targets", EntityArgument.players())
        .executes(context -> metal.run(context, operation, -1))
        .then(Commands.argument("amount", IntegerArgumentType.integer(operation == Operation.SET ? 0 : 1))
          .executes(context -> metal.run(context, operation, IntegerArgumentType.getInteger(context, "amount")))));
  }

  /** Operations to perform */
  private enum Operation {
    FILL,
    DRAIN,
    SET;

    /** Gets the lang key suffix */
    public String getName() {
      return name().toLowerCase(Locale.ROOT);
    }

    /** Gets the lang key suffix for no size */
    public String getNoSize() {
      return this == DRAIN ? "empty" : "full";
    }
  }

  /** Helper for the different command variants */
  private enum MetalType {
    METALMIND {
      @Override
      public int run(CommandContext<CommandSourceStack> context, Collection<? extends Player> targets, Operation op, int amount) throws CommandSyntaxException {
        // will error immediately with a more useful error if only a single target
        boolean single = targets.size() == 1;
        int successes = 0;
        // apply effect to each entity
        for (Player target : targets) {
          // must have item
          ItemStack stack = target.getMainHandItem();
          if (!stack.isEmpty()) {
            if (stack.getItem() instanceof MetalmindItem metalmind) {
              // if no size given, fill or empty max
              int toUpdate = amount;
              if (amount == -1) {
                toUpdate = metalmind.getCapacity(stack);
              }
              // fill or drain, don't care if it worked or if we are allowed to
              MetalbornData data = MetalbornData.getData(target);
              if (op == Operation.FILL) {
                toUpdate = metalmind.fill(stack, target, toUpdate, data);
              } else if (op == Operation.DRAIN) {
                toUpdate = metalmind.drain(stack, target, toUpdate, data);
              } else {
                metalmind.setAmount(stack, target, toUpdate, data);
              }

              // update amount if only target
              if (single) {
                amount = toUpdate;
              }
              successes++;
            } else if (single) {
              throw ERROR_NO_METALMIND.create(stack.getItem().getName(stack).getString());
            }
          } else if (single) {
            throw ERROR_NO_ITEM.create(target);
          }
        }

        // if nothing changed, throw
        if (successes == 0) {
          throw NO_ACTION_METALMIND.create();
        }
        // if only one target, simpler message
        sendSuccess(context, "metalmind.", targets, successes, op, amount);
        return successes;
      }
    },
    SPIKE {
      @Override
      public int run(CommandContext<CommandSourceStack> context, Collection<? extends Player> targets, Operation op, int amount) throws CommandSyntaxException {
        // will error immediately with a more useful error if only a single target
        boolean single = targets.size() == 1;
        int successes = 0;
        // apply effect to each entity
        for (Player target : targets) {
          // must have item
          ItemStack stack = target.getMainHandItem();
          if (!stack.isEmpty()) {
            // if filling spikes, we need a spike
            if (stack.getItem() instanceof SpikeItem spike) {
              int toFill = amount;
              // if no size given, fill max
              if (toFill == -1) {
                toFill = spike.getMaxCharge(stack);
              }
              // fill, don't care if it worked
              if (op == Operation.FILL) {
                toFill = spike.fill(stack, toFill);
              } else {
                toFill = spike.setCharge(stack, toFill);
              }

              // update amount if only target
              if (single) {
                amount = toFill;
              }
              successes++;
            } else if (single) {
              throw ERROR_NO_SPIKE.create(stack.getItem().getName(stack).getString());
            }
          } else if (single) {
            throw ERROR_NO_ITEM.create(target);
          }
        }

        // if nothing changed, throw
        if (successes == 0) {
          throw NO_ACTION_SPIKE.create();
        }
        sendSuccess(context, "spike.", targets, successes, op, amount);
        return successes;
      }
    };

    /** Runs the command */
    public abstract int run(CommandContext<CommandSourceStack> context, Collection<? extends Player> targets, Operation op, int amount) throws CommandSyntaxException;

    /** Runs the command with the target argument */
    public int run(CommandContext<CommandSourceStack> context, Operation op, int amount) throws CommandSyntaxException {
      return run(context, EntityArgument.getPlayers(context, "targets"), op, amount);
    }
  }

  /** Displays the success message */
  private static void sendSuccess(CommandContext<CommandSourceStack> context, String variant, Collection<? extends Player> targets, int successes, Operation op, int amount) {
    CommandSourceStack source = context.getSource();
    if (successes == 1) {
      // 1 target gets detailed message
      source.sendSuccess(() -> Component.translatable(KEY_PREFIX + variant + KEY_SINGLE + op.getName(), targets.iterator().next().getDisplayName(), amount), true);
    } else if (amount == -1) {
      // with multiple targets, can't display -1 so just say full or empty
      source.sendSuccess(() -> Component.translatable(KEY_PREFIX + variant + KEY_MULTIPLE + op.getNoSize(), successes), true);
    } else {
      // multiple targets and amount works
      source.sendSuccess(() -> Component.translatable(KEY_PREFIX + variant + KEY_MULTIPLE + op.getName(), successes, amount), true);
    }
  }
}

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
import knightminer.metalborn.item.Spike;
import knightminer.metalborn.item.metalmind.Metalmind;
import knightminer.metalborn.item.metalmind.MetalmindItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.command.MantleCommand;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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
  private static final SimpleCommandExceptionType NO_ACTION_IDENTITY = new SimpleCommandExceptionType(Metalborn.component("command", "identity.error.no_action"));
  /** UUID for fake player Sazed, used for testing identity. */
  private static final UUID SAZED = UUID.fromString("b0b6dbd9-8cbf-4897-8267-591fc275a9c6");

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

    // identity set [identity] [targets]
    // identity unkey|sazed [targets]
    root.then(Commands.literal("identity").requires(sender -> sender.hasPermission(MantleCommand.PERMISSION_GAME_COMMANDS))
      .then(Commands.literal("set")
        // sets the identity for yourself to yourself
        .executes(context -> {
          Player player = context.getSource().getPlayerOrException();
          return setIdentity(context, List.of(player), player);
        })
        // sets identity for yourself to listed player
        .then(Commands.argument("identity", EntityArgument.player())
          .executes(context -> setIdentityToArgument(context, List.of(context.getSource().getPlayerOrException())))
          // sets identity on targets to listed player
          .then(Commands.argument("targets", EntityArgument.players())
            .executes(context -> setIdentityToArgument(context, EntityArgument.getPlayers(context, "targets"))))))
      // clears identity (makes it unkeyed)
      .then(Commands.literal("unkey")
        // on the sender
        .executes(context -> setSenderIdentity(context, null, "unkeyed"))
        // on the list of targets
        .then(Commands.argument("targets", EntityArgument.players())
          .executes(context -> setTargetIdentity(context, null, "unkeyed"))))
      // makes identity sazed
      .then(Commands.literal("sazed")
        // on the sender
        .executes(context -> setSenderIdentity(context, SAZED, "Sazed"))
        // on the list of targets
        .then(Commands.argument("targets", EntityArgument.players())
          .executes(context -> setTargetIdentity(context, SAZED, "Sazed")))));
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
            if (stack.getItem() instanceof Metalmind metalmind) {
              // if no size given, fill or empty max
              int toUpdate = amount;
              if (amount == -1) {
                toUpdate = metalmind.getCapacity(stack);
                if (op != Operation.SET) {
                  toUpdate *= stack.getCount();
                }
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
        sendSuccess(context, "metalmind.", targets, successes, op, amount == -1 ? null : amount);
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
            if (stack.getItem() instanceof Spike spike) {
              int toFill = amount;
              // if no size given, fill max
              if (toFill == -1) {
                toFill = spike.getMaxCharge(stack);
                if (op != Operation.SET) {
                  toFill *= stack.getCount();
                }
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
        sendSuccess(context, "spike.", targets, successes, op, amount == -1 ? null : amount);
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
  private static void sendSuccess(CommandContext<CommandSourceStack> context, String variant, Collection<? extends Player> targets, int successes, Operation op, @Nullable Object value) {
    CommandSourceStack source = context.getSource();
    if (successes == 1) {
      // 1 target gets detailed message
      source.sendSuccess(() -> Component.translatable(KEY_PREFIX + variant + KEY_SINGLE + op.getName(), targets.iterator().next().getDisplayName(), value), true);
    } else if (value == null) {
      // with multiple targets, can't display -1 so just say full or empty
      source.sendSuccess(() -> Component.translatable(KEY_PREFIX + variant + KEY_MULTIPLE + op.getNoSize(), successes), true);
    } else {
      // multiple targets and amount works
      source.sendSuccess(() -> Component.translatable(KEY_PREFIX + variant + KEY_MULTIPLE + op.getName(), successes, value), true);
    }
  }


  /* Identity */

  /** Sets the identity on the list of targets to the argument player */
  private static int setIdentityToArgument(CommandContext<CommandSourceStack> context, Collection<? extends Player> targets) throws CommandSyntaxException {
    return setIdentity(context, targets, EntityArgument.getPlayer(context, "identity"));
  }

  /** Sets the identity on the list of targets to the given player */
  private static int setIdentity(CommandContext<CommandSourceStack> context, Collection<? extends Player> targets, Player player) throws CommandSyntaxException {
    return setIdentity(context, targets, player.getUUID(), player.getGameProfile().getName());
  }

  /** Sets the identity on the list of targets in the argument to the given player */
  private static int setSenderIdentity(CommandContext<CommandSourceStack> context, @Nullable UUID uuid, String name) throws CommandSyntaxException {
    return setIdentity(context, List.of(context.getSource().getPlayerOrException()), uuid, name);
  }

  /** Sets the identity on the list of targets in the argument to the given player */
  private static int setTargetIdentity(CommandContext<CommandSourceStack> context, @Nullable UUID uuid, String name) throws CommandSyntaxException {
    return setIdentity(context, EntityArgument.getPlayers(context, "targets"), uuid, name);
  }

  /** Sets the identity on the list of targets */
  private static int setIdentity(CommandContext<CommandSourceStack> context, Collection<? extends Player> targets, @Nullable UUID uuid, String name) throws CommandSyntaxException {
    // will error immediately with a more useful error if only a single target
    boolean single = targets.size() == 1;
    int successes = 0;
    // apply effect to each entity
    for (Player target : targets) {
      // must have item
      ItemStack stack = target.getMainHandItem();
      if (!stack.isEmpty()) {
        if (stack.getItem() instanceof Metalmind) {
          // null means remove owner
          if (uuid == null) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
              tag.remove(MetalmindItem.TAG_OWNER);
              tag.remove(MetalmindItem.TAG_OWNER_NAME);
              if (tag.isEmpty()) {
                stack.setTag(null);
              }
            }
          } else {
            // nonnull means set owner
            CompoundTag tag = stack.getOrCreateTag();
            tag.putUUID(MetalmindItem.TAG_OWNER, uuid);
            tag.putString(MetalmindItem.TAG_OWNER_NAME, name);
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
      throw NO_ACTION_IDENTITY.create();
    }
    sendSuccess(context, "identity.", targets, successes, Operation.SET, name);
    return successes;
  }
}

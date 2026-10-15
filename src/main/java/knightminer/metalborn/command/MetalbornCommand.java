package knightminer.metalborn.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import knightminer.metalborn.Metalborn;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import slimeknights.mantle.registration.deferred.ArgumentTypeDeferredRegister;

import java.util.function.Consumer;

/** Base class for registering all commands under Metalborn */
public class MetalbornCommand {
  private static final ArgumentTypeDeferredRegister ARGUMENT_TYPE = new ArgumentTypeDeferredRegister(Metalborn.MOD_ID);

  /** Registers all Metalborn command related content */
  public static void init(IEventBus bus) {
    ARGUMENT_TYPE.register(bus);
    // argument types
    ARGUMENT_TYPE.registerSingleton("metal_power", MetalArgument.class, MetalArgument::metal);

    NeoForge.EVENT_BUS.addListener(MetalbornCommand::registerCommand);
  }

  /** Registers a sub command for the root Mantle command */
  @SuppressWarnings("SameParameterValue")
  private static void register(LiteralArgumentBuilder<CommandSourceStack> root, String name, Consumer<LiteralArgumentBuilder<CommandSourceStack>> consumer) {
    LiteralArgumentBuilder<CommandSourceStack> subCommand = Commands.literal(name);
    consumer.accept(subCommand);
    root.then(subCommand);
  }

  /** Event listener to register the Mantle command */
  private static void registerCommand(RegisterCommandsEvent event) {
    LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(Metalborn.MOD_ID);

    // sub commands
    register(builder, "ferring", FerringCommand::register);
    register(builder, "metal", MetalCommand::register);
    MetalmindCommand.register(builder); // registeres "metalmind" and "spike" internally

    // register final command
    event.getDispatcher().register(builder);
  }
}

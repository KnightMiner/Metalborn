package knightminer.metalborn.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.util.ArgumentTypeDeferredRegister;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Consumer;

/** Base class for registering all commands under Metalborn */
public class MetalbornCommand {
  private static final ArgumentTypeDeferredRegister ARGUMENT_TYPE = new ArgumentTypeDeferredRegister(Metalborn.MOD_ID);

  /** Registers all Metalborn command related content */
  public static void init() {
    ARGUMENT_TYPE.register(FMLJavaModLoadingContext.get().getModEventBus());
    // argument types
    ARGUMENT_TYPE.registerSingleton("metal_power", MetalArgument.class, MetalArgument::metal);

    MinecraftForge.EVENT_BUS.addListener(MetalbornCommand::registerCommand);
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
    MetalmindCommand.register(builder); // registeres "metalmind" and "spike" internally

    // register final command
    event.getDispatcher().register(builder);
  }
}

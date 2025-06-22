package knightminer.metalborn.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.commands.CommandSourceStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/** Argument type for a metal power */
public class MetalArgument implements ArgumentType<MetalPower> {
  /** List of example IDs for whatever reason */
  static final Collection<String> EXAMPLES = Arrays.asList("metalborn:iron", "metalborn:copper");
  /** Exception thrown when a metal is not found */
  private static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(name -> Metalborn.component("command", "metal.not_found", name));

  /** Gets the tool stat from the context */
  public static MetalPower getPower(CommandContext<CommandSourceStack> context, String name) {
    return context.getArgument(name, MetalPower.class);
  }

  /** Static constructor to make builder more readable */
  public static MetalArgument metal() {
    return new MetalArgument();
  }

  @Override
  public MetalPower parse(StringReader reader) throws CommandSyntaxException {
    MetalId name = new MetalId(MetalbornSuggestionProvider.read(reader));
    MetalPower power = MetalManager.INSTANCE.get(name);
    if (power == MetalPower.DEFAULT) {
      throw NOT_FOUND.createWithContext(reader, name);
    }
    return power;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
    return MetalbornSuggestionProvider.suggestResource(MetalManager.INSTANCE.getSortedPowers().stream().map(MetalPower::id), builder, id -> id, MetalId::getName);
  }

  @Override
  public Collection<String> getExamples() {
    return EXAMPLES;
  }
}

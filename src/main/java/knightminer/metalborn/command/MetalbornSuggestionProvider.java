package knightminer.metalborn.command;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import knightminer.metalborn.Metalborn;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static net.minecraft.commands.SharedSuggestionProvider.matchesSubStr;

/** Suggestion helpers for Metalborn commands */
public interface MetalbornSuggestionProvider {
  /** Splits the given string into a namespace and path, using the given default mod ID */
  private static String[] decompose(String location) {
    String[] parts = {Metalborn.MOD_ID, location };
    int loc = location.indexOf(':');
    if (loc >= 0) {
      parts[1] = location.substring(loc + 1);
      if (loc >= 1) {
        parts[0] = location.substring(0, loc);
      }
    }

    return parts;
  }

  /**
   * Attempts to read a resource ID from the given string reader. Reimplementation of {@link ResourceLocation#read(String)} but in our ID.
   *
   * @param reader Reader to read from
   * @return Resource location, or exception if invalid
   * @throws CommandSyntaxException If parsing fails
   */
  static ResourceLocation read(StringReader reader) throws CommandSyntaxException {
    int start = reader.getCursor();
    while(reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
      reader.skip();
    }
    String string = reader.getString().substring(start, reader.getCursor());
    String[] parts = decompose(string);
    try {
      return new ResourceLocation(parts[0], parts[1]);
    } catch (ResourceLocationException ex) {
      reader.setCursor(start);
      throw ResourceLocation.ERROR_INVALID.createWithContext(reader);
    }
  }

  /**
   * Reimplementation of {@link net.minecraft.commands.SharedSuggestionProvider#filterResources(Iterable, String, Function, Consumer)} with an argument to change the default namespace.
   * @param resources       List of resources to filter
   * @param input           Current input string
   * @param idGetter        Function mapping the resource to its ID
   * @param resultConsumer  Consumer handling results
   * @param <T>  Resource type
   */
  static <T> void filterResources(Iterable<T> resources, String input, Function<T, ResourceLocation> idGetter, Consumer<T> resultConsumer) {
    boolean hasNamespace = input.indexOf(':') > -1;
    for(T resource : resources) {
      ResourceLocation location = idGetter.apply(resource);
      // if we have a namespace, do a complete match
      if (hasNamespace) {
        String locationStr = location.toString();
        if (matchesSubStr(input, locationStr)) {
          resultConsumer.accept(resource);
        }
        // if no namespace, match either on a starting namespace or using the default namespace
      } else if (matchesSubStr(input, location.getNamespace()) || Metalborn.MOD_ID.equals(location.getNamespace()) && matchesSubStr(input, location.getPath())) {
        resultConsumer.accept(resource);
      }
    }
  }

  /**
   * Reimplementation of {@link net.minecraft.commands.SharedSuggestionProvider#suggestResource(Iterable, SuggestionsBuilder, Function, Function)} with an argument to change the default namespace.
   * @param resources       List of resources to filter
   * @param builder         Builder for suggestion options
   * @param idGetter        Function mapping the resource to its ID
   * @param tooltip         Function mapping elements to their tooltip
   */
  static <T> CompletableFuture<Suggestions> suggestResource(Iterable<T> resources, SuggestionsBuilder builder, Function<T, ResourceLocation> idGetter, Function<T,Message> tooltip) {
    String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
    filterResources(resources, remaining, idGetter, (resource) -> builder.suggest(idGetter.apply(resource).toString(), tooltip.apply(resource)));
    return builder.buildFuture();
  }

  /**
   * Reimplementation of {@link net.minecraft.commands.SharedSuggestionProvider#suggestResource(Stream, SuggestionsBuilder, Function, Function)} with an argument to change the default namespace.
   * @param resources       List of resources to filter
   * @param builder         Builder for suggestion options
   * @param idGetter        Function mapping the resource to its ID
   * @param tooltip         Function mapping elements to their tooltip
   */
  static <T> CompletableFuture<Suggestions> suggestResource(Stream<T> resources, SuggestionsBuilder builder, Function<T, ResourceLocation> idGetter, Function<T, Message> tooltip) {
    return suggestResource(resources::iterator, builder, idGetter, tooltip);
  }
}

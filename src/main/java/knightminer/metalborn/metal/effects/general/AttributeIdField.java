package knightminer.metalborn.metal.effects.general;

import com.google.gson.JsonObject;
import knightminer.metalborn.metal.MetalId;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.Function;

/** Field for the attribute ID. If the key is unset in JSON, defaults to the metal ID. */
record AttributeIdField<P>(String key, Function<P,ResourceLocation> getter) implements LoadableField<ResourceLocation,P> {
  @Override
  public ResourceLocation get(JsonObject json, String key, TypedMap context) {
    if (json.has(key)) {
      return JsonHelper.getResourceLocation(json, key);
    }
    return context.getOrThrow(ContextKey.ID, ErrorFactory.JSON_SYNTAX_ERROR);
  }

  @Override
  public void serializeInto(P parent, JsonObject json, TypedMap context) {
    ResourceLocation id = getter.apply(parent);
    if (!id.equals(MetalId.NONE)) {
      json.addProperty(key, id.toString());
    }
  }

  @Override
  public ResourceLocation decode(RegistryFriendlyByteBuf buffer, TypedMap typedMap) {
    return buffer.readResourceLocation();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, P parent, TypedMap typedMap) {
    buffer.writeResourceLocation(getter.apply(parent));
  }
}

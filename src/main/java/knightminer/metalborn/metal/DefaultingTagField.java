package knightminer.metalborn.metal;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.field.RecordField;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.Function;

/** Tag field that defaults to the name key */
public record DefaultingTagField<T>(String key, ResourceKey<? extends Registry<T>> registry, String prefix, Function<MetalPower,TagKey<T>> getter) implements RecordField<TagKey<T>,MetalPower> {
  @Override
  public TagKey<T> get(JsonObject json, TypedMap context) {
    // if we have the key, use that to fetch the value
    if (json.has(key)) {
      return TagKey.create(registry, JsonHelper.getResourceLocation(json, key));
    }
    // if we have a name, make a common tag with the given prefix
    if (json.has("name")) {
      return TagKey.create(registry, Mantle.commonResource(prefix + GsonHelper.getAsString(json, "name")));
    }
    // both failed? throw exception
    throw new JsonSyntaxException("Missing both '" + key + " and 'name', expected to find a string at one of the two");
  }

  @Override
  public void serialize(MetalPower power, JsonObject json) {
    // our question on serializing is whether the tag matches the default. If it does, skip it
    ResourceLocation location = getter.apply(power).location();
    if (Mantle.COMMON.equals(location.getNamespace())) {
      // if we have a name, and the name matches our prefix format, skip
      if (json.has("name") && location.getPath().equals(prefix + json.get("name").getAsString())) {
        return;
      }
    }
    json.addProperty(key, location.toString());
  }

  @Override
  public TagKey<T> decode(FriendlyByteBuf buffer, TypedMap context) {
    return TagKey.create(registry, buffer.readResourceLocation());
  }

  @Override
  public void encode(FriendlyByteBuf buffer, MetalPower power) {
    buffer.writeResourceLocation(getter.apply(power).location());
  }
}

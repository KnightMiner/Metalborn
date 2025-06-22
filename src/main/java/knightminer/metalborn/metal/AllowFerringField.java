package knightminer.metalborn.metal;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.field.RecordField;
import slimeknights.mantle.util.typed.TypedMap;

/** Field for the ferring boolean, which is ignored if no feruchemical powers */
record AllowFerringField(String key, String requireIf) implements RecordField<Boolean,MetalPower> {
  @Override
  public Boolean get(JsonObject json, TypedMap context) {
    if (json.has(requireIf)) {
      return GsonHelper.getAsBoolean(json, key);
    }
    return false;
  }

  @Override
  public void serialize(MetalPower parent, JsonObject json) {
    if (!parent.feruchemy().isEmpty()) {
      json.addProperty(key, parent.ferring());
    }
  }

  @Override
  public Boolean decode(FriendlyByteBuf buffer, TypedMap context) {
    return buffer.readBoolean();
  }

  @Override
  public void encode(FriendlyByteBuf buffer, MetalPower parent) {
    buffer.writeBoolean(parent.ferring());
  }
}

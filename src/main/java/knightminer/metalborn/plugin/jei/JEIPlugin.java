package knightminer.metalborn.plugin.jei;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.item.SpikeItem;
import knightminer.metalborn.metal.MetalId;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Plugin adding any relevant things to JEI */
@JeiPlugin
public class JEIPlugin implements IModPlugin {
  private static final ResourceLocation ID = Metalborn.resource("jei_plugin");

  @Override
  public ResourceLocation getPluginUid() {
    return ID;
  }

  @Override
  public void registerItemSubtypes(ISubtypeRegistration registration) {
    IIngredientSubtypeInterpreter<ItemStack> metal = (stack, context) -> {
      MetalId id = MetalItem.getMetal(stack);
      return id == MetalId.NONE ? IIngredientSubtypeInterpreter.NONE : id.toString();
    };

    registration.registerSubtypeInterpreter(Registration.LERASIUM_ALLOY_NUGGET.asItem(), metal);
    registration.registerSubtypeInterpreter(Registration.BRACER.asItem(), metal);
    registration.registerSubtypeInterpreter(Registration.RING.asItem(), metal);
    registration.registerSubtypeInterpreter(Registration.SPIKE.asItem(), (stack, context) -> {
      MetalId id = MetalItem.getMetal(stack);
      if (id == MetalId.NONE) {
        return IIngredientSubtypeInterpreter.NONE;
      }
      String type = id.toString();
      if (((SpikeItem)stack.getItem()).isFull(stack)) {
        type += ",full";
      }
      return type;
    });
  }
}

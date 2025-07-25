package knightminer.metalborn.json.recipe;

import knightminer.metalborn.metal.MetalPower;
import knightminer.metalborn.metal.MetalShape;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.TagPreference;

import java.util.Optional;

/** Result for a recipe with a metal input that wishes to output ingots or nuggets. */
public record MetalResult(MetalShape shape, int amount) {
  /** Loadable instance for using this object as a recipe result */
  public static final RecordLoadable<MetalResult> LOADABLE = RecordLoadable.create(
    MetalShape.LOADABLE.requiredField("shape", MetalResult::shape),
    IntLoadable.FROM_ONE.defaultField("amount", 1, false, MetalResult::amount),
    MetalResult::new
  ).compact(MetalShape.LOADABLE.flatXmap(shape -> new MetalResult(shape, 1), MetalResult::shape), r -> r.amount == 1);


  /** Checks if this result is present */
  public boolean isPresent(MetalPower power) {
    return TagPreference.getPreference(power.tag(shape)).isPresent();
  }

  /** Gets the result for the given power, or empty if missing */
  public ItemStack get(MetalPower power) {
    Optional<Item> item = TagPreference.getPreference(power.tag(shape));
    if (item.isPresent()) {
      return new ItemStack(item.get(), amount);
    }
    return ItemStack.EMPTY;
  }

  /** Gets the display result to save under the recipe without a container */
  public ItemStack getDisplay() {
    return new ItemStack(switch (shape) {
      case NUGGET -> Items.IRON_NUGGET;
      case INGOT -> Items.IRON_INGOT;
    });
  }
}

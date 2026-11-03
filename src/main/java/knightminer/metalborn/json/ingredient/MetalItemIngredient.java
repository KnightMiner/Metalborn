package knightminer.metalborn.json.ingredient;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.json.predicate.MetalPredicate;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;

import java.util.stream.Stream;

/**
 * Ingredient that displays all metal variants for an item.
 * @see MetalShapeIngredient
 */
public record MetalItemIngredient(Item item, IJsonPredicate<MetalPower> filter) implements ICustomIngredient, MetalPredicate.IngredientWithMetal {
  public static final RecordLoadable<MetalItemIngredient> LOADER = RecordLoadable.create(
    Loadables.ITEM.requiredField("item", MetalItemIngredient::item),
    FILTER_FIELD, MetalItemIngredient::new);

  public static final ResourceLocation ID = Metalborn.resource("metal_item");

  /** Creates a new instance */
  public static Ingredient of(ItemLike item, IJsonPredicate<MetalPower> filter) {
    return new MetalItemIngredient(item.asItem(), filter).toVanilla();
  }

  @Override
  public IngredientType<MetalItemIngredient> getType() {
    return Registration.METAL_ITEM_INGREDIENT.get();
  }

  @Override
  public boolean test(ItemStack stack) {
    return stack.is(item);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public Stream<ItemStack> getItems() {
    return MetalManager.INSTANCE.getSortedPowers().stream()
      .filter(filter::matches)
      .map(power -> MetalItem.setMetal(new ItemStack(item), power.id()));
  }
}

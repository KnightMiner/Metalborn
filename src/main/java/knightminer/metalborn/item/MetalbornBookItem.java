package knightminer.metalborn.item;

import knightminer.metalborn.client.book.MetalbornBook;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.client.book.BookScreenOpener;
import slimeknights.mantle.item.AbstractBookItem;

/** Book item for the mod */
@Internal
public class MetalbornBookItem extends AbstractBookItem {
  public MetalbornBookItem(Properties properties) {
    super(properties);
  }

  @Override
  public BookScreenOpener getBook(ItemStack stack) {
    return MetalbornBook.getBook();
  }
}

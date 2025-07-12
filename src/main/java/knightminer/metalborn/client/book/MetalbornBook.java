package knightminer.metalborn.client.book;

import knightminer.metalborn.Metalborn;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.BookScreenOpener;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.repository.FileRepository;
import slimeknights.mantle.client.book.transformer.BookTransformer;

/** Class handling initialization of the book in Metalborn */
public class MetalbornBook {
  /** Book instance */
  private static final BookData BOOK = BookLoader.registerBook(Metalborn.resource("metalic_arts"), true, false, new FileRepository(Metalborn.resource("book")));

  /** Gets the book for usage in the item class */
  public static BookScreenOpener getBook() {
    return BOOK;
  }

  /** Adds all book related sections */
  public static void init() {
    // pages
    BookLoader.registerPageType(MetalContent.ID, MetalContent.class);

    // padding needs to be last to ensure page counts are right
    BOOK.addTransformer(MetalInjectingTransformer.INSTANCE);
    BOOK.addTransformer(BookTransformer.paddingTransformer());
  }
}

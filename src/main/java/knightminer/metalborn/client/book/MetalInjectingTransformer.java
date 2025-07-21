package knightminer.metalborn.client.book;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.data.content.ContentPageIconList;
import slimeknights.mantle.client.book.data.content.ContentPageIconList.PageWithIcon;
import slimeknights.mantle.client.book.transformer.BookTransformer;
import slimeknights.mantle.client.screen.book.element.ItemElement;
import slimeknights.mantle.client.screen.book.element.SizedBookElement;

import java.util.ArrayList;
import java.util.List;

/** Transformer that injects all metal power pages into the book */
public class MetalInjectingTransformer extends BookTransformer {
  public static final MetalInjectingTransformer INSTANCE = new MetalInjectingTransformer();
  private static final ResourceLocation KEY = Metalborn.resource("metals");

  private MetalInjectingTransformer() {}

  @Override
  public void transform(BookData book) {
    for (SectionData section : book.sections) {
      JsonElement element = section.extraData.get(KEY);
      if (element != null) {
        try {
          addPages(section, element);
        } catch (JsonParseException e) {
          Metalborn.LOG.error("Failed to parse metals section: {}", section.name, e);
        }
      }
    }
  }

  /** Adds the metal pages to the section */
  private void addPages(SectionData section, JsonElement element) {
    JsonObject json = GsonHelper.convertToJsonObject(element, KEY.toString());
    String path = GsonHelper.getAsString(json, "path");

    List<MetalPower> powers = MetalManager.INSTANCE.getSortedPowers();
    if (powers.isEmpty()) {
      return;
    }

    // create pages for each power
    List<PageWithIcon> newPages = new ArrayList<>();
    for (MetalPower power : powers) {
      // start building the page
      ResourceLocation name = power.id();
      PageData newPage = new PageData(true);
      newPage.parent = section;
      newPage.source = section.source;
      newPage.type = MetalContent.ID;
      newPage.name = name.getNamespace() + "." + name.getPath();
      String data = path + "/" + name.getNamespace() + "_" + name.getPath() + ".json";

      // if the path exists load the page, otherwise use a fallback option
      if (section.source.resourceExists(section.source.getResourceLocation(data))) {
        newPage.data = data;
      } else {
        newPage.content = new MetalContent();
      }
      newPage.load();

      // sets the power on the page
      if (newPage.content instanceof MetalContent content) {
        content.setMetal(power);
      }

      // set fluid effect properties into the page
      List<ItemStack> displayStacks = MetalContent.getItems(power.ingot());
      if (displayStacks.isEmpty()) {
        displayStacks = List.of(Registration.CHANGE_FERRING.get().withMetal(power.id()));
      }

      // build the icon
      SizedBookElement icon = new ItemElement(0, 0, 1f, displayStacks);
      newPages.add(new PageWithIcon(icon, newPage));
    }

    // add the pages and the indexes
    List<ContentPageIconList> listPages = ContentPageIconList.getPagesNeededForItemCount(
      newPages.size(), section,
      section.parent.translate(section.name),
      section.parent.strings.get(String.format("%s.subtext", section.name)));

    // add all pages
    ContentPageIconList.addPages(section, listPages, newPages);
  }
}

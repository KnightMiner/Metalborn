package knightminer.metalborn.client.book;

import com.google.gson.annotations.SerializedName;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.item.metalmind.Metalmind;
import knightminer.metalborn.item.spike.SpikeItem;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableObject;
import slimeknights.mantle.client.book.HTMLUtils;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.content.PageContent;
import slimeknights.mantle.client.book.data.element.IngredientData;
import slimeknights.mantle.client.book.data.element.TextComponentData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.ItemElement;
import slimeknights.mantle.client.screen.book.element.TextComponentElement;
import slimeknights.mantle.client.screen.book.element.TextElement;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.mantle.util.html.HtmlElement;
import slimeknights.mantle.util.html.HtmlGroup;
import slimeknights.mantle.util.html.HtmlSerializable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static knightminer.metalborn.item.metalmind.MetalmindItem.KEY_STORES;
import static knightminer.metalborn.item.spike.SpikeItem.KEY_TARGET;

/** Book page for metal */
public class MetalContent extends PageContent {
  public static final ResourceLocation ID = Metalborn.resource("metal");
  private static final Component TAPPING_EFFECTS = Metalborn.component("book", "metal.tapping").withStyle(ChatFormatting.UNDERLINE);
  private static final Component STORING_EFFECTS = Metalborn.component("book", "metal.storing").withStyle(ChatFormatting.UNDERLINE);
  private static final String KEY_FERRING = Metalborn.key("book", "metal.ferring");

  /** Metal displayed in JSON */
  @SerializedName("metal")
  protected String metalId = "";
  /** ID of the metal to display */
  @Nullable
  private transient MetalId metal;
  /** Power data to display */
  @Nullable
  private transient MetalPower power;

  /** List of metalmind items to display */
  @Nullable
  public IngredientData metalminds;
  /** List of spike items to display */
  @Nullable
  public IngredientData spikes;

  /** List of effects on tapping */
  @Nullable
  protected String[] tapping = null;
  /** List of effects on storing */
  @Nullable
  protected String[] storing = null;

  /** If true, appends the tapping texts instead of replacing */
  protected boolean append_tapping = false;
  /** If true, appends the storing texts instead of replacing */
  protected boolean append_storing = false;
  /** If true, appends both tapping and storing instead of replacing */
  protected boolean append = false;


  @Override
  public String getTitle() {
    return getMetal().getName().getString();
  }

  /** Gets the metal ID */
  private MetalId getMetal() {
    if (metal == null) {
      metal = Objects.requireNonNullElse(MetalId.tryParse(metalId), MetalId.NONE);
    }
    return metal;
  }

  /** Gets the power for this metal */
  private MetalPower getPower() {
    if (power == null) {
      power = MetalManager.INSTANCE.get(getMetal());
    }
    return power;
  }

  /** Sets the power from the injector */
  public void setMetal(MetalPower power) {
    this.metal = power.id();
    this.metalId = metal.toString();
    this.power = power;
  }

  /** Gets or creates the list of metalminds */
  private IngredientData getMetalminds() {
    if (metalminds == null) {
      MetalPower power = getPower();
      metalminds = IngredientData.getItemStackData(RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, Registration.METALMINDS).flatMap(item -> {
        if (item instanceof MetalItem metalItem && metalItem.canUseMetal(power)) {
          ItemStack stack = metalItem.withMetal(power.holder());
          if (item instanceof Metalmind metalmind) {
            metalmind.setAmount(stack, null, metalmind.getCapacity(stack) / 2, null);
          }
          return Stream.of(stack);
        }
        return Stream.empty();
      }).collect(Collectors.<ItemStack, NonNullList<ItemStack>>toCollection(NonNullList::create)));
    }
    return metalminds;
  }

  /** Gets or creates the list of metalminds */
  private IngredientData getSpikes() {
    if (spikes == null) {
      SpikeItem spike = Registration.SPIKE.get();
      MetalPower power = getPower();
      if (spike.canUseMetal(power)) {
        ItemStack stack = spike.withMetal(power.holder());
        spike.setCharge(stack, spike.getMaxCharge(stack) / 2);
        spikes = IngredientData.getItemStackData(stack);
      } else {
        spikes = IngredientData.getItemStackData(NonNullList.createWithCapacity(0));
      }
    }
    return spikes;
  }

  /** Gets a list of items in the tag */
  public static List<ItemStack> getItems(TagKey<Item> tag) {
    return RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, tag).map(ItemStack::new).toList();
  }

  /** Adds a list of effects to the page */
  private void addList(List<BookElement> list, int x, int y, int width, int height, Component title, @Nullable String[] texts, boolean append, int fallbackLevel) {
    list.add(new TextComponentElement(x, y, width, height, title));

    // we use text components when autogenerating data from the effects list
    if (append || this.append || texts == null) {
      Player player = Minecraft.getInstance().player;
      List<TextComponentData> effectData = new ArrayList<>();
      if (player != null) {
        List<Component> components = new ArrayList<>();
        getPower().getTooltip(player, fallbackLevel, components);
        for (Component text : components) {
          effectData.add(new TextComponentData(Component.literal(TextData.LIST_PREFIX).append(text.copy().setStyle(Style.EMPTY))).linebreak(true));
        }
      }
      // append texts array
      if (texts != null) {
        for (String text : texts) {
          effectData.add(new TextComponentData(TextData.LIST_PREFIX + text).linebreak(true));
        }
      }
      // add if we have any effects
      if (!effectData.isEmpty()) {
        list.add(new TextComponentElement(x, y + 14, width, height, effectData));
      }
    } else if (texts.length > 0) {
      List<TextData> effectData = new ArrayList<>();
      for (String text : texts) {
        effectData.add(new TextData(TextData.LIST_PREFIX + text).linebreak(true));
      }
      list.add(new TextElement(x, y + 14, width, height, effectData));
    }
  }

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    this.addTitle(list, getTitle());

    // add metal items around header
    MetalPower power = getPower();
    List<ItemStack> items = getItems(power.ingot());
    if (!items.isEmpty()) {
      list.add(new ItemElement(-2, 0, 1f, items));
    }
    items = getItems(power.nugget());
    if (!items.isEmpty()) {
      list.add(new ItemElement(BookScreen.PAGE_WIDTH + 2 - ItemElement.ITEM_SIZE_HARDCODED, 0, 1f, items));
    }

    int y = getTitleHeight();
    MetalId metal = getMetal();

    // metalminds
    int itemSize = ItemElement.ITEM_SIZE_HARDCODED * 2;
    List<ItemStack> metalminds = getMetalminds().getItems();
    if (!metalminds.isEmpty()) {
      ItemElement item = new ItemElement(-2, y, 2f, metalminds);
      list.add(item);
      // ferring
      TextComponentElement text = new TextComponentElement(item.width, y + 5, BookScreen.PAGE_WIDTH - item.width, 10, Component.translatable(KEY_FERRING, metal.getFerring().withStyle(ChatFormatting.DARK_AQUA)));
      list.add(text);
      // stores text
      list.add(new TextComponentElement(item.width, y + 5 + text.height, BookScreen.PAGE_WIDTH - item.width, 10, Component.translatable(KEY_STORES, metal.getStores().withStyle(ChatFormatting.DARK_GREEN))));
    }
    y += itemSize;

    // spikes
    List<ItemStack> spikes = getSpikes().getItems();
    if (!spikes.isEmpty()) {
      ItemElement item = new ItemElement(-2, y, 2f, spikes);
      list.add(item);
      // target
      list.add(new TextComponentElement(item.width, y + 11, BookScreen.PAGE_WIDTH - item.width, 10, Component.translatable(KEY_TARGET, metal.getTarget().withStyle(ChatFormatting.DARK_RED))));
    }
    y += itemSize;

    // add effects
    int width = BookScreen.PAGE_WIDTH / 2 - 2;
    int height = BookScreen.PAGE_HEIGHT - y;
    addList(list,  0, y, width, height, STORING_EFFECTS, storing, append_storing, -1);
    addList(list, width + 4, y, width, height, TAPPING_EFFECTS, tapping, append_tapping, 1);
  }


  /* HTML */

  /** Creates an element with the given style. */
  private static HtmlGroup applyStyle(Style style) {
    HtmlGroup group;
    if (!style.isEmpty()) {
      // span if we have styles
      HtmlElement element = HtmlElement.span();
      group = element;
      TextColor color = style.getColor();
      if (color != null && (color.getValue() & 0xFFFFFF) != 0) {
        element.color(color.getValue());
      }

      if (style.isBold()) element.style("font-weight", "bold");
      if (style.isItalic()) element.style("font-style", "italic");
      if (style.isStrikethrough()) element.style("text-decoration", "line-through");

      if (style.isUnderlined()) element.classes("underline");
    } else {
      group = HtmlGroup.inline();
    }
    return group;
  }

  /** Copy of {@link HTMLUtils#toHtml(Component)} to fix a bug with styles in arguments. TODO: replace with Mantle version after its merged back in. */
  private static HtmlSerializable toHtml(Component component) {
    HtmlGroup group = HtmlGroup.inline();
    // keep track of the last used style so we only add a new span if something changes - works around a lot of arguments sharing styles
    MutableObject<Style> lastStyle = new MutableObject<>(Style.EMPTY);
    MutableObject<HtmlGroup> lastElement = new MutableObject<>(group);

    // add contents
    component.visit((style, string) -> {
      HtmlGroup nested = lastElement.getValue();
      if (!style.equals(lastStyle.getValue())) {
        nested = applyStyle(style);
        group.add(nested);
        lastStyle.setValue(style);
        lastElement.setValue(nested);
      }
      if (string.indexOf('\n') != -1) {
        nested.add(HtmlGroup.indent().add(Arrays.stream(string.split("\n")).map(str -> HtmlElement.span().add(str))));
      } else {
        nested.add(string);
      }
      return Optional.empty();
    }, Style.EMPTY);

    return group;
  }


  /** Adds a list of effects to the page */
  private HtmlElement htmlList(Component title, @Nullable String[] texts, boolean append, int fallbackLevel) {
    HtmlElement element = HtmlElement.div().classes("column");
    // add title
    element.add(HtmlElement.p().classes("effect-title").add(toHtml(title)));

    // build list
    HtmlElement list = HtmlElement.ul().classes("effects");
    element.add(list);

    // we use text components when autogenerating data from the effects list
    if (append || this.append || texts == null) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
        List<Component> components = new ArrayList<>();
        getPower().getTooltip(player, fallbackLevel, components);
        for (Component text : components) {
          list.add(HtmlElement.li().add(HtmlElement.p().add(toHtml(text.copy().setStyle(Style.EMPTY)))));
        }
      }
    }
    // add texts array
    if (texts != null) {
      for (String text : texts) {
        list.add(HtmlElement.li().add(HtmlElement.p().add(text)));
      }
    }
    return element;
  }

  /** Makes a text line for the metal description */
  private static HtmlSerializable metalText(String key, MutableComponent text, ChatFormatting style) {
    return HtmlElement.p().add(toHtml(Component.translatable(key, text.withStyle(style))));
  }

  @Nullable
  @Override
  public HtmlSerializable toHTML(BookData book) {
    MetalId metal = getMetal();
    HtmlElement page = HtmlElement.div().classes("page-metalborn").add(makeTitleHTML().classes("title"));

    // add metal ingot and nugget tooltips
    MetalPower power = getPower();
    List<ItemStack> items = getItems(power.ingot());
    if (!items.isEmpty()) {
      page.add(HtmlElement.div().classes("metal-ingot").minetip(HTMLUtils.itemTooltip(items.getFirst())));
    }
    items = getItems(power.nugget());
    if (!items.isEmpty()) {
      page.add(HtmlElement.div().classes("metal-nugget").minetip(HTMLUtils.itemTooltip(items.getFirst())));
    }

    // add metalmind
    List<ItemStack> metalminds = getMetalminds().getItems();
    HtmlElement metalmindElement = HtmlElement.div().classes("metalmind");
    page.add(metalmindElement);
    if (!metalminds.isEmpty()) {
      metalmindElement
        .add(HtmlElement.div().classes("item").minetip(HTMLUtils.itemTooltip(metalminds.getFirst())))
        .add(HtmlElement.div().classes("item-text")
          .add(metalText(KEY_FERRING, metal.getFerring(), ChatFormatting.DARK_AQUA))
          .add(metalText(KEY_STORES, metal.getStores(), ChatFormatting.DARK_GREEN))
        );
    }

    // add spikes
    List<ItemStack> spikes = getSpikes().getItems();
    HtmlElement spikeElement = HtmlElement.div().classes("spike");
    page.add(spikeElement);
    if (!spikes.isEmpty()) {
      spikeElement
        .add(HtmlElement.div().classes("item").minetip(HTMLUtils.itemTooltip(spikes.getFirst())))
        .add(HtmlElement.div().classes("item-text").add(metalText(KEY_TARGET, metal.getTarget(), ChatFormatting.DARK_RED)));
    }
    // add effects
    page.add(HtmlElement.div().classes("row").add(
      htmlList(STORING_EFFECTS, storing, append_storing, -1),
      htmlList(TAPPING_EFFECTS, tapping, append_tapping, 1)
    ));

    return page;
  }
}

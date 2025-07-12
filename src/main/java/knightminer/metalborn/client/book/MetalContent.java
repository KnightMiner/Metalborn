package knightminer.metalborn.client.book;

import com.google.gson.annotations.SerializedName;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.item.MetalItem;
import knightminer.metalborn.item.SpikeItem;
import knightminer.metalborn.item.metalmind.Metalmind;
import knightminer.metalborn.metal.MetalId;
import knightminer.metalborn.metal.MetalManager;
import knightminer.metalborn.metal.MetalPower;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static knightminer.metalborn.item.SpikeItem.KEY_TARGET;
import static knightminer.metalborn.item.metalmind.MetalmindItem.KEY_STORES;

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
          ItemStack stack = metalItem.withMetal(power.id());
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
        ItemStack stack = spike.withMetal(power.id());
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
  private void addList(List<BookElement> list, int x, int y, int width, int height, Component title, @Nullable String[] texts, int fallbackLevel) {
    list.add(new TextComponentElement(x, y, width, height, title));

    // we use text components when autogenerating data from the effects list
    if (texts == null) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
        List<Component> components = new ArrayList<>();
        getPower().getTooltip(player, fallbackLevel, components);
        List<TextComponentData> effectData = new ArrayList<>();
        for (Component text : components) {
          effectData.add(new TextComponentData("● "));
          effectData.add(new TextComponentData(text.copy().setStyle(Style.EMPTY)));
          effectData.add(new TextComponentData("\n"));
        }
        list.add(new TextComponentElement(x, y + 14, width, height, effectData));
      }
    } else if (texts.length > 0) {
      List<TextData> effectData = new ArrayList<>();
      for (String text : texts) {
        effectData.add(new TextData("● "));
        effectData.add(new TextData(text));
        effectData.add(new TextData("\n"));
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
    addList(list,  0, y, width, height, STORING_EFFECTS, storing, -1);
    addList(list, width + 4, y, width, height, TAPPING_EFFECTS, tapping, 1);
  }
}

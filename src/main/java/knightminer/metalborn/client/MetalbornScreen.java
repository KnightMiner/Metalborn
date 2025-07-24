package knightminer.metalborn.client;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.item.metalmind.Metalmind.Usable;
import knightminer.metalborn.menu.MetalbornMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

/** Screen for {@link MetalbornMenu} */
public class MetalbornScreen extends AbstractContainerScreen<MetalbornMenu> {
  /** The ResourceLocation containing the chest GUI texture. */
  private static final ResourceLocation TEXTURE = Metalborn.resource("textures/gui/metalborn.png");
  // tooltips
  private static final String KEY_TAP_START = Metalborn.key("gui", "tap.start");
  private static final String KEY_TAP_STOP = Metalborn.key("gui", "tap.stop");
  private static final String KEY_STORE_START = Metalborn.key("gui", "store.start");
  private static final String KEY_STORE_STOP = Metalborn.key("gui", "store.stop");
  private static final Component STOP_ALL = Metalborn.component("gui", "metalminds.stop_all").withStyle(ChatFormatting.GRAY);
  // texture
  /** U index for all extra elements */
  private static final int ELEMENT_U = 176;
  /** Width of metalmind buttons */
  private static final int BUTTON_WIDTH = 18;
  /** Height of metalmind buttons */
  private static final int BUTTON_HEIGHT = 5;
  /** V index for neutral button */
  private static final int NEUTRAL_V = 0;
  /** V index for hovered button */
  private static final int HOVER_OFFSET = BUTTON_HEIGHT;
  /** V index for storing button */
  private static final int STORING_V = 4 * BUTTON_HEIGHT;
  /** V index for tapping button */
  private static final int TAPPING_V = 6 * BUTTON_HEIGHT;

  /** X coordinate for metalmind button */
  private static final int METALMIND_X = 47;
  /** Y coordinate for info buttons */
  private static final int INFO_Y = 67;
  /** V coordinate for metalmind hover */
  private static final int METALMIND_HOVER_V = 40;
  /** X coordinate for spike button */
  private static final int SPIKE_X = 117;
  /** V coordinate for spike hover */
  private static final int SPIKE_HOVER_V = 52;
  /** Size of the info icons */
  private static final int INFO_SIZE = 12;

  /** X position of the center of the player */
  private static final int PLAYER_X = 88;
  /** Y position of the bottom of the player */
  private static final int PLAYER_Y = 75;


  /** Metalborn data for the player */
  private final MetalbornData data;
  public MetalbornScreen(MetalbornMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    data = MetalbornData.getData(inventory.player);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(graphics);
    super.render(graphics, mouseX, mouseY, partialTicks);
    this.renderTooltip(graphics, mouseX, mouseY);
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
    graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

    // draw player in center, watching you
    assert this.minecraft != null;
    assert this.minecraft.player != null;
    InventoryScreen.renderEntityInInventoryFollowsAngle(graphics, leftPos + PLAYER_X, topPos + PLAYER_Y, 30, 0, 0, this.minecraft.player);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    mouseX -= leftPos;
    mouseY -= topPos;
    for (Slot slot : menu.getMetalmindSlots()) {
      if (!slot.hasItem()) {
        continue;
      }
      Usable usable = menu.canUse(slot.index);
      if (usable != Usable.NEVER) {
        // draw backgrounds, always needed
        int startX = slot.x - 1;
        int startY = slot.y + 17;
        // based on the level and hover, might render another layer
        int level = menu.getMetalmindLevel(slot.index);

        // select button V based on action
        int v = NEUTRAL_V;
        if (level < 0) {
          v = STORING_V;
        } else if (level > 0) {
          v = TAPPING_V;
        }

        // if hovering, offset V index
        if (startY <= mouseY && mouseY < startY + BUTTON_HEIGHT && startX <= mouseX && mouseX < startX + BUTTON_WIDTH) {
          if (level == 0) {
            v += HOVER_OFFSET * usable.ordinal();
          } else {
            v += HOVER_OFFSET;
          }
        }
        // draw the button
        graphics.blit(TEXTURE, startX, startY, ELEMENT_U, v, BUTTON_WIDTH, BUTTON_HEIGHT);
      }
    }

    // draw icon on hovering over the metalmind
    if (INFO_Y <= mouseY && mouseY < INFO_Y + INFO_SIZE) {
      if (METALMIND_X <= mouseX && mouseX < METALMIND_X + INFO_SIZE) {
        graphics.blit(TEXTURE, METALMIND_X, INFO_Y, ELEMENT_U, METALMIND_HOVER_V, INFO_SIZE, INFO_SIZE);
      }
      else if (SPIKE_X <= mouseX && mouseX < SPIKE_X + INFO_SIZE) {
        graphics.blit(TEXTURE, SPIKE_X, INFO_Y, ELEMENT_U, SPIKE_HOVER_V, INFO_SIZE, INFO_SIZE);
      }
    }
  }

  @Override
  protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
    super.renderTooltip(graphics, mouseX, mouseY);

    // draw tooltips on buttons, for tapping and storing
    int checkX = mouseX - leftPos;
    int checkY = mouseY - topPos;
    for (Slot slot : menu.getMetalmindSlots()) {
      // check location and slot filled first, its fastest
      int startX = slot.x - 1;
      int startY = slot.y + 17;
      if (startY <= checkY && checkY < startY + BUTTON_HEIGHT && startX <= checkX && checkX < startX + BUTTON_WIDTH && slot.hasItem()) {
        Usable usable = menu.canUse(slot.index);
        if (usable != Usable.NEVER) {
          int level = menu.getMetalmindLevel(slot.index);
          Component stores = menu.getStores(slot.index);
          List<Component> tooltip = new ArrayList<>(2);
          // if storing, suggest stopping
          if (level < 0) {
            tooltip.add(Component.translatable(KEY_STORE_STOP, stores));
          // not storing and can? suggest doing so
          } else if (usable.canStore()) {
            tooltip.add(Component.translatable(KEY_STORE_START, stores));
          }
          // if tapping, suggest stopping
          if (level > 0) {
            tooltip.add(Component.translatable(KEY_TAP_STOP, stores));
          // not tapping and can? suggest doing so
          } else if (usable.canTap()) {
            tooltip.add(Component.translatable(KEY_TAP_START, stores));
          }
          graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
      }
    }

    // tooltip for hovering the info button
    if (INFO_Y <= checkY && checkY < INFO_Y + INFO_SIZE) {
      if (METALMIND_X <= checkX && checkX < METALMIND_X + INFO_SIZE) {
        List<Component> tooltip = new ArrayList<>();
        if (data.getFeruchemyTooltip(tooltip)) {
          tooltip.add(STOP_ALL);
        }
        graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
      }
      else if (SPIKE_X <= checkX && checkX < SPIKE_X + INFO_SIZE) {
        List<Component> tooltip = new ArrayList<>();
        data.getHemalurgyTooltip(tooltip);
        graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
      }
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    assert minecraft != null && minecraft.player != null && minecraft.gameMode != null;
    // buttons below each slot change metalmind status
    if (!minecraft.player.isSpectator() && (button == 0 || button == 1)) {
      int checkX = (int)mouseX - leftPos;
      int checkY = (int)mouseY - topPos;

      // check if we clicked the metalborn button
      if (METALMIND_X <= checkX && checkX < METALMIND_X + INFO_SIZE && INFO_Y <= checkY && checkY < INFO_Y + INFO_SIZE) {
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 20);
        return true;
      }

      // check if we clicked any slot button
      for (Slot slot : menu.getMetalmindSlots()) {
        int startX = slot.x - 1;
        int startY = slot.y + 17;
        // check mouse inside next as its fastest, and slot having an item
        if (startY <= checkY && checkY < startY + BUTTON_HEIGHT && startX <= checkX && checkX < startX + BUTTON_WIDTH && slot.hasItem()) {
          // must be usable
          Usable usable = menu.canUse(slot.index);
          int level = menu.getMetalmindLevel(slot.index);
          // if we are actively fill or tapping, either button will cancel, though one might reverse
          if (level != 0 || (button == 0 ? usable.canStore() : usable.canTap())) {
            // if tapping, and we can't store, map the storing button to stop tapping
            if (level > 0 && button == 0 && !usable.canStore()) {
              button = 1;
            }
            // if storing, and we can't tap, map the tapping button to stop storing
            if (level < 0 && button == 1 && !usable.canTap()) {
              button = 0;
            }
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, slot.index * 2 + button);
            return true;
          }
        }
      }
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }
}

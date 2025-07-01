package knightminer.metalborn.client;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.MetalbornData;
import knightminer.metalborn.menu.MetalbornMenu;
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
  // texture
  /** U index for all extra elements */
  private static final int ELEMENT_U = 176;
  /** Width of metalmind buttons */
  private static final int SLOT_WIDTH = 18;
  /** Width of metalmind buttons */
  private static final int BUTTON_WIDTH = SLOT_WIDTH / 2;
  /** Height of metalmind buttons */
  private static final int BUTTON_HEIGHT = 5;
  /** V index for neutral buttons */
  private static final int NEUTRAL_V = 0;
  /** V index for hovered buttons */
  private static final int HOVER_V = BUTTON_HEIGHT;
  /** V index for active buttons */
  private static final int ACTIVE_V = 2 * BUTTON_HEIGHT;
  /** V index for active and hovered buttons */
  private static final int HOVER_ACTIVE_V = 3 * BUTTON_HEIGHT;

  /** X coordinate for metalmind button */
  private static final int METALMIND_X = 47;
  /** Y coordinate for info buttons */
  private static final int INFO_Y = 67;
  /** V coordinate for metalmind hover */
  private static final int METALMIND_HOVER_V = 20;
  /** X coordinate for spike button */
  private static final int SPIKE_X = 117;
  /** V coordinate for spike hover */
  private static final int SPIKE_HOVER_V = 32;
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
      if (slot.hasItem() && menu.canUse(slot.index)) {
        // draw backgrounds, always needed
        int startX = slot.x - 1;
        int startY = slot.y + 17;
        graphics.blit(TEXTURE, startX, startY, ELEMENT_U, NEUTRAL_V, SLOT_WIDTH, BUTTON_HEIGHT);
        // based on the level and hover, might render another layer
        int level = menu.getMetalmindLevel(slot.index);
        boolean hoverY = startY <= mouseY && mouseY < startY + BUTTON_HEIGHT;

        // draw left button
        boolean hoverLeft = hoverY && startX <= mouseX && mouseX < startX + BUTTON_WIDTH;
        if (hoverLeft || level < 0) {
          int v = hoverLeft ? (level < 0 ? HOVER_ACTIVE_V : HOVER_V) : ACTIVE_V;
          graphics.blit(TEXTURE, startX, startY, ELEMENT_U, v, BUTTON_WIDTH, BUTTON_HEIGHT);
        }

        // draw right button
        boolean hoverRight = hoverY && startX + BUTTON_WIDTH <= mouseX && mouseX < startX + SLOT_WIDTH;
        if (hoverRight || level > 0) {
          int v = hoverRight ? (level > 0 ? HOVER_ACTIVE_V : HOVER_V) : ACTIVE_V;
          graphics.blit(TEXTURE, startX + BUTTON_WIDTH, startY, ELEMENT_U + BUTTON_WIDTH, v, BUTTON_WIDTH, BUTTON_HEIGHT);
        }
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
      if (slot.hasItem() && menu.canUse(slot.index)) {
        int startX = slot.x - 1;
        int startY = slot.y + 17;
        if (startY <= checkY && checkY < startY + BUTTON_HEIGHT && startX <= checkX) {
          int level = menu.getMetalmindLevel(slot.index);
          Component stores = menu.getStores(slot.index);
          if (checkX < startX + BUTTON_WIDTH) {
            graphics.renderTooltip(font, Component.translatable(level < 0 ? KEY_STORE_STOP : KEY_STORE_START, stores), mouseX, mouseY);
          } else if (checkX < startX + SLOT_WIDTH) {
            graphics.renderTooltip(font, Component.translatable(level > 0 ? KEY_TAP_STOP : KEY_TAP_START, stores), mouseX, mouseY);
          }
        }
      }
    }

    // tooltip for hovering the info button
    if (INFO_Y <= checkY && checkY < INFO_Y + INFO_SIZE) {
      if (METALMIND_X <= checkX && checkX < METALMIND_X + INFO_SIZE) {
        List<Component> tooltip = new ArrayList<>();
        data.getFeruchemyTooltip(tooltip);
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
    if (!minecraft.player.isSpectator() && button == 0) {
      int checkX = (int)mouseX - leftPos;
      int checkY = (int)mouseY - topPos;
      for (Slot slot : menu.getMetalmindSlots()) {
        if (slot.hasItem() && menu.canUse(slot.index)) {
          int startX = slot.x - 1;
          int startY = slot.y + 17;
          if (startY <= checkY && checkY < startY + BUTTON_HEIGHT && startX <= checkX) {
            // left button is 2i+0
            if (checkX < startX + BUTTON_WIDTH) {
              minecraft.gameMode.handleInventoryButtonClick(menu.containerId, slot.index * 2);
              return true;
            // right button is 2i+1
            } else if (checkX < startX + SLOT_WIDTH) {
              minecraft.gameMode.handleInventoryButtonClick(menu.containerId, slot.index * 2 + 1);
              return true;
            }
          }
        }
      }
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }
}

package knightminer.metalborn.client;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.menu.ForgeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Screen for the metal forge block */
public class ForgeScreen extends AbstractContainerScreen<ForgeMenu> {
  /** The ResourceLocation containing the chest GUI texture. */
  private static final ResourceLocation TEXTURE = Metalborn.resource("textures/gui/forge.png");

  public ForgeScreen(ForgeMenu menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title);
  }

  @Override
  protected void init() {
    super.init();
    this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
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

    // draw fire for fuel
    if (menu.isFueled()) {
      int fuel = this.menu.getFuelAmount();
      graphics.blit(TEXTURE, leftPos + 26, topPos + 27 + 12 - fuel, 176, 12 - fuel, 14, fuel + 1);
    }

    int progress = this.menu.getRecipeProgress();
    if (progress > 0) {
      graphics.blit(TEXTURE, leftPos + 89, topPos + 34, 176, 14, progress + 1, 16);
    }
  }
}

package mekanism.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Mouse;

@OnlyIn(Dist.CLIENT)
public abstract class GuiMekanism extends ContainerScreen implements IGuiWrapper {

    private Set<GuiElement> guiElements = new HashSet<>();

    public GuiMekanism(Container container) {
        super(container);
    }

    public static boolean isTextboxKey(char c, int i) {
        return i == GLFW.GLFW_KEY_BACKSPACE || i == GLFW.GLFW_KEY_DELETE || i == GLFW.GLFW_KEY_LEFT || i == GLFW.GLFW_KEY_RIGHT || i == GLFW.GLFW_KEY_END ||
               i == GLFW.GLFW_KEY_HOME || isKeyComboCtrlA(i) || isKeyComboCtrlC(i) || isKeyComboCtrlV(i) || isKeyComboCtrlX(i);
    }

    public Set<GuiElement> getGuiElements() {
        return guiElements;
    }

    protected float getNeededScale(String text, int maxX) {
        int length = font.getStringWidth(text);
        return length <= maxX ? 1 : (float) maxX / length;
    }

    protected void addGuiElement(GuiElement element) {
        guiElements.add(element);
    }

    /**
     * returns scale
     */
    public void renderScaledText(String text, int x, int y, int color, int maxX) {
        int length = font.getStringWidth(text);
        if (length <= maxX) {
            font.drawString(text, x, y, color);
        } else {
            float scale = (float) maxX / length;
            float reverse = 1 / scale;
            float yAdd = 4 - (scale * 8) / 2F;
            GlStateManager.pushMatrix();
            GlStateManager.translatef(scale, scale, scale);
            font.drawString(text, (int) (x * reverse), (int) ((y * reverse) + yAdd), color);
            GlStateManager.popMatrix();
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        //Ensure that the GL color is white, as drawing rectangles, text boxes, or even text might have changed the color from
        // what we assume it is at the start. This prevents any unintentional color state leaks. GlStateManager, will ensure that
        // GL changes only get ran if it is not already the color we are assuming it is.
        // This is called here as, all extenders of GuiMekanism that overwrite this method call super on it at the end of their
        // implementation, and almost all have the color get changed at one point or another due to drawing text
        MekanismRenderer.resetColor();
        guiElements.forEach(element -> element.renderForeground(xAxis, yAxis));
    }

    protected boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY) {
        return isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY);
    }

    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        //Ensure the GL color is white as mods adding an overlay (such as JEI for bookmarks), might have left
        // it in an unexpected state.
        MekanismRenderer.resetColor();
        minecraft.textureManager.bindTexture(getGuiLocation());
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        drawGuiContainerBackgroundLayer(xAxis, yAxis);
        guiElements.forEach(element -> element.renderBackground(xAxis, yAxis, guiLeft, guiTop));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        guiElements.forEach(element -> element.preMouseClicked(xAxis, yAxis, button));
        super.mouseClicked(mouseX, mouseY, button);
        guiElements.forEach(element -> element.mouseClicked(xAxis, yAxis, button));
    }

    @Override
    public void drawTexturedRect(int x, int y, int u, int v, int w, int h) {
        drawTexturedModalRect(x, y, u, v, w, h);
    }

    @Override
    public void drawTexturedRectFromIcon(int x, int y, TextureAtlasSprite icon, int w, int h) {
        drawTexturedModalRect(x, y, icon, w, h);
    }

    @Override
    public void displayTooltip(String s, int x, int y) {
        drawHoveringText(s, x, y);
        //Fix unwanted lighting changes made by drawHoveringText
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public void displayTooltips(List<String> list, int xAxis, int yAxis) {
        drawHoveringText(list, xAxis, yAxis);
        //Fix unwanted lighting changes made by drawHoveringText
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public FontRenderer getFont() {
        return fontRenderer;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long ticks) {
        super.mouseClickMove(mouseX, mouseY, button, ticks);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        guiElements.forEach(element -> element.mouseClickMove(xAxis, yAxis, button, ticks));
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int type) {
        super.mouseReleased(mouseX, mouseY, type);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        guiElements.forEach(element -> element.mouseReleased(xAxis, yAxis, type));
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int delta = Mouse.getEventDWheel();
        if (delta != 0) {
            int xAxis = Mouse.getEventX() * width / minecraft.mainWindow.getWidth() - guiLeft;
            int yAxis = height - Mouse.getEventY() * height / minecraft.mainWindow.getHeight() - 1 - guiTop;
            guiElements.forEach(element -> element.mouseWheel(xAxis, yAxis, delta));
        }
    }

    protected FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    protected void renderCenteredText(int leftMargin, int areaWidth, int y, int color, String text) {
        int textWidth = font.getStringWidth(text);
        int centerX = leftMargin + (areaWidth / 2) - (textWidth / 2);
        font.drawString(text, centerX, y, color);
    }

    protected void drawColorIcon(int x, int y, EnumColor color, float alpha) {
        if (color != null) {
            drawRect(x, y, x + 16, y + 16, MekanismRenderer.getColorARGB(color, alpha));
            MekanismRenderer.resetColor();
        }
    }

    protected void renderItem(@Nonnull ItemStack stack, int xAxis, int yAxis) {
        renderItem(stack, xAxis, yAxis, 1);
    }

    protected void renderItem(@Nonnull ItemStack stack, int xAxis, int yAxis, float scale) {
        if (!stack.isEmpty()) {
            try {
                GlStateManager.pushMatrix();
                GlStateManager.enableDepth();
                RenderHelper.enableGUIStandardItemLighting();
                if (scale != 1) {
                    GlStateManager.translatef(scale, scale, scale);
                }
                itemRender.renderItemAndEffectIntoGUI(stack, xAxis, yAxis);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableDepth();
                GlStateManager.popMatrix();
            } catch (Exception e) {
                Mekanism.logger.error("Failed to render stack into gui: " + stack, e);
            }
        }
    }

    protected abstract ResourceLocation getGuiLocation();
}
package mekanism.client.gui;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.render.GLSMHelper;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public abstract class GuiMekanism extends GuiContainer implements IGuiWrapper {

    private Set<GuiElement> guiElements = new HashSet<>();

    public GuiMekanism(Container container) {
        super(container);
    }

    public static boolean isTextboxKey(char c, int i) {
        return i == Keyboard.KEY_BACK || i == Keyboard.KEY_DELETE || i == Keyboard.KEY_LEFT || i == Keyboard.KEY_RIGHT || i == Keyboard.KEY_END ||
               i == Keyboard.KEY_HOME || isKeyComboCtrlA(i) || isKeyComboCtrlC(i) || isKeyComboCtrlV(i) || isKeyComboCtrlX(i);
    }

    public Set<GuiElement> getGuiElements() {
        return guiElements;
    }

    protected float getNeededScale(String text, int maxX) {
        int length = fontRenderer.getStringWidth(text);
        return length <= maxX ? 1 : (float) maxX / length;
    }

    protected void addGuiElement(GuiElement element) {
        guiElements.add(element);
    }

    /**
     * returns scale
     */
    public void renderScaledText(String text, int x, int y, int color, int maxX) {
        int length = fontRenderer.getStringWidth(text);
        if (length <= maxX) {
            fontRenderer.drawString(text, x, y, color);
        } else {
            float scale = (float) maxX / length;
            float reverse = 1 / scale;
            float yAdd = 4 - (scale * 8) / 2F;
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale);
            fontRenderer.drawString(text, (int) (x * reverse), (int) ((y * reverse) + yAdd), color);
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
        GLSMHelper.INSTANCE.resetColor();
        guiElements.forEach(element -> {
            element.renderForeground(xAxis, yAxis);
            //Continue ensuring color is what we are assuming.
            GLSMHelper.INSTANCE.resetColor();
        });
    }

    protected boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY) {
        return isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY);
    }

    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(getGuiLocation());
        //Ensure the GL color is white as mods adding an overlay (such as JEI for bookmarks), might have left
        // it in an unexpected state.
        GLSMHelper.INSTANCE.resetColor();
        drawTexturedModalRect(guiLeft, guiTop);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        drawGuiContainerBackgroundLayer(xAxis, yAxis);
        //Ensure that the GL color is white, as drawing rectangles, text boxes, or even text might have changed the color from
        // what we assume it is at the start. This prevents any unintentional color state leaks. GlStateManager, will ensure that
        // GL changes only get ran if it is not already the color we are assuming it is.
        GLSMHelper.INSTANCE.resetColor();
        guiElements.forEach(element -> {
            element.renderBackground(xAxis, yAxis, guiLeft, guiTop);
            //Continue ensuring color is what we are assuming.
            GLSMHelper.INSTANCE.resetColor();
        });
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
    }

    @Override
    public void displayTooltips(List<String> list, int xAxis, int yAxis) {
        drawHoveringText(list, xAxis, yAxis);
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
            int xAxis = Mouse.getEventX() * width / mc.displayWidth - guiLeft;
            int yAxis = height - Mouse.getEventY() * height / mc.displayHeight - 1 - guiTop;
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
        int textWidth = fontRenderer.getStringWidth(text);
        int centerX = leftMargin + (areaWidth / 2) - (textWidth / 2);
        fontRenderer.drawString(text, centerX, y, color);
    }

    protected void drawColorIcon(int x, int y, EnumColor color, float alpha) {
        if (color != null) {
            drawRect(x, y, x + 16, y + 16, MekanismRenderer.getColorARGB(color, alpha));
        }
    }

    protected void renderItem(@Nonnull ItemStack stack, int xAxis, int yAxis) {
        renderItem(stack, xAxis, yAxis, 1);
    }

    protected void renderItem(@Nonnull ItemStack stack, int xAxis, int yAxis, float scale) {
        if (!stack.isEmpty()) {
            //TODO: Is this try catch even needed, some places had it
            try {
                MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).enableDepth().enableGUIStandardItemLighting();
                if (scale != 1) {
                    GlStateManager.scale(scale, scale, scale);
                }
                itemRender.renderItemAndEffectIntoGUI(stack, xAxis, yAxis);
                renderHelper.cleanup();
            } catch (Exception ignored) {
            }
        }
    }

    protected void drawTexturedModalRect(int x, int y) {
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }

    protected void drawTexturedModalRect(int x, int y, int textureX, boolean inBounds, int size) {
        drawTexturedModalRect(x, y, textureX, 0, inBounds, size);
    }

    protected void drawTexturedModalRect(int x, int y, int textureX, int textureY, boolean inBounds, int size) {
        drawTexturedModalRect(x, y, textureX, inBounds ? textureY : textureY + size, size, size);
    }

    protected abstract ResourceLocation getGuiLocation();
}
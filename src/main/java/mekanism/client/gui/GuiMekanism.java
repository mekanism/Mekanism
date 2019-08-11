package mekanism.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.config.GuiUtils;
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
        int length = getStringWidth(text);
        return length <= maxX ? 1 : (float) maxX / length;
    }

    protected void addGuiElement(GuiElement element) {
        guiElements.add(element);
    }

    public int getStringWidth(ITextComponent component) {
        //TODO: See if this should be calculated in a different way
        return getStringWidth(component.getFormattedText());
    }

    public int getStringWidth(String text) {
        //TODO: Replace uses of this/the other getStringWidth with drawCenteredText where appropriate
        return font.getStringWidth(text);
    }

    public int drawString(ITextComponent component, int x, int y, int color) {
        //TODO: Check if color actually does anything
        return drawString(component.getFormattedText(), x, y, color);
    }

    public int drawString(String text, int x, int y, int color) {
        //TODO: Eventually make drawString(ITextComponent) be what gets used in places
        return font.drawString(text, x, y, color);
    }

    protected void drawCenteredText(ITextComponent component, int y, int color) {
        drawCenteredText(component, 0, 0, y, color);
    }

    protected void drawCenteredText(ITextComponent component, int leftMargin, int y, int color) {
        drawCenteredText(component, leftMargin, 0, y, color);
    }

    protected void drawCenteredText(ITextComponent component, int leftMargin, int areaWidth, int y, int color) {
        int textWidth = getStringWidth(component);
        int centerX = leftMargin + (areaWidth / 2) - (textWidth / 2);
        drawString(component, centerX, y, color);
    }

    public void renderScaledText(ITextComponent component, int x, int y, int color, int maxX) {
        renderScaledText(component.getFormattedText(), x, y, color, maxX);
    }

    /**
     * returns scale
     */
    public void renderScaledText(String text, int x, int y, int color, int maxX) {
        int length = getStringWidth(text);
        if (length <= maxX) {
            drawString(text, x, y, color);
        } else {
            float scale = (float) maxX / length;
            float reverse = 1 / scale;
            float yAdd = 4 - (scale * 8) / 2F;
            GlStateManager.pushMatrix();
            GlStateManager.translatef(scale, scale, scale);
            drawString(text, (int) (x * reverse), (int) ((y * reverse) + yAdd), color);
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
        drawTexturedRect(guiLeft, guiTop, 0, 0, xSize, ySize);
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
        blit(x, y, u, v, w, h);
    }

    @Override
    public void drawTexturedRectFromIcon(int x, int y, TextureAtlasSprite icon, int w, int h) {
        drawTexturedModalRect(x, y, icon, w, h);
    }

    @Override
    public void displayTooltip(ITextComponent component, int x, int y) {
        this.displayTooltips(Collections.singletonList(component), x, y);
    }

    @Override
    public void displayTooltips(List<ITextComponent> components, int xAxis, int yAxis) {
        //TODO: Evaluate if we want to use this for splitting the text
        List<String> toolTips = components.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList());
        GuiUtils.drawHoveringText(toolTips, xAxis, yAxis, width, height, -1, font);
        //Fix unwanted lighting changes made by drawHoveringText
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public FontRenderer getFont() {
        return font;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long ticks) {
        super.mouseClickMove(mouseX, mouseY, button, ticks);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        guiElements.forEach(element -> element.mouseClickMove(xAxis, yAxis, button, ticks));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int type) {
        super.mouseReleased(mouseX, mouseY, type);
        double xAxis = mouseX - guiLeft;
        double yAxis = mouseY - guiTop;
        guiElements.forEach(element -> element.mouseReleased(xAxis, yAxis, type));
        return true;
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

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.renderd(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
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
                GlStateManager.enableDepthTest();
                RenderHelper.enableGUIStandardItemLighting();
                if (scale != 1) {
                    GlStateManager.translatef(scale, scale, scale);
                }
                itemRenderer.renderItemAndEffectIntoGUI(stack, xAxis, yAxis);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableDepthTest();
                GlStateManager.popMatrix();
            } catch (Exception e) {
                Mekanism.logger.error("Failed to render stack into gui: " + stack, e);
            }
        }
    }

    protected abstract ResourceLocation getGuiLocation();
}
package mekanism.client.gui.element;

import java.util.List;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiElement extends Widget {

    public static final Minecraft minecraft = Minecraft.getInstance();

    protected final IGuiWrapper guiObj;

    protected boolean playClickSound;

    public GuiElement(IGuiWrapper gui, int x, int y, int width, int height, String text) {
        super(x, y, width, height, text);
        guiObj = gui;
    }

    public void renderForeground(int mouseX, int mouseY, int xAxis, int yAxis) {
    }

    public void displayTooltip(ITextComponent component, int xAxis, int yAxis) {
        guiObj.displayTooltip(component, xAxis, yAxis);
    }

    public void displayTooltips(List<ITextComponent> list, int xAxis, int yAxis) {
        guiObj.displayTooltips(list, xAxis, yAxis);
    }

    public int drawString(ITextComponent component, int x, int y, int color) {
        return drawString(component.getFormattedText(), x, y, color);
    }

    public void renderScaledText(ITextComponent component, int x, int y, int color, int maxX) {
        renderScaledText(component.getFormattedText(), x, y, color, maxX);
    }

    public int drawString(String text, int x, int y, int color) {
        return getFontRenderer().drawString(text, x, y, color);
    }

    public int getStringWidth(ITextComponent component) {
        return getFontRenderer().getStringWidth(component.getFormattedText());
    }

    public int getStringWidth(String text) {
        return getFontRenderer().getStringWidth(text);
    }

    public float getNeededScale(ITextComponent text, int maxX) {
        int length = getStringWidth(text);
        return length <= maxX ? 1 : (float) maxX / length;
    }

    protected void renderCenteredText(ITextComponent text, int left, int y, int color) {
        int textWidth = getStringWidth(text);
        int centerX = left - (textWidth / 2);
        drawString(text.getString(), centerX, y, color);
    }

    protected void renderScaledCenteredText(ITextComponent text, int left, int y, int color, float scale) {
        int textWidth = getStringWidth(text);
        int centerX = left - (int)((textWidth / 2) * scale);
        renderTextWithScale(text.getString(), centerX, y, color, scale);
    }

    public void renderScaledText(String text, int x, int y, int color, int maxX) {
        int length = getFontRenderer().getStringWidth(text);

        if (length <= maxX) {
            drawString(text, x, y, color);
        } else {
            renderTextWithScale(text, x, y, color, (float) maxX / length);
        }
        //Make sure the color does not leak from having drawn the string
        MekanismRenderer.resetColor();
    }

    public void renderTextWithScale(String text, int x, int y, int color, float scale) {
        float reverse = 1 / scale;
        float yAdd = 4 - (scale * 8) / 2F;

        RenderSystem.pushMatrix();
        RenderSystem.scalef(scale, scale, scale);
        drawString(text, (int) (x * reverse), (int) ((y * reverse) + yAdd), color);
        RenderSystem.popMatrix();
        MekanismRenderer.resetColor();
    }

    protected FontRenderer getFontRenderer() {
        return guiObj.getFont();
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        //The code for clicked and isMouseOver is the same. Overriding it here lets us override isMouseOver in sub classes
        // and have it propagate to clicking
        return isMouseOver(mouseX, mouseY);
    }

    /**
     * Override this to render the button with a different x position than this GuiElement
     */
    protected int getButtonX() {
        return x;
    }

    /**
     * Override this to render the button with a different y position than this GuiElement
     */
    protected int getButtonY() {
        return y;
    }

    /**
     * Override this to render the button with a different width than this GuiElement
     */
    protected int getButtonWidth() {
        return width;
    }

    /**
     * Override this to render the button with a different height than this GuiElement
     */
    protected int getButtonHeight() {
        return height;
    }

    /**
     * Override this if you do not want renderButton to reset the color before drawing
     */
    protected boolean resetColorBeforeRender() {
        return true;
    }

    //TODO: Convert this stuff into a javadoc
    //Based off how it is drawn in Widget, except that instead of drawing left half and right half, we draw all four corners individually
    // The benefit of drawing all four corners instead of just left and right halves, is that we ensure we include the bottom black bar of the texture
    // Math has also been added to fix rendering odd size buttons.
    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        drawButton(mouseX, mouseY);
    }

    //This method exists so that we don't have to rely on having a path to super.renderButton if we want to draw a background button
    protected void drawButton(int mouseX, int mouseY) {
        if (resetColorBeforeRender()) {
            //TODO: Support alpha like super? Is there a point
            MekanismRenderer.resetColor();
        }
        //TODO: Convert this to being two different 16x48 images, one for with border and one for buttons without a black border?
        // And then make it so that they can stretch out to be any size (make this make use of the renderExtendedTexture method
        MekanismRenderer.bindTexture(WIDGETS_LOCATION);
        //TODO: This can use isHovered() once we fix the isHovered logic
        int i = getYImage(isMouseOver(mouseX, mouseY));
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

        int width = getButtonWidth();
        int height = getButtonHeight();
        int halfWidthLeft = width / 2;
        int halfWidthRight = width % 2 == 0 ? halfWidthLeft : halfWidthLeft + 1;
        int halfHeightTop = height / 2;
        int halfHeightBottom = height % 2 == 0 ? halfHeightTop : halfHeightTop + 1;
        int position = 46 + i * 20;

        int x = getButtonX();
        int y = getButtonY();
        //Left Top Corner
        blit(x, y, 0, position, halfWidthLeft, halfHeightTop);
        //Left Bottom Corner
        blit(x, y + halfHeightTop, 0, position + 20 - halfHeightBottom, halfWidthLeft, halfHeightBottom);
        //Right Top Corner
        blit(x + halfWidthLeft, y, 200 - halfWidthRight, position, halfWidthRight, halfHeightTop);
        //Right Bottom Corner
        blit(x + halfWidthLeft, y + halfHeightTop, 200 - halfWidthRight, position + 20 - halfHeightBottom, halfWidthRight, halfHeightBottom);

        //TODO: Add support for buttons that are larger than 200x20 in either direction (most likely would be in the height direction
        // Can use a lot of the same logic as GuiMekanism does for its background

        this.renderBg(minecraft, mouseX, mouseY);

        String message = getMessage();
        //Only attempt to draw the message if we have a message to draw
        if (!message.isEmpty()) {
            //TODO: Improve the math for this so that it calculates the y value better
            drawCenteredString(getFontRenderer(), message, x + halfWidthLeft, y + (height - 8) / 2,
                  getFGColor() | MathHelper.ceil(alpha * 255.0F) << 24);
        }
        RenderSystem.disableBlend();
    }

    protected void renderExtendedTexture(ResourceLocation resource, int sideWidth, int sideHeight) {
        GuiUtils.renderExtendedTexture(resource, sideWidth, sideHeight, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
    }

    @Override
    public void playDownSound(SoundHandler soundHandler) {
        if (playClickSound) {
            super.playDownSound(soundHandler);
        }
    }

    protected void drawTiledSprite(int xPosition, int yPosition, int yOffset, int desiredWidth, int desiredHeight, TextureAtlasSprite sprite) {
        GuiUtils.drawTiledSprite(xPosition, yPosition, yOffset, desiredWidth, desiredHeight, sprite, 16, 16, getBlitOffset());
    }

    @FunctionalInterface
    public interface IHoverable {

        void onHover(GuiElement element, int mouseX, int mouseY);
    }

    @FunctionalInterface
    public interface IClickable {

        void onClick(GuiElement element, int mouseX, int mouseY);
    }
}
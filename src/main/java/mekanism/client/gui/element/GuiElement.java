package mekanism.client.gui.element;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.IFancyFontRenderer;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiElement extends Widget implements IFancyFontRenderer {

    private static final NumberFormat intFormatter = NumberFormat.getIntegerInstance();
    public static final ResourceLocation BUTTON_LOCATION = MekanismUtils.getResource(ResourceType.GUI, "button.png");
    private static final int BUTTON_TEX_X = 200, BUTTON_TEX_Y = 60;

    public static final Minecraft minecraft = Minecraft.getInstance();

    private final List<GuiElement> children = new ArrayList<>();

    protected final IGuiWrapper guiObj;

    protected boolean playClickSound;

    public GuiElement(IGuiWrapper gui, int x, int y, int width, int height, String text) {
        super(x, y, width, height, text);
        guiObj = gui;
    }

    protected void addChild(GuiElement element) {
        children.add(element);
    }

    public void onRenderForeground(int mouseX, int mouseY) {
        RenderSystem.pushMatrix();
        // fix render offset
        RenderSystem.translatef(-guiObj.getLeft(), -guiObj.getTop(), 0);
        // render background overlay and children above everything else
        renderBackgroundOverlay(mouseX, mouseY);
        children.forEach(child -> child.render(mouseX, mouseY, 0));
        RenderSystem.popMatrix();
        renderForeground(mouseX, mouseY);
        children.forEach(child -> child.renderForeground(mouseX, mouseY));
    }

    public void renderForeground(int mouseX, int mouseY) {}

    public void renderBackgroundOverlay(int mouseX, int mouseY) {}

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        children.forEach(child -> child.renderToolTip(mouseX, mouseY));
    }

    public void displayTooltip(ITextComponent component, int xAxis, int yAxis) {
        guiObj.displayTooltip(component, xAxis, yAxis);
    }

    public void displayTooltips(List<ITextComponent> list, int xAxis, int yAxis) {
        guiObj.displayTooltips(list, xAxis, yAxis);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean ret = false;
        for (GuiElement element : children) {
            ret |= element.mouseClicked(mouseX, mouseY, button);
        }
        ret |= super.mouseClicked(mouseX, mouseY, button);
        return ret;
    }

    @Override
    public FontRenderer getFont() {
        return guiObj.getFont();
    }

    @Override
    public int getXSize() {
        return width;
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
        MekanismRenderer.bindTexture(BUTTON_LOCATION);
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
        int position = i * 20;

        int x = getButtonX();
        int y = getButtonY();
        //Left Top Corner
        blit(x, y, 0, position, halfWidthLeft, halfHeightTop, BUTTON_TEX_X, BUTTON_TEX_Y);
        //Left Bottom Corner
        blit(x, y + halfHeightTop, 0, position + 20 - halfHeightBottom, halfWidthLeft, halfHeightBottom, BUTTON_TEX_X, BUTTON_TEX_Y);
        //Right Top Corner
        blit(x + halfWidthLeft, y, 200 - halfWidthRight, position, halfWidthRight, halfHeightTop, BUTTON_TEX_X, BUTTON_TEX_Y);
        //Right Bottom Corner
        blit(x + halfWidthLeft, y + halfHeightTop, 200 - halfWidthRight, position + 20 - halfHeightBottom, halfWidthRight, halfHeightBottom, BUTTON_TEX_X, BUTTON_TEX_Y);

        //TODO: Add support for buttons that are larger than 200x20 in either direction (most likely would be in the height direction
        // Can use a lot of the same logic as GuiMekanism does for its background

        this.renderBg(minecraft, mouseX, mouseY);

        String message = getMessage();
        //Only attempt to draw the message if we have a message to draw
        if (!message.isEmpty()) {
            //TODO: Improve the math for this so that it calculates the y value better
            drawCenteredString(getFont(), message, x + halfWidthLeft, y + (height - 8) / 2,
                  getFGColor() | MathHelper.ceil(alpha * 255.0F) << 24);
        }
        RenderSystem.disableBlend();
    }

    protected void renderExtendedTexture(ResourceLocation resource, int sideWidth, int sideHeight) {
        GuiUtils.renderExtendedTexture(resource, sideWidth, sideHeight, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
    }

    protected void renderBackgroundTexture(ResourceLocation resource, int sideWidth, int sideHeight) {
        GuiUtils.renderBackgroundTexture(resource, sideWidth, sideHeight, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight(), 256, 256);
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

    protected static String formatInt(long l) {
        return intFormatter.format(l);
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
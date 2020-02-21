package mekanism.client.gui.element;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

public abstract class GuiElement extends Widget {

    public static final Minecraft minecraft = Minecraft.getInstance();

    protected final IGuiWrapper guiObj;

    protected boolean playClickSound;

    public GuiElement(IGuiWrapper gui, int x, int y, int width, int height, String text) {
        super(x, y, width, height, text);
        guiObj = gui;
    }

    public void renderForeground(int mouseX, int mouseY, int xAxis, int yAxis) {
        if (isMouseOver(mouseX, mouseY)) {//.isHovered()) {
            //TODO: Should it pass it the proper mouseX and mouseY. Probably, though buttons may have to be redone slightly then
            renderToolTip(xAxis, yAxis);
        }
    }

    public void displayTooltip(ITextComponent component, int xAxis, int yAxis) {
        guiObj.displayTooltip(component, xAxis, yAxis);
    }

    public void displayTooltips(List<ITextComponent> list, int xAxis, int yAxis) {
        guiObj.displayTooltips(list, xAxis, yAxis);
    }

    public int drawString(ITextComponent component, int x, int y, int color) {
        //TODO: Check if color actually does anything
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

    public float getNeededScale(ITextComponent text, int maxX) {
        int length = getStringWidth(text);
        return length <= maxX ? 1 : (float) maxX / length;
    }

    public void renderScaledText(String text, int x, int y, int color, int maxX) {
        int length = getFontRenderer().getStringWidth(text);

        if (length <= maxX) {
            drawString(text, x, y, color);
        } else {
            float scale = (float) maxX / length;
            float reverse = 1 / scale;
            float yAdd = 4 - (scale * 8) / 2F;

            RenderSystem.pushMatrix();
            RenderSystem.scalef(scale, scale, scale);
            drawString(text, (int) (x * reverse), (int) ((y * reverse) + yAdd), color);
            RenderSystem.popMatrix();
        }
        //Make sure the color does not leak from having drawn the string
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

    //TODO: Better name?
    protected void renderExtendedTexture(ResourceLocation resource, int sideWidth, int sideHeight) {
        //TODO: Can some of this code that also exists in GuiMekanism be moved to some util class or something
        //TODO: Do we want to add in some validation here about dimensions
        int left = getButtonX();
        int top = getButtonY();
        int textureWidth = 2 * sideWidth + 1;
        int textureHeight = 2 * sideHeight + 1;
        int centerWidth = getButtonWidth() - 2 * sideWidth;
        int centerHeight = getButtonHeight() - 2 * sideHeight;
        int leftEdgeEnd = left + sideHeight;
        int rightEdgeStart = leftEdgeEnd + centerWidth;
        int topEdgeEnd = top + sideWidth;
        int bottomEdgeStart = topEdgeEnd + centerHeight;
        minecraft.textureManager.bindTexture(resource);
        //Left Side
        //Top Left Corner
        blit(left, top, 0, 0, sideWidth, sideHeight, textureWidth, textureHeight);
        //Left Middle
        if (centerHeight > 0) {
            blit(left, topEdgeEnd, sideWidth, centerHeight, 0, sideHeight, sideWidth, 1, textureWidth, textureHeight);
        }
        //Bottom Left Corner
        blit(left, bottomEdgeStart, 0, sideHeight + 1, sideWidth, sideHeight, textureWidth, textureHeight);

        //Middle
        if (centerWidth > 0) {
            //Top Middle
            blit(leftEdgeEnd, top, centerWidth, sideHeight, sideWidth, 0, 1, sideHeight, textureWidth, textureHeight);
            if (centerHeight > 0) {
                //Center
                blit(leftEdgeEnd, topEdgeEnd, centerWidth, centerHeight, sideWidth, sideHeight, 1, 1, textureWidth, textureHeight);
            }
            //Bottom Middle
            blit(leftEdgeEnd, bottomEdgeStart, centerWidth, sideHeight, sideWidth, sideHeight + 1, 1, sideHeight, textureWidth, textureHeight);
        }

        //Right side
        //Top Right Corner
        blit(rightEdgeStart, top, sideWidth + 1, 0, sideWidth, sideHeight, textureWidth, textureHeight);
        //Right Middle
        if (centerHeight > 0) {
            blit(rightEdgeStart, topEdgeEnd, sideWidth, centerHeight, sideWidth + 1, sideHeight, sideWidth, 1, textureWidth, textureHeight);
        }
        //Bottom Right Corner
        blit(rightEdgeStart, bottomEdgeStart, sideWidth + 1, sideHeight + 1, sideWidth, sideHeight, textureWidth, textureHeight);
    }

    @Override
    public void playDownSound(SoundHandler soundHandler) {
        if (playClickSound) {
            super.playDownSound(soundHandler);
        }
    }

    protected void drawTiledSprite(int xPosition, int yPosition, int yOffset, int desiredWidth, int desiredHeight, TextureAtlasSprite sprite) {
        drawTiledSprite(xPosition, yPosition, yOffset, desiredWidth, desiredHeight, sprite, 16, 16);
    }

    protected void drawTiledSprite(int xPosition, int yPosition, int yOffset, int desiredWidth, int desiredHeight, TextureAtlasSprite sprite, int textureWidth, int textureHeight) {
        MekanismRenderer.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        final int xTileCount = desiredWidth / textureWidth;
        final int xRemainder = desiredWidth - (xTileCount * textureWidth);
        final int yTileCount = desiredHeight / textureHeight;
        final int yRemainder = desiredHeight - (yTileCount * textureHeight);
        final int yStart = yPosition + yOffset;
        int zLevel = getBlitOffset();
        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            int width = (xTile == xTileCount) ? xRemainder : textureWidth;
            if (width > 0) {
                int x = xPosition + (xTile * textureWidth);
                int maskRight = textureWidth - width;
                for (int yTile = 0; yTile <= yTileCount; yTile++) {
                    int height = (yTile == yTileCount) ? yRemainder : textureHeight;
                    if (height > 0) {
                        int y = yStart - ((yTile + 1) * textureHeight);
                        int maskTop = textureHeight - height;
                        drawTextureWithMasking(x, y, sprite, maskTop, maskRight, zLevel, textureWidth, textureHeight);
                    }
                }
            }
        }
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
    }

    private static void drawTextureWithMasking(double xCoord, double yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, double zLevel, int textureWidth, int textureHeight) {
        float uMin = textureSprite.getMinU();
        float uMax = textureSprite.getMaxU();
        float vMin = textureSprite.getMinV();
        float vMax = textureSprite.getMaxV();
        uMax = (float) (uMax - (maskRight / (double) textureWidth * (uMax - uMin)));
        vMax = (float) (vMax - (maskTop / (double) textureHeight * (vMax - vMin)));

        BufferBuilder vertexBuffer = Tessellator.getInstance().getBuffer();
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vertexBuffer.pos(xCoord, yCoord + textureHeight, zLevel).tex(uMin, vMax).endVertex();
        vertexBuffer.pos(xCoord + textureWidth - maskRight, yCoord + textureHeight, zLevel).tex(uMax, vMax).endVertex();
        vertexBuffer.pos(xCoord + textureWidth - maskRight, yCoord + maskTop, zLevel).tex(uMax, vMin).endVertex();
        vertexBuffer.pos(xCoord, yCoord + maskTop, zLevel).tex(uMin, vMin).endVertex();
        vertexBuffer.finishDrawing();
        WorldVertexBufferUploader.draw(vertexBuffer);
    }
}
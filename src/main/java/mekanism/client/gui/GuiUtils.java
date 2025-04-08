package mekanism.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.List;
import java.util.function.Predicate;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class GuiUtils {

    private GuiUtils() {
    }

    // Note: Does not validate that the passed in dimensions are valid
    // this strategy starts with a small texture and will expand it (by scaling) to meet the size requirements. good for small widgets
    // where the background texture is a single color
    public static void renderExtendedTexture(GuiGraphics guiGraphics, ResourceLocation resource, int sideWidth, int sideHeight, int left, int top, int width, int height) {
        int textureWidth = 2 * sideWidth + 1;
        int textureHeight = 2 * sideHeight + 1;
        blitNineSlicedSized(guiGraphics, resource, left, top, width, height, sideWidth, sideHeight, textureWidth, textureHeight, 0, 0, textureWidth, textureHeight);
    }

    // this strategy starts with a large texture and will scale it down or tile it if necessary. good for larger widgets, but requires a large texture;
    // small textures will tank FPS due to tiling
    public static void renderBackgroundTexture(GuiGraphics guiGraphics, ResourceLocation resource, int texSideWidth, int texSideHeight, int left, int top, int width,
          int height, int textureWidth, int textureHeight) {
        blitNineSlicedSized(guiGraphics, resource, left, top, width, height, texSideWidth, texSideHeight, textureWidth, textureHeight, 0, 0, textureWidth, textureHeight);
    }

    public static void drawOutline(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        fill(guiGraphics, x, y, width, 1, color);
        fill(guiGraphics, x, y + height - 1, width, 1, color);
        if (height > 2) {
            fill(guiGraphics, x, y + 1, 1, height - 2, color);
            fill(guiGraphics, x + width - 1, y + 1, 1, height - 2, color);
        }
    }

    public static void fill(GuiGraphics guiGraphics, RenderType renderType, int x, int y, int width, int height, int color) {
        if (width != 0 && height != 0) {
            guiGraphics.fill(renderType, x, y, x + width, y + height, color);
        }
    }

    public static void fill(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        if (width != 0 && height != 0) {
            guiGraphics.fill(x, y, x + width, y + height, color);
        }
    }

    public static void drawBackdrop(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, int width, int alpha) {
        drawBackdrop(guiGraphics, minecraft, x, y, width, minecraft.font.lineHeight, alpha);
    }

    public static void drawBackdrop(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, int width, int height, int alpha) {
        //Slightly modified copy of Gui#drawBackdrop so that we can support it in places that can't directly call it
        int backgroundColor = minecraft.options.getBackgroundColor(0.0F);
        if (backgroundColor != 0) {
            int argb = 0xFFFFFF | alpha << 24;
            guiGraphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, FastColor.ARGB32.multiply(backgroundColor, argb));
        }
    }

    public static void drawTiledSprite(GuiGraphics guiGraphics, int xPosition, int yPosition, int yOffset, int desiredWidth, int desiredHeight, TextureAtlasSprite sprite,
          int textureWidth, int textureHeight, int zLevel, TilingDirection tilingDirection) {
        drawTiledSprite(guiGraphics, xPosition, yPosition, yOffset, desiredWidth, desiredHeight, sprite, textureWidth, textureHeight, zLevel, tilingDirection, true);
    }

    public static void drawTiledSprite(GuiGraphics guiGraphics, int xPosition, int yPosition, int yOffset, int desiredWidth, int desiredHeight, TextureAtlasSprite sprite,
          int textureWidth, int textureHeight, int zLevel, TilingDirection tilingDirection, boolean blend) {
        if (desiredWidth == 0 || desiredHeight == 0 || textureWidth == 0 || textureHeight == 0) {
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        int xTileCount = desiredWidth / textureWidth;
        int xRemainder = desiredWidth - (xTileCount * textureWidth);
        int yTileCount = desiredHeight / textureHeight;
        int yRemainder = desiredHeight - (yTileCount * textureHeight);
        int yStart = yPosition + yOffset;
        float uMin = sprite.getU0();
        float uMax = sprite.getU1();
        float vMin = sprite.getV0();
        float vMax = sprite.getV1();
        float uDif = uMax - uMin;
        float vDif = vMax - vMin;
        if (blend) {
            RenderSystem.enableBlend();
        }
        //Note: We still use the tesselator as that is what GuiGraphics#innerBlit does
        BufferBuilder vertexBuffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            int width = (xTile == xTileCount) ? xRemainder : textureWidth;
            if (width == 0) {
                break;
            }
            int x = xPosition + (xTile * textureWidth);
            int maskRight = textureWidth - width;
            int shiftedX = x + textureWidth - maskRight;
            float uLocalDif = uDif * maskRight / textureWidth;
            float uLocalMin;
            float uLocalMax;
            if (tilingDirection.right) {
                uLocalMin = uMin;
                uLocalMax = uMax - uLocalDif;
            } else {
                uLocalMin = uMin + uLocalDif;
                uLocalMax = uMax;
            }
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int height = (yTile == yTileCount) ? yRemainder : textureHeight;
                if (height == 0) {
                    //Note: We don't want to fully break out because our height will be zero if we are looking to
                    // draw the remainder, but there is no remainder as it divided evenly
                    break;
                }
                int y = yStart - ((yTile + 1) * textureHeight);
                int maskTop = textureHeight - height;
                float vLocalDif = vDif * maskTop / textureHeight;
                float vLocalMin;
                float vLocalMax;
                if (tilingDirection.down) {
                    vLocalMin = vMin;
                    vLocalMax = vMax - vLocalDif;
                } else {
                    vLocalMin = vMin + vLocalDif;
                    vLocalMax = vMax;
                }
                vertexBuffer.addVertex(matrix4f, x, y + textureHeight, zLevel)
                      .setUv(uLocalMin, vLocalMax);
                vertexBuffer.addVertex(matrix4f, shiftedX, y + textureHeight, zLevel)
                      .setUv(uLocalMax, vLocalMax);
                vertexBuffer.addVertex(matrix4f, shiftedX, y + maskTop, zLevel)
                      .setUv(uLocalMax, vLocalMin);
                vertexBuffer.addVertex(matrix4f, x, y + maskTop, zLevel)
                      .setUv(uLocalMin, vLocalMin);
            }
        }
        BufferUploader.drawWithShader(vertexBuffer.buildOrThrow());
        if (blend) {
            RenderSystem.disableBlend();
        }
    }

    // reverse-order iteration over children w/ built-in GuiElement check, runs a basic anyMatch with checker
    public static <CHILD extends GuiEventListener> boolean checkChildren(List<? extends CHILD> children, Predicate<CHILD> checker) {
        return findChild(children, checker) != null;
    }

    // reverse-order iteration over children w/ built-in GuiElement check, runs a basic anyMatch with checker
    @Nullable
    public static <CHILD extends GuiEventListener> CHILD findChild(List<? extends CHILD> children, Predicate<CHILD> checker) {
        for (int i = children.size() - 1; i >= 0; i--) {
            CHILD child = children.get(i);
            if (checker.test(child)) {
                return child;
            }
        }
        return null;
    }

    // reverse-order iteration over children w/ built-in GuiElement check, runs a basic anyMatch with checker
    public static <CHILD extends GuiEventListener> boolean checkChildren(List<? extends CHILD> children, double mouseX, double mouseY, MouseOverPredicate<CHILD> checker) {
        return findChild(children, mouseX, mouseY, checker) != null;
    }

    @Nullable
    public static <CHILD extends GuiEventListener> CHILD findChild(List<? extends CHILD> children, double mouseX, double mouseY, MouseOverPredicate<CHILD> checker) {
        for (int i = children.size() - 1; i >= 0; i--) {
            CHILD child = children.get(i);
            if (checker.test(child, mouseX, mouseY)) {
                return child;
            }
        }
        return null;
    }

    @Nullable
    public static <CHILD extends GuiEventListener> CHILD findChild(List<? extends CHILD> children, double mouseX, double mouseY, int button, MouseClickedPredicate<CHILD> checker) {
        for (int i = children.size() - 1; i >= 0; i--) {
            CHILD child = children.get(i);
            if (checker.test(child, mouseX, mouseY, button)) {
                return child;
            }
        }
        return null;
    }

    public static <CHILD extends GuiEventListener> boolean checkChildren(List<? extends CHILD> children, int keyCode, int scanCode, int modifiers, KeyPressedPredicate<CHILD> checker) {
        for (int i = children.size() - 1; i >= 0; i--) {
            CHILD child = children.get(i);
            if (checker.test(child, keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    public static <CHILD extends GuiEventListener> boolean checkChildrenChar(List<? extends CHILD> children, char c, int keyCode, CharTypedPredicate<CHILD> checker) {
        for (int i = children.size() - 1; i >= 0; i--) {
            CHILD child = children.get(i);
            if (checker.test(child, c, keyCode)) {
                return true;
            }
        }
        return false;
    }

    public interface MouseOverPredicate<ELEMENT> {

        boolean test(ELEMENT element, double mouseX, double mouseY);
    }

    public interface MouseClickedPredicate<ELEMENT> {

        boolean test(ELEMENT element, double mouseX, double mouseY, int button);
    }

    public interface KeyPressedPredicate<ELEMENT> {

        boolean test(ELEMENT element, int keyCode, int scanCode, int modifiers);
    }

    public interface CharTypedPredicate<ELEMENT> {

        boolean test(ELEMENT element, char c, int keyCode);
    }

    public static int drawStringNoFlush(GuiGraphics graphics, Font font, Component component, float x, float y, int color, boolean drawShadow) {
        return drawStringNoFlush(graphics, graphics.pose().last().pose(), font, component, x, y, color, drawShadow);
    }

    public static int drawStringNoFlush(GuiGraphics graphics, Matrix4f matrix, Font font, Component component, float x, float y, int color, boolean drawShadow) {
        //Copy of GuiGraphics#drawString(Font, FormattedCharSequence, float, float, int, boolean) but without the flush at the end
        return font.drawInBatch(component.getVisualOrderText(), x, y, color, drawShadow, matrix, graphics.bufferSource(),
              Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
    }

    public static void renderItem(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis, float scale, Font font, @Nullable String text, boolean overlay) {
        if (!stack.isEmpty()) {
            try {
                PoseStack pose = guiGraphics.pose();
                pose.pushPose();
                if (scale != 1) {
                    //Translate before scaling, and then set xAxis and yAxis to zero so that we don't translate a second time
                    pose.translate(xAxis, yAxis, 0);
                    pose.scale(scale, scale, scale);
                    xAxis = 0;
                    yAxis = 0;
                }
                guiGraphics.renderItem(stack, xAxis, yAxis);
                if (overlay) {
                    //When we render items ourselves in virtual slots or scroll slots we want to compress the z scale
                    // for rendering the stored count so that it doesn't clip with later windows
                    pose.translate(0, 0, -25);
                    guiGraphics.renderItemDecorations(font, stack, xAxis, yAxis, text);
                }

                pose.popPose();
            } catch (Exception e) {
                Mekanism.logger.error("Failed to render stack into gui: {}", stack, e);
            }
        }
    }

    public static void renderBorder(GuiGraphics guiGraphics, int x, int y, int boxWidth, int boxHeight, int color) {
        guiGraphics.hLine(x, x + boxWidth, y, color);
        guiGraphics.hLine(x, x + boxWidth, y + boxHeight, color);
        guiGraphics.vLine(x, y, y + boxHeight, color);
        guiGraphics.vLine(x + boxWidth, y, y + boxHeight, color);
    }

    /**
     * Represents which direction our tiling is done when extending past the max size.
     */
    public enum TilingDirection {
        /**
         * Textures are being tiled/filled from top left to bottom right.
         */
        DOWN_RIGHT(true, true),
        /**
         * Textures are being tiled/filled from top right to bottom left.
         */
        DOWN_LEFT(true, false),
        /**
         * Textures are being tiled/filled from bottom left to top right.
         */
        UP_RIGHT(false, true),
        /**
         * Textures are being tiled/filled from bottom right to top left.
         */
        UP_LEFT(false, false);

        private final boolean down;
        private final boolean right;

        TilingDirection(boolean down, boolean right) {
            this.down = down;
            this.right = right;
        }
    }

    // like guiGraphics.blitNineSlicedSized but uses one BufferBuilder
    public static void blitNineSlicedSized(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height, int sliceWidth, int sliceHeight, int uWidth, int vHeight, int uOffset, int vOffset, int textureWidth, int textureHeight) {
        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        profiler.push("blit setup");
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        profiler.pop();

        BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        profiler.push("blitting");

        int cornerWidth = sliceWidth;
        int cornerHeight = sliceHeight;
        int edgeWidth = sliceWidth;
        int edgeHeight = sliceHeight;
        cornerWidth = Math.min(cornerWidth, width / 2);
        edgeWidth = Math.min(edgeWidth, width / 2);
        cornerHeight = Math.min(cornerHeight, height / 2);
        edgeHeight = Math.min(edgeHeight, height / 2);
        if (width == uWidth && height == vHeight) {
            blit(buffer, matrix4f, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight);
        } else if (height == vHeight) {
            blit(buffer, matrix4f, x, y, uOffset, vOffset, cornerWidth, height, textureWidth, textureHeight);
            blitRepeating(buffer, matrix4f, x + cornerWidth, y, width - edgeWidth - cornerWidth, height, uOffset + cornerWidth, vOffset, uWidth - edgeWidth - cornerWidth, vHeight, textureWidth, textureHeight);
            blit(buffer, matrix4f, x + width - edgeWidth, y, uOffset + uWidth - edgeWidth, vOffset, edgeWidth, height, textureWidth, textureHeight);
        } else if (width == uWidth) {
            blit(buffer, matrix4f, x, y, uOffset, vOffset, width, cornerHeight, textureWidth, textureHeight);
            blitRepeating(buffer, matrix4f, x, y + cornerHeight, width, height - edgeHeight - cornerHeight, uOffset, vOffset + cornerHeight, uWidth, vHeight - edgeHeight - cornerHeight, textureWidth, textureHeight);
            blit(buffer, matrix4f, x, y + height - edgeHeight, uOffset, vOffset + vHeight - edgeHeight, width, edgeHeight, textureWidth, textureHeight);
        } else {
            blit(buffer, matrix4f, x, y, uOffset, vOffset, cornerWidth, cornerHeight, textureWidth, textureHeight);
            blitRepeating(buffer, matrix4f, x + cornerWidth, y, width - edgeWidth - cornerWidth, cornerHeight, uOffset + cornerWidth, vOffset, uWidth - edgeWidth - cornerWidth, cornerHeight, textureWidth, textureHeight);
            blit(buffer, matrix4f, x + width - edgeWidth, y, uOffset + uWidth - edgeWidth, vOffset, edgeWidth, cornerHeight, textureWidth, textureHeight);
            blit(buffer, matrix4f, x, y + height - edgeHeight, uOffset, vOffset + vHeight - edgeHeight, cornerWidth, edgeHeight, textureWidth, textureHeight);
            blitRepeating(buffer, matrix4f, x + cornerWidth, y + height - edgeHeight, width - edgeWidth - cornerWidth, edgeHeight, uOffset + cornerWidth, vOffset + vHeight - edgeHeight, uWidth - edgeWidth - cornerWidth, edgeHeight, textureWidth, textureHeight);
            blit(buffer, matrix4f, x + width - edgeWidth, y + height - edgeHeight, uOffset + uWidth - edgeWidth, vOffset + vHeight - edgeHeight, edgeWidth, edgeHeight, textureWidth, textureHeight);
            blitRepeating(buffer, matrix4f, x, y + cornerHeight, cornerWidth, height - edgeHeight - cornerHeight, uOffset, vOffset + cornerHeight, cornerWidth, vHeight - edgeHeight - cornerHeight, textureWidth, textureHeight);
            blitRepeating(buffer, matrix4f, x + cornerWidth, y + cornerHeight, width - edgeWidth - cornerWidth, height - edgeHeight - cornerHeight, uOffset + cornerWidth, vOffset + cornerHeight, uWidth - edgeWidth - cornerWidth, vHeight - edgeHeight - cornerHeight, textureWidth, textureHeight);
            blitRepeating(buffer, matrix4f, x + width - edgeWidth, y + cornerHeight, cornerWidth, height - edgeHeight - cornerHeight, uOffset + uWidth - edgeWidth, vOffset + cornerHeight, edgeWidth, vHeight - edgeHeight - cornerHeight, textureWidth, textureHeight);
        }
        profiler.pop();

        profiler.push("drawing");
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        profiler.pop();
    }

    public static void blitNineSlicedSized(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height, int sliceSize, int uWidth, int vHeight, int uOffset, int vOffset, int textureWidth, int textureHeight) {
        blitNineSlicedSized(guiGraphics, texture, x, y, width, height, sliceSize, sliceSize, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    private static void blit(BufferBuilder bufferbuilder, Matrix4f matrix4f, int pX, int pY, float pUOffset, float pVOffset, int pWidth, int pHeight, int pTextureWidth, int pTextureHeight) {
        bufferbuilder.addVertex(matrix4f, pX, pY, 0)
              .setUv(pUOffset / pTextureWidth, pVOffset / pTextureHeight);
        bufferbuilder.addVertex(matrix4f, pX, pY + pHeight, 0)
              .setUv(pUOffset / pTextureWidth, (pVOffset + pHeight) / pTextureHeight);
        bufferbuilder.addVertex(matrix4f, pX + pWidth, pY + pHeight, 0)
              .setUv((pUOffset + pWidth) / pTextureWidth, (pVOffset + pHeight) / pTextureHeight);
        bufferbuilder.addVertex(matrix4f, pX + pWidth, pY, 0)
              .setUv((pUOffset + pWidth) / pTextureWidth, pVOffset / pTextureHeight);
    }

    private static void blitRepeating(BufferBuilder bufferbuilder, Matrix4f matrix4f, int pX, int pY, int pWidth, int pHeight, int pUOffset, int pVOffset, int pSourceWidth, int pSourceHeight, int textureWidth, int textureHeight) {
        int i = pX;

        int j;
        for (IntIterator intiterator = slices(pWidth, pSourceWidth); intiterator.hasNext(); i += j) {
            j = intiterator.nextInt();
            int k = (pSourceWidth - j) / 2;
            int l = pY;

            int i1;
            for (IntIterator intiterator1 = slices(pHeight, pSourceHeight); intiterator1.hasNext(); l += i1) {
                i1 = intiterator1.nextInt();
                int j1 = (pSourceHeight - i1) / 2;
                blit(bufferbuilder, matrix4f, i, l, pUOffset + k, pVOffset + j1, j, i1, textureWidth, textureHeight);
            }
        }
    }

    /**
     * Returns an iterator for dividing a value into slices of a specified size.
     *
     * @param pTarget the value to be divided.
     * @param pTotal  the size of each slice.
     *
     * @return An iterator for iterating over the slices.
     */
    private static IntIterator slices(int pTarget, int pTotal) {
        int i = Mth.positiveCeilDiv(pTarget, pTotal);
        return new Divisor(pTarget, i);
    }
}
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
import mekanism.client.gui.element.GuiElement;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
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
        blitNineSliced(guiGraphics, resource, left, top, width, height, sideWidth, sideHeight, textureWidth, textureHeight, 0, 0, textureWidth, textureHeight);
    }

    // this strategy starts with a large texture and will scale it down or tile it if necessary. good for larger widgets, but requires a large texture;
    // small textures will tank FPS due to tiling
    public static void renderBackgroundTexture(GuiGraphics guiGraphics, ResourceLocation resource, int texSideWidth, int texSideHeight, int left, int top, int width,
          int height, int textureWidth, int textureHeight) {
        blitNineSliced(guiGraphics, resource, left, top, width, height, texSideWidth, texSideHeight, textureWidth, textureHeight, 0, 0, textureWidth,
              textureHeight);
    }

    //TODO - 1.20: Replace the below nineSlicedMethods with using https://github.com/MinecraftForge/MinecraftForge/pull/9641
    //Copy of GuiGraphics#blitNineSliced but modified to support specifying texture size
    public static void blitNineSliced(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height, int sliceSize, int uWidth,
          int vHeight, int uOffset, int vOffset, int textureWidth, int textureHeight) {
        blitNineSliced(guiGraphics, texture, x, y, width, height, sliceSize, sliceSize, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    //Copy of GuiGraphics#blitNineSliced but modified to support specifying texture size
    public static void blitNineSliced(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height, int sliceWidth, int sliceHeight,
          int uWidth, int vHeight, int uOffset, int vOffset, int textureWidth, int textureHeight) {
        blitNineSliced(guiGraphics, texture, x, y, width, height, sliceWidth, sliceHeight, sliceWidth, sliceHeight, uWidth, vHeight, uOffset, vOffset,
              textureWidth, textureHeight);
    }

    //Copy of GuiGraphics#blitNineSliced but modified to support specifying texture size
    public static void blitNineSliced(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height, int cornerWidth, int cornerHeight,
          int edgeWidth, int edgeHeight, int uWidth, int vHeight, int uOffset, int vOffset, int textureWidth, int textureHeight) {
        cornerWidth = Math.min(cornerWidth, width / 2);
        edgeWidth = Math.min(edgeWidth, width / 2);
        cornerHeight = Math.min(cornerHeight, height / 2);
        edgeHeight = Math.min(edgeHeight, height / 2);
        if (width == uWidth && height == vHeight) {
            guiGraphics.blit(texture, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight);
        } else if (height == vHeight) {
            guiGraphics.blit(texture, x, y, uOffset, vOffset, cornerWidth, height, textureWidth, textureHeight);
            blitRepeating(guiGraphics, texture, x + cornerWidth, y, width - edgeWidth - cornerWidth, height, uOffset + cornerWidth, vOffset, uWidth - edgeWidth - cornerWidth, vHeight, textureWidth, textureHeight);
            guiGraphics.blit(texture, x + width - edgeWidth, y, uOffset + uWidth - edgeWidth, vOffset, edgeWidth, height, textureWidth, textureHeight);
        } else if (width == uWidth) {
            guiGraphics.blit(texture, x, y, uOffset, vOffset, width, cornerHeight, textureWidth, textureHeight);
            blitRepeating(guiGraphics, texture, x, y + cornerHeight, width, height - edgeHeight - cornerHeight, uOffset, vOffset + cornerHeight, uWidth, vHeight - edgeHeight - cornerHeight, textureWidth, textureHeight);
            guiGraphics.blit(texture, x, y + height - edgeHeight, uOffset, vOffset + vHeight - edgeHeight, width, edgeHeight, textureWidth, textureHeight);
        } else {
            guiGraphics.blit(texture, x, y, uOffset, vOffset, cornerWidth, cornerHeight, textureWidth, textureHeight);
            blitRepeating(guiGraphics, texture, x + cornerWidth, y, width - edgeWidth - cornerWidth, cornerHeight, uOffset + cornerWidth, vOffset, uWidth - edgeWidth - cornerWidth, cornerHeight, textureWidth, textureHeight);
            guiGraphics.blit(texture, x + width - edgeWidth, y, uOffset + uWidth - edgeWidth, vOffset, edgeWidth, cornerHeight, textureWidth, textureHeight);
            guiGraphics.blit(texture, x, y + height - edgeHeight, uOffset, vOffset + vHeight - edgeHeight, cornerWidth, edgeHeight, textureWidth, textureHeight);
            blitRepeating(guiGraphics, texture, x + cornerWidth, y + height - edgeHeight, width - edgeWidth - cornerWidth, edgeHeight, uOffset + cornerWidth, vOffset + vHeight - edgeHeight, uWidth - edgeWidth - cornerWidth, edgeHeight, textureWidth, textureHeight);
            guiGraphics.blit(texture, x + width - edgeWidth, y + height - edgeHeight, uOffset + uWidth - edgeWidth, vOffset + vHeight - edgeHeight, edgeWidth, edgeHeight, textureWidth, textureHeight);
            blitRepeating(guiGraphics, texture, x, y + cornerHeight, cornerWidth, height - edgeHeight - cornerHeight, uOffset, vOffset + cornerHeight, cornerWidth, vHeight - edgeHeight - cornerHeight, textureWidth, textureHeight);
            blitRepeating(guiGraphics, texture, x + cornerWidth, y + cornerHeight, width - edgeWidth - cornerWidth, height - edgeHeight - cornerHeight, uOffset + cornerWidth, vOffset + cornerHeight, uWidth - edgeWidth - cornerWidth, vHeight - edgeHeight - cornerHeight, textureWidth, textureHeight);
            blitRepeating(guiGraphics, texture, x + width - edgeWidth, y + cornerHeight, cornerWidth, height - edgeHeight - cornerHeight, uOffset + uWidth - edgeWidth, vOffset + cornerHeight, edgeWidth, vHeight - edgeHeight - cornerHeight, textureWidth, textureHeight);
        }
    }

    //Copy of GuiGraphics#blitRepeating but modified to support specifying texture size
    public static void blitRepeating(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height, int uOffset, int yOffset, int sourceWidth,
          int sourceHeight, int textureWidth, int textureHeight) {
        int xStart = x;
        int uWidth;
        for (IntIterator xSlices = slices(width, sourceWidth); xSlices.hasNext(); xStart += uWidth) {
            uWidth = xSlices.nextInt();
            int uShift = (sourceWidth - uWidth) / 2;
            int yStart = y;
            int vHeight;
            for (IntIterator ySlices = slices(height, sourceHeight); ySlices.hasNext(); yStart += vHeight) {
                vHeight = ySlices.nextInt();
                int vShift = (sourceHeight - vHeight) / 2;
                guiGraphics.blit(texture, xStart, yStart, uOffset + uShift, yOffset + vShift, uWidth, vHeight, textureWidth, textureHeight);
            }
        }
    }

    //Copy of GuiGraphics#slices
    private static IntIterator slices(int target, int total) {
        int denominator = Mth.positiveCeilDiv(target, total);
        return new Divisor(target, denominator);
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
        drawBackdrop(guiGraphics, minecraft, x, y, width, 9, alpha);
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
        BufferBuilder vertexBuffer = Tesselator.getInstance().getBuilder();
        vertexBuffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
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
                vertexBuffer.vertex(matrix4f, x, y + textureHeight, zLevel).uv(uLocalMin, vLocalMax).endVertex();
                vertexBuffer.vertex(matrix4f, shiftedX, y + textureHeight, zLevel).uv(uLocalMax, vLocalMax).endVertex();
                vertexBuffer.vertex(matrix4f, shiftedX, y + maskTop, zLevel).uv(uLocalMax, vLocalMin).endVertex();
                vertexBuffer.vertex(matrix4f, x, y + maskTop, zLevel).uv(uLocalMin, vLocalMin).endVertex();
            }
        }
        BufferUploader.drawWithShader(vertexBuffer.end());
        if (blend) {
            RenderSystem.disableBlend();
        }
    }

    // reverse-order iteration over children w/ built-in GuiElement check, runs a basic anyMatch with checker
    public static boolean checkChildren(List<? extends GuiEventListener> children, Predicate<GuiElement> checker) {
        for (int i = children.size() - 1; i >= 0; i--) {
            Object obj = children.get(i);
            if (obj instanceof GuiElement element && checker.test(element)) {
                return true;
            }
        }
        return false;
    }

    public static int drawString(GuiGraphics guiGraphics, Font font, Component component, float x, float y, int color, boolean drawShadow) {
        return guiGraphics.drawString(font, component.getVisualOrderText(), x, y, color, drawShadow);
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
}
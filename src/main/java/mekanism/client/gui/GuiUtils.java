package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;

public class GuiUtils {

    private GuiUtils() {
    }

    // Note: Does not validate that the passed in dimensions are valid
    // this strategy starts with a small texture and will expand it (by scaling) to meet the size requirements. good for small widgets
    // where the background texture is a single color
    public static void renderExtendedTexture(MatrixStack matrix, ResourceLocation resource, int sideWidth, int sideHeight, int left, int top, int width, int height) {
        int textureWidth = 2 * sideWidth + 1;
        int textureHeight = 2 * sideHeight + 1;
        int centerWidth = width - 2 * sideWidth;
        int centerHeight = height - 2 * sideHeight;
        int leftEdgeEnd = left + sideWidth;
        int rightEdgeStart = leftEdgeEnd + centerWidth;
        int topEdgeEnd = top + sideHeight;
        int bottomEdgeStart = topEdgeEnd + centerHeight;
        MekanismRenderer.bindTexture(resource);
        //Left Side
        //Top Left Corner
        AbstractGui.blit(matrix, left, top, 0, 0, sideWidth, sideHeight, textureWidth, textureHeight);
        //Left Middle
        if (centerHeight > 0) {
            AbstractGui.blit(matrix, left, topEdgeEnd, sideWidth, centerHeight, 0, sideHeight, sideWidth, 1, textureWidth, textureHeight);
        }
        //Bottom Left Corner
        AbstractGui.blit(matrix, left, bottomEdgeStart, 0, sideHeight + 1, sideWidth, sideHeight, textureWidth, textureHeight);

        //Middle
        if (centerWidth > 0) {
            //Top Middle
            AbstractGui.blit(matrix, leftEdgeEnd, top, centerWidth, sideHeight, sideWidth, 0, 1, sideHeight, textureWidth, textureHeight);
            if (centerHeight > 0) {
                //Center
                AbstractGui.blit(matrix, leftEdgeEnd, topEdgeEnd, centerWidth, centerHeight, sideWidth, sideHeight, 1, 1, textureWidth, textureHeight);
            }
            //Bottom Middle
            AbstractGui.blit(matrix, leftEdgeEnd, bottomEdgeStart, centerWidth, sideHeight, sideWidth, sideHeight + 1, 1, sideHeight, textureWidth, textureHeight);
        }

        //Right side
        //Top Right Corner
        AbstractGui.blit(matrix, rightEdgeStart, top, sideWidth + 1, 0, sideWidth, sideHeight, textureWidth, textureHeight);
        //Right Middle
        if (centerHeight > 0) {
            AbstractGui.blit(matrix, rightEdgeStart, topEdgeEnd, sideWidth, centerHeight, sideWidth + 1, sideHeight, sideWidth, 1, textureWidth, textureHeight);
        }
        //Bottom Right Corner
        AbstractGui.blit(matrix, rightEdgeStart, bottomEdgeStart, sideWidth + 1, sideHeight + 1, sideWidth, sideHeight, textureWidth, textureHeight);
    }

    // this strategy starts with a large texture and will scale it down or tile it if necessary. good for larger widgets, but requires a large texture; small textures will tank FPS due
    // to tiling
    public static void renderBackgroundTexture(MatrixStack matrix, ResourceLocation resource, int texSideWidth, int texSideHeight, int left, int top, int width, int height, int textureWidth, int textureHeight) {
        // render as much side as we can, based on element dimensions
        int sideWidth = Math.min(texSideWidth, width / 2);
        int sideHeight = Math.min(texSideHeight, height / 2);

        // Adjustment for small odd-height and odd-width GUIs
        int leftWidth = sideWidth < texSideWidth ? sideWidth + (width % 2) : sideWidth;
        int topHeight = sideHeight < texSideHeight ? sideHeight + (height % 2) : sideHeight;

        int texCenterWidth = textureWidth - texSideWidth * 2, texCenterHeight = textureHeight - texSideHeight * 2;
        int centerWidth = width - leftWidth - sideWidth, centerHeight = height - topHeight - sideHeight;

        int leftEdgeEnd = left + leftWidth;
        int rightEdgeStart = leftEdgeEnd + centerWidth;
        int topEdgeEnd = top + topHeight;
        int bottomEdgeStart = topEdgeEnd + centerHeight;
        MekanismRenderer.bindTexture(resource);

        //Top Left Corner
        AbstractGui.blit(matrix, left, top, 0, 0, leftWidth, topHeight, textureWidth, textureHeight);
        //Bottom Left Corner
        AbstractGui.blit(matrix, left, bottomEdgeStart, 0, textureHeight - sideHeight, leftWidth, sideHeight, textureWidth, textureHeight);

        //Middle
        if (centerWidth > 0) {
            //Top Middle
            blitTiled(matrix, leftEdgeEnd, top, centerWidth, topHeight, texSideWidth, 0, texCenterWidth, texSideHeight, textureWidth, textureHeight);
            if (centerHeight > 0) {
                //Center
                blitTiled(matrix, leftEdgeEnd, topEdgeEnd, centerWidth, centerHeight, texSideWidth, texSideHeight, texCenterWidth, texCenterHeight, textureWidth, textureHeight);
            }
            //Bottom Middle
            blitTiled(matrix, leftEdgeEnd, bottomEdgeStart, centerWidth, sideHeight, texSideWidth, textureHeight - sideHeight, texCenterWidth, texSideHeight, textureWidth, textureHeight);
        }

        if (centerHeight > 0) {
            //Left Middle
            blitTiled(matrix, left, topEdgeEnd, leftWidth, centerHeight, 0, texSideHeight, texSideWidth, texCenterHeight, textureWidth, textureHeight);
            //Right Middle
            blitTiled(matrix, rightEdgeStart, topEdgeEnd, sideWidth, centerHeight, textureWidth - sideWidth, texSideHeight, texSideWidth, texCenterHeight, textureWidth, textureHeight);
        }

        //Top Right Corner
        AbstractGui.blit(matrix, rightEdgeStart, top, textureWidth - sideWidth, 0, sideWidth, topHeight, textureWidth, textureHeight);
        //Bottom Right Corner
        AbstractGui.blit(matrix, rightEdgeStart, bottomEdgeStart, textureWidth - sideWidth, textureHeight - sideHeight, sideWidth, sideHeight, textureWidth, textureHeight);
    }

    public static void blitTiled(MatrixStack matrix, int x, int y, int width, int height, int texX, int texY, int texDrawWidth, int texDrawHeight, int textureWidth, int textureHeight) {
        int xTiles = (int) Math.ceil((float) width / texDrawWidth), yTiles = (int) Math.ceil((float) height / texDrawHeight);

        int drawWidth = width, drawHeight = height;
        for (int tileX = 0; tileX < xTiles; tileX++) {
            for (int tileY = 0; tileY < yTiles; tileY++) {
                AbstractGui.blit(matrix, x + texDrawWidth * tileX, y + texDrawHeight * tileY, texX, texY, Math.min(drawWidth, texDrawWidth), Math.min(drawHeight, texDrawHeight), textureWidth, textureHeight);
                drawHeight -= texDrawHeight;
            }
            drawWidth -= texDrawWidth;
            drawHeight = height;
        }
    }

    public static void drawOutline(MatrixStack matrix, int x, int y, int width, int height, int color) {
        fill(matrix, x, y, width, 1, color);
        fill(matrix, x, y + height - 1, width, 1, color);
        if (height > 2) {
            fill(matrix, x, y + 1, 1, height - 2, color);
            fill(matrix, x + width - 1, y + 1, 1, height - 2, color);
        }
    }

    public static void fill(MatrixStack matrix, int x, int y, int width, int height, int color) {
        AbstractGui.fill(matrix, x, y, x + width, y + height, color);
    }

    public static void drawSprite(MatrixStack matrix, int x, int y, int width, int height, int zLevel, TextureAtlasSprite sprite) {
        MekanismRenderer.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        BufferBuilder vertexBuffer = Tessellator.getInstance().getBuffer();
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        Matrix4f matrix4f = matrix.getLast().getMatrix();
        vertexBuffer.pos(matrix4f, x, y + height, zLevel).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
        vertexBuffer.pos(matrix4f, x + width, y + height, zLevel).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
        vertexBuffer.pos(matrix4f, x + width, y, zLevel).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
        vertexBuffer.pos(matrix4f, x, y, zLevel).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
        vertexBuffer.finishDrawing();
        WorldVertexBufferUploader.draw(vertexBuffer);
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
    }

    public static void drawTiledSprite(MatrixStack matrix, int xPosition, int yPosition, int yOffset, int desiredWidth, int desiredHeight, TextureAtlasSprite sprite, int textureWidth,
          int textureHeight, int zLevel) {
        if (desiredWidth == 0 || desiredHeight == 0 || textureWidth == 0 || textureHeight == 0) {
            return;
        }
        MekanismRenderer.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        int xTileCount = desiredWidth / textureWidth;
        int xRemainder = desiredWidth - (xTileCount * textureWidth);
        int yTileCount = desiredHeight / textureHeight;
        int yRemainder = desiredHeight - (yTileCount * textureHeight);
        int yStart = yPosition + yOffset;
        float uMin = sprite.getMinU();
        float uMax = sprite.getMaxU();
        float vMin = sprite.getMinV();
        float vMax = sprite.getMaxV();
        float uDif = uMax - uMin;
        float vDif = vMax - vMin;
        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        BufferBuilder vertexBuffer = Tessellator.getInstance().getBuffer();
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        Matrix4f matrix4f = matrix.getLast().getMatrix();
        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            int width = (xTile == xTileCount) ? xRemainder : textureWidth;
            if (width == 0) {
                break;
            }
            int x = xPosition + (xTile * textureWidth);
            int maskRight = textureWidth - width;
            int shiftedX = x + textureWidth - maskRight;
            float uMaxLocal = uMax - (uDif * maskRight / textureWidth);
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int height = (yTile == yTileCount) ? yRemainder : textureHeight;
                if (height == 0) {
                    //Note: We don't want to fully break out because our height will be zero if we are looking to
                    // draw the remainder, but there is no remainder as it divided evenly
                    break;
                }
                int y = yStart - ((yTile + 1) * textureHeight);
                int maskTop = textureHeight - height;
                float vMaxLocal = vMax - (vDif * maskTop / textureHeight);
                vertexBuffer.pos(matrix4f, x, y + textureHeight, zLevel).tex(uMin, vMaxLocal).endVertex();
                vertexBuffer.pos(matrix4f, shiftedX, y + textureHeight, zLevel).tex(uMaxLocal, vMaxLocal).endVertex();
                vertexBuffer.pos(matrix4f, shiftedX, y + maskTop, zLevel).tex(uMaxLocal, vMin).endVertex();
                vertexBuffer.pos(matrix4f, x, y + maskTop, zLevel).tex(uMin, vMin).endVertex();
            }
        }
        vertexBuffer.finishDrawing();
        WorldVertexBufferUploader.draw(vertexBuffer);
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
    }

    // reverse-order iteration over children w/ built-in GuiElement check, runs a basic anyMatch with checker
    public static boolean checkChildren(List<? extends Widget> children, Predicate<GuiElement> checker) {
        for (int i = children.size() - 1; i >= 0; i--) {
            Object obj = children.get(i);
            if (obj instanceof GuiElement && checker.test((GuiElement) obj)) {
                return true;
            }
        }
        return false;
    }

    public static void renderItem(MatrixStack matrix, ItemRenderer renderer, @Nonnull ItemStack stack, int xAxis, int yAxis, float scale, FontRenderer font,
          @Nullable String text, boolean overlay) {
        if (!stack.isEmpty()) {
            try {
                matrix.push();
                RenderSystem.enableDepthTest();
                RenderHelper.enableStandardItemLighting();
                if (scale != 1) {
                    matrix.scale(scale, scale, scale);
                }
                //Apply our matrix stack to the render system and pass an unmodified one to the render methods
                // Vanilla still renders the items using render system transformations so this is required to
                // have things render in the correct order
                RenderSystem.pushMatrix();
                RenderSystem.multMatrix(matrix.getLast().getMatrix());
                renderer.renderItemAndEffectIntoGUI(stack, xAxis, yAxis);
                if (overlay) {
                    renderer.renderItemOverlayIntoGUI(font, stack, xAxis, yAxis, text);
                }
                RenderSystem.popMatrix();
                RenderHelper.disableStandardItemLighting();
                RenderSystem.disableDepthTest();
                matrix.pop();
            } catch (Exception e) {
                Mekanism.logger.error("Failed to render stack into gui: {}", stack, e);
            }
        }
    }
}
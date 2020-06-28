package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;

public class GuiUtils {

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
        AbstractGui.func_238463_a_(matrix, left, top, 0, 0, sideWidth, sideHeight, textureWidth, textureHeight);
        //Left Middle
        if (centerHeight > 0) {
            AbstractGui.func_238466_a_(matrix, left, topEdgeEnd, sideWidth, centerHeight, 0, sideHeight, sideWidth, 1, textureWidth, textureHeight);
        }
        //Bottom Left Corner
        AbstractGui.func_238463_a_(matrix, left, bottomEdgeStart, 0, sideHeight + 1, sideWidth, sideHeight, textureWidth, textureHeight);

        //Middle
        if (centerWidth > 0) {
            //Top Middle
            AbstractGui.func_238466_a_(matrix, leftEdgeEnd, top, centerWidth, sideHeight, sideWidth, 0, 1, sideHeight, textureWidth, textureHeight);
            if (centerHeight > 0) {
                //Center
                AbstractGui.func_238466_a_(matrix, leftEdgeEnd, topEdgeEnd, centerWidth, centerHeight, sideWidth, sideHeight, 1, 1, textureWidth, textureHeight);
            }
            //Bottom Middle
            AbstractGui.func_238466_a_(matrix, leftEdgeEnd, bottomEdgeStart, centerWidth, sideHeight, sideWidth, sideHeight + 1, 1, sideHeight, textureWidth, textureHeight);
        }

        //Right side
        //Top Right Corner
        AbstractGui.func_238463_a_(matrix, rightEdgeStart, top, sideWidth + 1, 0, sideWidth, sideHeight, textureWidth, textureHeight);
        //Right Middle
        if (centerHeight > 0) {
            AbstractGui.func_238466_a_(matrix, rightEdgeStart, topEdgeEnd, sideWidth, centerHeight, sideWidth + 1, sideHeight, sideWidth, 1, textureWidth, textureHeight);
        }
        //Bottom Right Corner
        AbstractGui.func_238463_a_(matrix, rightEdgeStart, bottomEdgeStart, sideWidth + 1, sideHeight + 1, sideWidth, sideHeight, textureWidth, textureHeight);
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
        AbstractGui.func_238463_a_(matrix, left, top, 0, 0, leftWidth, topHeight, textureWidth, textureHeight);
        //Bottom Left Corner
        AbstractGui.func_238463_a_(matrix, left, bottomEdgeStart, 0, textureHeight - sideHeight, leftWidth, sideHeight, textureWidth, textureHeight);

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
        AbstractGui.func_238463_a_(matrix, rightEdgeStart, top, textureWidth - sideWidth, 0, sideWidth, topHeight, textureWidth, textureHeight);
        //Bottom Right Corner
        AbstractGui.func_238463_a_(matrix, rightEdgeStart, bottomEdgeStart, textureWidth - sideWidth, textureHeight - sideHeight, sideWidth, sideHeight, textureWidth, textureHeight);
    }

    public static void blitTiled(MatrixStack matrix, int x, int y, int width, int height, int texX, int texY, int texDrawWidth, int texDrawHeight, int textureWidth, int textureHeight) {
        int xTiles = (int) Math.ceil((float) width / texDrawWidth), yTiles = (int) Math.ceil((float) height / texDrawHeight);

        int drawWidth = width, drawHeight = height;
        for (int tileX = 0; tileX < xTiles; tileX++) {
            for (int tileY = 0; tileY < yTiles; tileY++) {
                AbstractGui.func_238463_a_(matrix, x + texDrawWidth * tileX, y + texDrawHeight * tileY, texX, texY, Math.min(drawWidth, texDrawWidth), Math.min(drawHeight, texDrawHeight), textureWidth, textureHeight);
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
        AbstractGui.func_238467_a_(matrix, x, y, x + width, y + height, Color.packOpaque(color));
    }

    public static void drawSprite(int x, int y, int width, int height, int zLevel, TextureAtlasSprite sprite) {
        MekanismRenderer.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        BufferBuilder vertexBuffer = Tessellator.getInstance().getBuffer();
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vertexBuffer.pos(x, y + height, zLevel).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
        vertexBuffer.pos(x + width, y + height, zLevel).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
        vertexBuffer.pos(x + width, y, zLevel).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
        vertexBuffer.pos(x, y, zLevel).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
        vertexBuffer.finishDrawing();
        WorldVertexBufferUploader.draw(vertexBuffer);
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
    }

    public static void drawTiledSprite(int xPosition, int yPosition, int yOffset, int desiredWidth, int desiredHeight, TextureAtlasSprite sprite, int textureWidth,
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
                vertexBuffer.pos(x, y + textureHeight, zLevel).tex(uMin, vMaxLocal).endVertex();
                vertexBuffer.pos(shiftedX, y + textureHeight, zLevel).tex(uMaxLocal, vMaxLocal).endVertex();
                vertexBuffer.pos(shiftedX, y + maskTop, zLevel).tex(uMaxLocal, vMin).endVertex();
                vertexBuffer.pos(x, y + maskTop, zLevel).tex(uMin, vMin).endVertex();
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

    //ItemRenderer Copies for use with matrix stacks
    public static void renderItemAndEffectIntoGUI(ItemRenderer itemRenderer, MatrixStack matrix, ItemStack stack, int xPosition, int yPosition) {
        //Copy of ItemRenderer#renderItemAndEffectIntoGUI and func_239387_b_ with added support for a matrix stack
        if (!stack.isEmpty()) {
            itemRenderer.zLevel += 50.0F;
            try {
                renderItemModelIntoGUI(itemRenderer, matrix, stack, xPosition, yPosition,
                      itemRenderer.getItemModelWithOverrides(stack, null, Minecraft.getInstance().player));
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being rendered");
                crashreportcategory.addDetail("Item Type", () -> String.valueOf(stack.getItem()));
                crashreportcategory.addDetail("Registry Name", () -> String.valueOf(stack.getItem().getRegistryName()));
                crashreportcategory.addDetail("Item Damage", () -> String.valueOf(stack.getDamage()));
                crashreportcategory.addDetail("Item NBT", () -> String.valueOf(stack.getTag()));
                crashreportcategory.addDetail("Item Foil", () -> String.valueOf(stack.hasEffect()));
                throw new ReportedException(crashreport);
            }
            itemRenderer.zLevel -= 50.0F;
        }
    }

    private static void renderItemModelIntoGUI(ItemRenderer itemRenderer, MatrixStack matrix, ItemStack stack, int x, int y, IBakedModel model) {
        //Copy of ItemRenderer#renderItemModelIntoGUI with added support for a matrix stack instead of using render system
        matrix.push();
        Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        Minecraft.getInstance().getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmapDirect(false, false);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        matrix.translate(x, y, 100.0F + itemRenderer.zLevel);
        matrix.translate(8, 8, 0);
        matrix.scale(1, -1, 1);
        matrix.scale(16, 16, 16);
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        boolean flag = !model.func_230044_c_();
        if (flag) {
            RenderHelper.setupGuiFlatDiffuseLighting();
        }
        itemRenderer.renderItem(stack, TransformType.GUI, false, matrix, buffer, MekanismRenderer.FULL_LIGHT, OverlayTexture.NO_OVERLAY, model);
        buffer.finish();
        RenderSystem.enableDepthTest();
        if (flag) {
            RenderHelper.setupGui3DDiffuseLighting();
        }
        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        matrix.pop();
    }

    public static void renderItemOverlayIntoGUI(ItemRenderer itemRenderer, MatrixStack matrix, FontRenderer font, ItemStack stack, int xPosition, int yPosition,
          @Nullable String text) {
        //Copy of ItemRenderer#renderItemOverlayIntoGUI with added support for a matrix stack instead of using render system
        if (!stack.isEmpty()) {
            if (stack.getCount() != 1 || text != null) {
                matrix.push();
                String s = text == null ? String.valueOf(stack.getCount()) : text;
                matrix.translate(0, 0, itemRenderer.zLevel + 200);
                IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
                font.renderString(s, xPosition + 17 - font.getStringWidth(s), yPosition + 9, 0xFFFFFF, true, matrix.getLast().getMatrix(),
                      buffer, false, 0, MekanismRenderer.FULL_LIGHT);
                buffer.finish();
                matrix.pop();
            }
            if (stack.getItem().showDurabilityBar(stack)) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableAlphaTest();
                RenderSystem.disableBlend();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                double health = stack.getItem().getDurabilityForDisplay(stack);
                int i = Math.round(13.0F - (float)health * 13.0F);
                int j = stack.getItem().getRGBDurabilityForDisplay(stack);
                Matrix4f matrix4f = matrix.getLast().getMatrix();
                draw(matrix4f, buffer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
                draw(matrix4f, buffer, xPosition + 2, yPosition + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
                RenderSystem.enableBlend();
                RenderSystem.enableAlphaTest();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
            ClientPlayerEntity player = Minecraft.getInstance().player;
            float f3 = player == null ? 0 : player.getCooldownTracker().getCooldown(stack.getItem(), Minecraft.getInstance().getRenderPartialTicks());
            if (f3 > 0) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
                draw(matrix.getLast().getMatrix(), buffer, xPosition, yPosition + MathHelper.floor(16F * (1F - f3)), 16,
                      MathHelper.ceil(16F * f3), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
        }
    }

    private static void draw(Matrix4f matrix, BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        //Copy of ItemRenderer#draw with added support for a matrix stack instead of using render system
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos(matrix, x, y, 0).color(red, green, blue, alpha).endVertex();
        renderer.pos(matrix, x, y + height, 0).color(red, green, blue, alpha).endVertex();
        renderer.pos(matrix, x + width, y + height, 0).color(red, green, blue, alpha).endVertex();
        renderer.pos(matrix, x + width, y, 0).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }
}
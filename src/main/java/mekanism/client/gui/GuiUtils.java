package mekanism.client.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.List;
import java.util.function.Predicate;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

public class GuiUtils {

    private GuiUtils() {
    }

    public static void drawOutline(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int color) {
        fill(guiGraphics, x, y, width, 1, color);
        fill(guiGraphics, x, y + height - 1, width, 1, color);
        if (height > 2) {
            fill(guiGraphics, x, y + 1, 1, height - 2, color);
            fill(guiGraphics, x + width - 1, y + 1, 1, height - 2, color);
        }
    }

    public static void fill(GuiGraphicsExtractor guiGraphics, RenderPipeline pipeline, int x, int y, int width, int height, int color) {
        if (width != 0 && height != 0) {
            guiGraphics.fill(pipeline, x, y, x + width, y + height, color);
        }
    }

    public static void fill(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int color) {
        if (width != 0 && height != 0) {
            guiGraphics.fill(x, y, x + width, y + height, color);
        }
    }

    public static void drawBackdrop(GuiGraphicsExtractor guiGraphics, Minecraft minecraft, int x, int y, int width, int alpha) {
        drawBackdrop(guiGraphics, minecraft, x, y, width, minecraft.font.lineHeight, alpha);
    }

    public static void drawBackdrop(GuiGraphicsExtractor guiGraphics, Minecraft minecraft, int x, int y, int width, int height, int alpha) {
        //Slightly modified copy of Gui#drawBackdrop so that we can support it in places that can't directly call it
        int backgroundColor = minecraft.options.getBackgroundColor(0.0F);
        if (backgroundColor != 0) {
            int argb = ARGB.white(alpha);
            //TODO - 26.1: Can we merge the multiply and argb calls into one?
            guiGraphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, ARGB.multiply(backgroundColor, argb));
        }
    }

    //Copied from JEI
    public static void drawTiledSpriteVanilla(GuiGraphicsExtractor guiGraphics, int posX, int posY, int tiledWidth, int tiledHeight, int color, int scaledAmount, TextureAtlasSprite sprite) {
        SpriteContents spriteContents = sprite.contents();
        GuiSpriteScaling.Tile tileScaling = new GuiSpriteScaling.Tile(spriteContents.width(), spriteContents.height());

        posY = posY + tiledHeight - scaledAmount;

        guiGraphics.enableScissor(posX, posY, posX + tiledWidth, posY + scaledAmount);
        {
            guiGraphics.blitTiledSprite(
                  RenderPipelines.GUI_TEXTURED,
                  sprite,
                  posX,
                  posY,
                  tiledWidth,
                  scaledAmount,
                  0,
                  0,
                  tileScaling.width(),
                  tileScaling.height(),
                  tileScaling.width(),
                  tileScaling.height(),
                  color
            );
        }
        guiGraphics.disableScissor();
    }

    public static void drawTiledSprite(GuiGraphicsExtractor guiGraphics, int xPosition, int yPosition, int yOffset, int desiredWidth, int desiredHeight, TextureAtlasSprite sprite,
          int textureWidth, int textureHeight, int zLevel, TilingDirection tilingDirection) {
        drawTiledSprite(guiGraphics, xPosition, yPosition, yOffset, desiredWidth, desiredHeight, sprite, textureWidth, textureHeight, zLevel, tilingDirection, true);
    }

    public static void drawTiledSprite(GuiGraphicsExtractor guiGraphics, int xPosition, int yPosition, int yOffset, int desiredWidth, int desiredHeight, TextureAtlasSprite sprite,
          int textureWidth, int textureHeight, int zLevel, TilingDirection tilingDirection, boolean blend) {
        if (desiredWidth == 0 || desiredHeight == 0 || textureWidth == 0 || textureHeight == 0) {
            return;
        }
        //TODO - 26.1 drawTiledSprite
        /*RenderSystem.setShader(GameRenderer::getPositionTexShader);
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
        //Note: We still use the tesselator as that is what GuiGraphicsExtractor#innerBlit does
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
        }*/
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
    public static <CHILD extends GuiEventListener> CHILD findChild(List<? extends CHILD> children, MouseButtonEvent event, boolean isDoubleClick, MouseClickedPredicate<CHILD> checker) {
        for (int i = children.size() - 1; i >= 0; i--) {
            CHILD child = children.get(i);
            if (checker.test(child, event, isDoubleClick)) {
                return child;
            }
        }
        return null;
    }

    public static <CHILD extends GuiEventListener> boolean checkChildren(List<? extends CHILD> children, KeyEvent event, KeyPressedPredicate<CHILD> checker) {
        for (int i = children.size() - 1; i >= 0; i--) {
            CHILD child = children.get(i);
            if (checker.test(child, event)) {
                return true;
            }
        }
        return false;
    }

    public static <CHILD extends GuiEventListener> boolean checkChildrenChar(List<? extends CHILD> children, CharacterEvent event, CharTypedPredicate<CHILD> checker) {
        for (int i = children.size() - 1; i >= 0; i--) {
            CHILD child = children.get(i);
            if (checker.test(child, event)) {
                return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface MouseOverPredicate<ELEMENT> {

        boolean test(ELEMENT element, double mouseX, double mouseY);
    }

    @FunctionalInterface
    public interface MouseClickedPredicate<ELEMENT> {

        boolean test(ELEMENT element, MouseButtonEvent event, boolean isDoubleClick);
    }

    @FunctionalInterface
    public interface KeyPressedPredicate<ELEMENT> {

        boolean test(ELEMENT element, KeyEvent event);
    }

    public interface CharTypedPredicate<ELEMENT> {

        boolean test(ELEMENT element, CharacterEvent event);
    }

    public static void renderItem(GuiGraphicsExtractor guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis, float scale, Font font, @Nullable String text, boolean overlay) {
        if (!stack.isEmpty()) {
            try {
                Matrix3x2fStack pose = guiGraphics.pose();
                pose.pushMatrix();
                if (scale != 1) {
                    //Translate before scaling, and then set xAxis and yAxis to zero so that we don't translate a second time
                    pose.translate(xAxis, yAxis);
                    pose.scale(scale, scale);
                    xAxis = 0;
                    yAxis = 0;
                }
                guiGraphics.item(stack, xAxis, yAxis);
                if (overlay) {
                    //When we render items ourselves in virtual slots or scroll slots we want to compress the z scale
                    // for rendering the stored count so that it doesn't clip with later windows
                    //TODO - 26.1 check this - pose.translate(0, 0, -25);
                    guiGraphics.itemDecorations(font, stack, xAxis, yAxis, text);
                }

                pose.popMatrix();
            } catch (Exception e) {
                Mekanism.logger.error("Failed to render stack into gui: {}", stack, e);
            }
        }
    }

    public static void renderBorder(GuiGraphicsExtractor guiGraphics, int x, int y, int boxWidth, int boxHeight, int color) {
        guiGraphics.horizontalLine(x, x + boxWidth, y, color);
        guiGraphics.horizontalLine(x, x + boxWidth, y + boxHeight, color);
        guiGraphics.verticalLine(x, y, y + boxHeight, color);
        guiGraphics.verticalLine(x + boxWidth, y, y + boxHeight, color);
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
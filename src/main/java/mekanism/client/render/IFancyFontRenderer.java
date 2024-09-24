package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.SpecialColors;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

//TODO - 1.21: Document this class
public interface IFancyFontRenderer {

    int getXSize();

    Font font();

    default int titleTextColor() {
        return SpecialColors.TEXT_TITLE.argb();
    }

    default int headingTextColor() {
        return SpecialColors.TEXT_HEADING.argb();
    }

    default int subheadingTextColor() {
        return SpecialColors.TEXT_SUBHEADING.argb();
    }

    default int screenTextColor() {
        return SpecialColors.TEXT_SCREEN.argb();
    }

    default int activeButtonTextColor() {
        return SpecialColors.TEXT_ACTIVE_BUTTON.argb();
    }

    default int inactiveButtonTextColor() {
        return SpecialColors.TEXT_INACTIVE_BUTTON.argb();
    }

    default int drawString(GuiGraphics guiGraphics, Component component, float x, float y, int color, boolean shadow) {
        return guiGraphics.drawString(font(), component.getVisualOrderText(), x, y, color, shadow);
    }

    default int getStringWidth(Component component) {
        return font().width(component);
    }

    default int getLineHeight() {
        return font().lineHeight;
    }

    default void drawTitleText(GuiGraphics guiGraphics, Component text, int y) {
        drawScrollingString(guiGraphics, text, 0, y, TextAlignment.CENTER, titleTextColor(), 4, false);
    }

    default void drawTitleTextTextWithOffset(GuiGraphics guiGraphics, Component text, int x, int y) {
        drawTitleTextTextWithOffset(guiGraphics, text, x, y, getXSize());
    }

    default void drawTitleTextTextWithOffset(GuiGraphics guiGraphics, Component text, int x, int y, int end) {
        drawTitleTextTextWithOffset(guiGraphics, text, x, y, end, 4, TextAlignment.CENTER);
    }

    default void drawTitleTextTextWithOffset(GuiGraphics guiGraphics, Component text, int x, int y, int end, int maxLengthPad, TextAlignment alignment) {
        drawScrollingString(guiGraphics, text, x, y, alignment, titleTextColor(), end - x, maxLengthPad, false);
    }

    default void drawScrollingString(GuiGraphics guiGraphics, Component text, int x, int y, TextAlignment alignment, int color, int maxLengthPad, boolean shadow) {
        drawScrollingString(guiGraphics, text, x, y, alignment, color, getXSize(), maxLengthPad, shadow);
    }

    default void drawScrollingString(GuiGraphics guiGraphics, Component text, int x, int y, TextAlignment alignment, int color, int width, int maxLengthPad, boolean shadow) {
        drawScrollingString(guiGraphics, text, x, y, alignment, color, width, getLineHeight(), maxLengthPad, shadow);
    }

    default void drawScrollingString(GuiGraphics guiGraphics, Component text, int x, int y, TextAlignment alignment, int color, int width, int height, int maxLengthPad, boolean shadow) {
        drawScrollingString(guiGraphics, text, x + maxLengthPad, y, x + width - maxLengthPad, y + height, alignment, color, shadow);
    }

    default void drawScrollingString(GuiGraphics guiGraphics, Component text, int minX, int minY, int maxX, int maxY, TextAlignment alignment, int color, boolean shadow) {
        int textWidth = getStringWidth(text);
        //Note: Instead of doing what vanilla does, we divide to float, and don't add one
        // That way if min and max are not just lineHeight away they will be more accurate, and otherwise it won't render one line below where it should be
        float targetY = (minY + maxY - getLineHeight()) / 2F;
        int areaWidth = maxX - minX;
        if (textWidth > areaWidth) {
            int overflowWidth = textWidth - areaWidth;
            double seconds = Util.getMillis() / 1_000D;
            double scrollPeriod = Math.max(overflowWidth * AbstractWidget.PERIOD_PER_SCROLLED_PIXEL, AbstractWidget.MIN_SCROLL_PERIOD);
            double scrolledSoFar = Math.sin((Math.PI / 2) * Math.cos((2 * Math.PI) * seconds / scrollPeriod)) / 2.0 + AbstractWidget.PERIOD_PER_SCROLLED_PIXEL;
            double overflowedBy = scrolledSoFar * overflowWidth;//Vanilla uses: Mth.lerp(d2, 0.0, overflowWidth); But that is equivalent to just multiplying performing the multiplication
            //Note: We are drawing in relative coordinates, but GuiGraphics#enableScissor, is expecting absolute coordinates,
            // so we need to get the translations from our pose stack
            int left = getTranslationX(guiGraphics);
            int top = getTranslationY(guiGraphics);
            guiGraphics.enableScissor(left + minX, top + minY, left + maxX, top + maxY);
            //TODO: Float?
            drawString(guiGraphics, text, minX - (int) overflowedBy, targetY, color, shadow);
            guiGraphics.disableScissor();
        } else {
            drawString(guiGraphics, text, alignment.getTarget(minX, maxX, areaWidth, textWidth), targetY, color, shadow);
        }
    }

    default void drawScaledScrollingString(GuiGraphics guiGraphics, Component text, int x, int y, TextAlignment alignment, int color, int maxLengthPad, boolean shadow, float scale) {
        drawScaledScrollingString(guiGraphics, text, x, y, alignment, color, getXSize(), maxLengthPad, shadow, scale);
    }

    default void drawScaledScrollingString(GuiGraphics guiGraphics, Component text, int x, int y, TextAlignment alignment, int color, int width, int maxLengthPad,
          boolean shadow, float scale) {
        drawScaledScrollingString(guiGraphics, text, x, y, alignment, color, width, getLineHeight(), maxLengthPad, shadow, scale);
    }

    default void drawScaledScrollingString(GuiGraphics guiGraphics, Component text, int x, int y, TextAlignment alignment, int color, int width, int height, int maxLengthPad,
          boolean shadow, float scale) {
        drawScaledScrollingString(guiGraphics, text, x + maxLengthPad, y, x + width - maxLengthPad, y + getLineHeight(), alignment, color, shadow, scale);
    }

    default void drawScaledScrollingString(GuiGraphics guiGraphics, Component text, int minX, int minY, int maxX, int maxY, TextAlignment alignment, int color, boolean shadow,
          float scale) {
        if (scale == 1.0F) {
            drawScrollingString(guiGraphics, text, minX, minY, maxX, maxY, alignment, color, shadow);
            return;
        }
        float textWidth = getStringWidth(text) * scale;
        //Note: Instead of doing what vanilla does, we divide to float, and don't add one
        // That way if min and max are not just lineHeight away they will be more accurate, and otherwise it won't render one line below where it should be
        float targetY = (minY + maxY - getLineHeight()) / 2F;
        int areaWidth = maxX - minX;
        if (textWidth > areaWidth) {
            float overflowWidth = textWidth - areaWidth;
            double seconds = Util.getMillis() / 1_000D;
            double scrollPeriod = Math.max(overflowWidth * AbstractWidget.PERIOD_PER_SCROLLED_PIXEL, AbstractWidget.MIN_SCROLL_PERIOD);
            double scrolledSoFar = Math.sin((Math.PI / 2) * Math.cos((2 * Math.PI) * seconds / scrollPeriod)) / 2.0 + AbstractWidget.PERIOD_PER_SCROLLED_PIXEL;
            double overflowedBy = scrolledSoFar * overflowWidth;//Vanilla uses: Mth.lerp(d2, 0.0, overflowWidth); But that is equivalent to just multiplying performing the multiplication
            //Note: We are drawing in relative coordinates, but GuiGraphics#enableScissor, is expecting absolute coordinates,
            // so we need to get the translations from our pose stack
            int left = getTranslationX(guiGraphics);
            int top = getTranslationY(guiGraphics);
            guiGraphics.enableScissor(left + minX, top + minY, left + maxX, top + maxY);
            //TODO: Should this cast to float? I believe this makes it less choppy, but a bit blurry?
            // So we probably don't want to cast overflowedBy to float
            drawTextWithScale(guiGraphics, text, minX - (int) overflowedBy, targetY, color, shadow, scale);
            guiGraphics.disableScissor();
        } else {
            drawTextWithScale(guiGraphics, text, alignment.getTarget(minX, maxX, areaWidth, textWidth), targetY, color, shadow, scale);
        }
    }

    private void drawTextWithScale(GuiGraphics guiGraphics, Component text, float x, float y, int color, boolean shadow, float scale) {
        PoseStack pose = prepTextScale(guiGraphics, x, y, scale);
        drawString(guiGraphics, text, 0, 0, color, shadow);
        pose.popPose();
    }

    //Note: As translate will implicitly cast x and y to being floats, we might as well pass these in as floats to reduce duplicate code
    private static PoseStack prepTextScale(GuiGraphics guiGraphics, float x, float y, float scale) {
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        return prepTextScale(pose, x, y, scale);
    }

    static PoseStack prepTextScale(PoseStack pose, float x, float y, float scale) {
        float yAdd = 4 - 4 * scale;
        pose.translate(x, y + yAdd, 0);
        pose.scale(scale, scale, scale);
        return pose;
    }

    /**
     * Equivalent to what {@link Matrix4f#getTranslation(Vector3f)} would do, without the extra allocations.
     */
    private static int getTranslationX(GuiGraphics guiGraphics) {
        return (int) guiGraphics.pose().last().pose().m30();
    }

    /**
     * Equivalent to what {@link Matrix4f#getTranslation(Vector3f)} would do, without the extra allocations.
     */
    private static int getTranslationY(GuiGraphics guiGraphics) {
        return (int) guiGraphics.pose().last().pose().m31();
    }

    enum TextAlignment {
        LEFT,
        CENTER,
        RIGHT;

        public float getTarget(int minX, int maxX, float areaWidth, float textWidth) {
            //TODO: Do we want to swap left and right when Font#isBidirectional is true?
            // I believe in right to left languages text is meant to be aligned to the right
            // but it likely would look odd in various GUIs
            return switch (this) {
                case LEFT -> minX;
                case CENTER -> minX + (areaWidth - textWidth) / 2F;
                case RIGHT -> maxX - textWidth;
            };
        }
    }

    // efficient tool to draw word-by-word wrapped text based on a horizontal bound. looks intimidating but runs in O(n)
    class WrappedTextRenderer {

        private final List<LineData> linesToDraw = new ArrayList<>();
        private final IFancyFontRenderer font;
        private final String text;
        @Nullable
        private Font lastFont;
        private int lastMaxLength = -1;
        private int lineLength = 0;

        public WrappedTextRenderer(IFancyFontRenderer font, Component text) {
            this(font, text.getString());
        }

        public WrappedTextRenderer(IFancyFontRenderer font, String text) {
            this.font = font;
            this.text = text;
        }

        public void renderCentered(GuiGraphics guiGraphics, int x, int y, int color, int maxLength) {
            calculateLines(maxLength);
            int startY = y;
            int lineHeight = font.getLineHeight();
            int width = font.getXSize();
            for (LineData line : linesToDraw) {
                font.drawString(guiGraphics, line.component(), x + (width - line.length()) / 2F, startY, color, false);
                startY += lineHeight;
            }
        }

        public int renderWithScale(GuiGraphics guiGraphics, int x, int y, int color, int maxLength, float scale) {
            //Divide by scale for calculating actual max length so that when the text is scaled it has the proper total space available
            calculateLines(Mth.floor(maxLength / scale));
            PoseStack pose = prepTextScale(guiGraphics, x, y, scale);
            int startY = 0;
            int lineHeight = font.getLineHeight();
            for (LineData line : linesToDraw) {
                font.drawString(guiGraphics, line.component(), 0, startY, color, false);
                startY += lineHeight;
            }
            pose.popPose();
            return linesToDraw.size();
        }

        void calculateLines(int maxLength) {
            //If something changed since the last time we calculated it
            Font font = this.font.font();
            if (font != null && (lastFont != font || lastMaxLength != maxLength)) {
                lastFont = font;
                lastMaxLength = maxLength;
                linesToDraw.clear();
                StringBuilder lineBuilder = new StringBuilder();
                StringBuilder wordBuilder = new StringBuilder();
                int spaceLength = font.width(" ");
                int wordLength = 0;
                for (char c : text.toCharArray()) {
                    if (c == ' ') {
                        lineBuilder = addWord(lineBuilder, wordBuilder, maxLength, spaceLength, wordLength);
                        wordBuilder = new StringBuilder();
                        wordLength = 0;
                        continue;
                    }
                    wordBuilder.append(c);
                    wordLength += font.width(Character.toString(c));
                }
                if (!wordBuilder.isEmpty()) {
                    lineBuilder = addWord(lineBuilder, wordBuilder, maxLength, spaceLength, wordLength);
                }
                if (!lineBuilder.isEmpty()) {
                    linesToDraw.add(new LineData(lineBuilder, lineLength));
                }
            }
        }

        StringBuilder addWord(StringBuilder lineBuilder, StringBuilder wordBuilder, int maxLength, int spaceLength, int wordLength) {
            // ignore spacing if this is the first word of the line
            int spacingLength = lineBuilder.isEmpty() ? 0 : spaceLength;
            if (lineLength + spacingLength + wordLength > maxLength) {
                linesToDraw.add(new LineData(lineBuilder, lineLength));
                lineBuilder = new StringBuilder(wordBuilder);
                lineLength = wordLength;
            } else {
                if (spacingLength > 0) {
                    lineBuilder.append(" ");
                }
                lineBuilder.append(wordBuilder);
                lineLength += spacingLength + wordLength;
            }
            return lineBuilder;
        }

        public static int calculateHeightRequired(Font font, Component text, int width, int maxLength) {
            //TODO: Come up with a better way of doing this (maybe allow it to somehow replace what the stored font is
            // that way we can calculate, and then use the calculated values in our actual renderer without having to calculate once more
            WrappedTextRenderer wrappedTextRenderer = new WrappedTextRenderer(new SimpleFancyFontRenderer(font, width), text);
            wrappedTextRenderer.calculateLines(maxLength);
            return font.lineHeight * wrappedTextRenderer.linesToDraw.size();
        }

        private record LineData(Component component, int length) {

            private LineData(StringBuilder lineBuilder, int length) {
                //TODO - 1.21: Make use of the style of the passed in parent component that we split during?
                // Similar to StringSplitter$LineBreakFinder
                this(TextComponentUtil.getString(lineBuilder.toString()), length);
            }
        }

        private record SimpleFancyFontRenderer(Font font, int getXSize) implements IFancyFontRenderer {
        }
    }
}

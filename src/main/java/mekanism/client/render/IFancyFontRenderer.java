package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.SpecialColors;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface IFancyFontRenderer {

    int getXSize();

    Font getFont();

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

    default int drawString(PoseStack matrix, Component component, int x, int y, int color) {
        return getFont().draw(matrix, component, x, y, color);
    }

    default int getStringWidth(Component component) {
        return getFont().width(component);
    }

    default void drawCenteredText(PoseStack matrix, Component component, float x, float y, int color) {
        drawCenteredText(matrix, component, x, 0, y, color);
    }

    default void drawCenteredText(PoseStack matrix, Component component, float xStart, float areaWidth, float y, int color) {
        int textWidth = getStringWidth(component);
        float centerX = xStart + (areaWidth / 2F) - (textWidth / 2F);
        drawTextExact(matrix, component, centerX, y, color);
    }

    default void drawTitleText(PoseStack matrix, Component text, float y) {
        drawCenteredTextScaledBound(matrix, text, getXSize() - 8, y, titleTextColor());
    }

    default void drawScaledCenteredTextScaledBound(PoseStack matrix, Component text, float left, float y, int color, float maxX, float textScale) {
        float width = getStringWidth(text) * textScale;
        float scale = Math.min(1, maxX / width) * textScale;
        drawScaledCenteredText(matrix, text, left, y, color, scale);
    }

    default void drawScaledCenteredText(PoseStack matrix, Component text, float left, float y, int color, float scale) {
        int textWidth = getStringWidth(text);
        float centerX = left - (textWidth / 2F) * scale;
        drawTextWithScale(matrix, text, centerX, y, color, scale);
    }

    default void drawCenteredTextScaledBound(PoseStack matrix, Component text, float maxLength, float y, int color) {
        drawCenteredTextScaledBound(matrix, text, maxLength, 0, y, color);
    }

    default void drawCenteredTextScaledBound(PoseStack matrix, Component text, float maxLength, float x, float y, int color) {
        float scale = Math.min(1, maxLength / getStringWidth(text));
        drawScaledCenteredText(matrix, text, x + getXSize() / 2F, y, color, scale);
    }

    default void drawTextExact(PoseStack matrix, Component text, float x, float y, int color) {
        matrix.pushPose();
        matrix.translate(x, y, 0);
        drawString(matrix, text, 0, 0, color);
        matrix.popPose();
    }

    default float getNeededScale(Component text, float maxLength) {
        int length = getStringWidth(text);
        return length <= maxLength ? 1 : maxLength / length;
    }

    default void drawTextScaledBound(PoseStack matrix, String text, float x, float y, int color, float maxLength) {
        drawTextScaledBound(matrix, TextComponentUtil.getString(text), x, y, color, maxLength);
    }

    default void drawTextScaledBound(PoseStack matrix, Component component, float x, float y, int color, float maxLength) {
        int length = getStringWidth(component);

        if (length <= maxLength) {
            drawTextExact(matrix, component, x, y, color);
        } else {
            drawTextWithScale(matrix, component, x, y, color, maxLength / length);
        }
        //Make sure the color does not leak from having drawn the string
        MekanismRenderer.resetColor();
    }

    default void drawScaledTextScaledBound(PoseStack matrix, Component text, float x, float y, int color, float maxX, float textScale) {
        float width = getStringWidth(text) * textScale;
        float scale = Math.min(1, maxX / width) * textScale;
        drawTextWithScale(matrix, text, x, y, color, scale);
    }

    default void drawTextWithScale(PoseStack matrix, Component text, float x, float y, int color, float scale) {
        prepTextScale(matrix, m -> drawString(m, text, 0, 0, color), x, y, scale);
    }

    default void prepTextScale(PoseStack matrix, Consumer<PoseStack> runnable, float x, float y, float scale) {
        float yAdd = 4 - (scale * 8) / 2F;
        matrix.pushPose();
        matrix.translate(x, y + yAdd, 0);
        matrix.scale(scale, scale, scale);
        runnable.accept(matrix);
        matrix.popPose();
        MekanismRenderer.resetColor();
    }

    /**
     * @apiNote Consider caching the {@link WrappedTextRenderer} instead of using this method.
     */
    default int drawWrappedTextWithScale(PoseStack matrix, Component text, float x, float y, int color, float maxLength, float scale) {
        return new WrappedTextRenderer(this, text).renderWithScale(matrix, x, y, color, maxLength, scale);
    }

    /**
     * @apiNote Consider caching the {@link WrappedTextRenderer} instead of using this method.
     */
    default void drawWrappedCenteredText(PoseStack matrix, Component text, float x, float y, int color, float maxLength) {
        new WrappedTextRenderer(this, text).renderCentered(matrix, x, y, color, maxLength);
    }

    // efficient tool to draw word-by-word wrapped text based on a horizontal bound. looks intimidating but runs in O(n)
    class WrappedTextRenderer {

        private final List<LineData> linesToDraw = new ArrayList<>();
        private final IFancyFontRenderer font;
        private final String text;
        @Nullable
        private Font lastFont;
        private float lastMaxLength = -1;
        private float lineLength = 0;

        public WrappedTextRenderer(IFancyFontRenderer font, Component text) {
            this(font, text.getString());
        }

        public WrappedTextRenderer(IFancyFontRenderer font, String text) {
            this.font = font;
            this.text = text;
        }

        public void renderCentered(PoseStack matrix, float x, float y, int color, float maxLength) {
            calculateLines(maxLength);
            float startY = y;
            for (LineData line : linesToDraw) {
                font.drawTextExact(matrix, line.component(), x - line.length() / 2, startY, color);
                startY += 9;
            }
        }

        public int renderWithScale(PoseStack matrix, float x, float y, int color, float maxLength, float scale) {
            //Divide by scale for calculating actual max length so that when the text is scaled it has the proper total space available
            calculateLines(maxLength / scale);
            font.prepTextScale(matrix, m -> {
                int startY = 0;
                for (LineData line : linesToDraw) {
                    font.drawString(m, line.component(), 0, startY, color);
                    startY += 9;
                }
            }, x, y, scale);
            return linesToDraw.size();
        }

        void calculateLines(float maxLength) {
            //If something changed since the last time we calculated it
            Font font = this.font.getFont();
            if (font != null && (lastFont != font || lastMaxLength != maxLength)) {
                lastFont = font;
                lastMaxLength = maxLength;
                linesToDraw.clear();
                StringBuilder lineBuilder = new StringBuilder();
                StringBuilder wordBuilder = new StringBuilder();
                int spaceLength = lastFont.width(" ");
                int wordLength = 0;
                for (char c : text.toCharArray()) {
                    if (c == ' ') {
                        lineBuilder = addWord(lineBuilder, wordBuilder, maxLength, spaceLength, wordLength);
                        wordBuilder = new StringBuilder();
                        wordLength = 0;
                        continue;
                    }
                    wordBuilder.append(c);
                    wordLength += lastFont.width(Character.toString(c));
                }
                if (!wordBuilder.isEmpty()) {
                    lineBuilder = addWord(lineBuilder, wordBuilder, maxLength, spaceLength, wordLength);
                }
                if (!lineBuilder.isEmpty()) {
                    linesToDraw.add(new LineData(TextComponentUtil.getString(lineBuilder.toString()), lineLength));
                }
            }
        }

        StringBuilder addWord(StringBuilder lineBuilder, StringBuilder wordBuilder, float maxLength, int spaceLength, int wordLength) {
            // ignore spacing if this is the first word of the line
            float spacingLength = lineBuilder.isEmpty() ? 0 : spaceLength;
            if (lineLength + spacingLength + wordLength > maxLength) {
                linesToDraw.add(new LineData(TextComponentUtil.getString(lineBuilder.toString()), lineLength));
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

        public static int calculateHeightRequired(Font font, Component text, int width, float maxLength) {
            return calculateHeightRequired(font, text.getString(), width, maxLength);
        }

        public static int calculateHeightRequired(Font font, String text, int width, float maxLength) {
            //TODO: Come up with a better way of doing this
            WrappedTextRenderer wrappedTextRenderer = new WrappedTextRenderer(new IFancyFontRenderer() {
                @Override
                public int getXSize() {
                    return width;
                }

                @Override
                public Font getFont() {
                    return font;
                }
            }, text);
            wrappedTextRenderer.calculateLines(maxLength);
            return 9 * wrappedTextRenderer.linesToDraw.size();
        }

        private record LineData(Component component, float length) {
        }
    }
}

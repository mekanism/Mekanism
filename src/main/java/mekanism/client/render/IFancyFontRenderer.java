package mekanism.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mekanism.common.config.MekanismConfig;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.tuple.Pair;

//TODO - 1.16: Test this class to make sure it still works as intended
public interface IFancyFontRenderer {

    int getXSize();

    FontRenderer getFont();

    default int titleTextColor() {
        return MekanismConfig.client.guiTitleTextColor.get();
    }

    default int screenTextColor() {
        return MekanismConfig.client.guiScreenTextColor.get();
    }

    default int drawString(MatrixStack matrix, ITextComponent component, int x, int y, int color) {
        return getFont().func_238422_b_(matrix, component, x, y, color);
    }

    default int getStringWidth(ITextComponent component) {
        return getFont().func_238414_a_(component);
    }

    default void drawCenteredText(MatrixStack matrix, ITextComponent component, float x, float y, int color) {
        drawCenteredText(matrix, component, x, 0, y, color);
    }

    default void drawCenteredText(MatrixStack matrix, ITextComponent component, float xStart, float areaWidth, float y, int color) {
        int textWidth = getStringWidth(component);
        float centerX = xStart + (areaWidth / 2F) - (textWidth / 2F);
        drawTextExact(matrix, component, centerX, y, color);
    }

    default void drawTitleText(MatrixStack matrix, ITextComponent text, float y) {
        drawCenteredTextScaledBound(matrix, text, getXSize() - 8, y, titleTextColor());
    }

    default void drawScaledCenteredText(MatrixStack matrix, ITextComponent text, float left, float y, int color, float scale) {
        int textWidth = getStringWidth(text);
        float centerX = left - (textWidth / 2F) * scale;
        drawTextWithScale(matrix, text, centerX, y, color, scale);
    }

    default void drawCenteredTextScaledBound(MatrixStack matrix, ITextComponent text, float maxLength, float y, int color) {
        float scale = Math.min(1, maxLength / getStringWidth(text));
        drawScaledCenteredText(matrix, text, getXSize() / 2F, y, color, scale);
    }

    default void drawTextExact(MatrixStack matrix, ITextComponent text, float x, float y, int color) {
        matrix.push();
        matrix.translate(x, y, 0);
        drawString(matrix, text, 0, 0, color);
        matrix.pop();
    }

    default float getNeededScale(ITextComponent text, float maxLength) {
        int length = getStringWidth(text);
        return length <= maxLength ? 1 : maxLength / length;
    }

    default void drawTextScaledBound(MatrixStack matrix, String text, float x, float y, int color, float maxLength) {
        drawTextScaledBound(matrix, new StringTextComponent(text), x, y, color, maxLength);
    }

    default void drawTextScaledBound(MatrixStack matrix, ITextComponent component, float x, float y, int color, float maxLength) {
        int length = getStringWidth(component);

        if (length <= maxLength) {
            drawTextExact(matrix, component, x, y, color);
        } else {
            drawTextWithScale(matrix, component, x, y, color, maxLength / length);
        }
        //Make sure the color does not leak from having drawn the string
        MekanismRenderer.resetColor();
    }

    default void drawScaledTextScaledBound(MatrixStack matrix, ITextComponent text, float x, float y, int color, float maxX, float textScale) {
        float width = getStringWidth(text) * textScale;
        float scale = Math.min(1, maxX / width) * textScale;
        drawTextWithScale(matrix, text, x, y, color, scale);
    }

    default void drawTextWithScale(MatrixStack matrix, ITextComponent text, float x, float y, int color, float scale) {
        prepTextScale(matrix, m -> drawString(m, text, 0, 0, color), x, y, scale);
    }

    default void prepTextScale(MatrixStack matrix, Consumer<MatrixStack> runnable, float x, float y, float scale) {
        float yAdd = 4 - (scale * 8) / 2F;
        matrix.push();
        matrix.translate(x, y + yAdd, 0);
        matrix.scale(scale, scale, scale);
        runnable.accept(matrix);
        matrix.pop();
        MekanismRenderer.resetColor();
    }

    default void drawWrappedCenteredText(MatrixStack matrix, String text, float x, float y, int color, float maxLength) {
        new WrappedTextRenderer(this).render(matrix, text, x, y, color, maxLength);
    }

    // efficient tool to draw word-by-word wrapped text based on a horizontal bound. looks intimidating but runs in O(n)
    class WrappedTextRenderer {

        final IFancyFontRenderer font;
        final List<Pair<ITextComponent, Float>> linesToDraw = new ArrayList<>();
        StringBuilder lineBuilder = new StringBuilder(), wordBuilder = new StringBuilder();
        float lineLength = 0, wordLength = 0;
        final float SPACE_LENGTH;

        WrappedTextRenderer(IFancyFontRenderer font) {
            this.font = font;
            SPACE_LENGTH = font.getFont().getStringWidth(" ");//TODO - 1.16: Evaluate this, used to use char width
        }

        void render(MatrixStack matrix, String text, float x, float y, int color, float maxLength) {
            for (char c : text.toCharArray()) {
                if (c == ' ') {
                    addWord(maxLength);
                    continue;
                }
                wordBuilder.append(c);
                wordLength += font.getFont().getStringWidth(Character.toString(c));//TODO - 1.16: Evaluate this, used to use char width
            }
            if (wordBuilder.length() > 0) {
                addWord(maxLength);
            }
            if (lineBuilder.length() > 0) {
                linesToDraw.add(Pair.of(new StringTextComponent(lineBuilder.toString()), lineLength));
            }
            float startY = y;
            for (Pair<ITextComponent, Float> p : linesToDraw) {
                font.drawTextExact(matrix, p.getLeft(), x - p.getRight() / 2, startY, color);
                startY += 9;
            }
        }

        void addWord(float maxLength) {
            // ignore spacing if this is the first word of the line
            float spacingLength = lineBuilder.length() == 0 ? 0 : SPACE_LENGTH;
            if (lineLength + spacingLength + wordLength > maxLength) {
                linesToDraw.add(Pair.of(new StringTextComponent(lineBuilder.toString()), lineLength));
                lineBuilder = new StringBuilder(wordBuilder);
                lineLength = wordLength;
            } else {
                if (spacingLength > 0) {
                    lineBuilder.append(" ");
                }
                lineBuilder.append(wordBuilder);
                lineLength += spacingLength + wordLength;
            }
            wordLength = 0;
            wordBuilder = new StringBuilder();
        }
    }
}

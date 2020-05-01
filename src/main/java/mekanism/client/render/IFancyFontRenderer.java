package mekanism.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import mekanism.common.config.MekanismConfig;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;

public interface IFancyFontRenderer {

    int getXSize();

    FontRenderer getFont();

    default int titleTextColor() {
        return MekanismConfig.client.guiTitleTextColor.get();
    }

    default int screenTextColor() {
        return MekanismConfig.client.guiScreenTextColor.get();
    }

    default int drawString(String text, int x, int y, int color) {
        return getFont().drawString(text, x, y, color);
    }

    default int drawString(ITextComponent component, int x, int y, int color) {
        return drawString(component.getFormattedText(), x, y, color);
    }

    default int getStringWidth(ITextComponent component) {
        return getFont().getStringWidth(component.getFormattedText());
    }

    default int getStringWidth(String text) {
        return getFont().getStringWidth(text);
    }

    default void drawCenteredText(ITextComponent component, float x, float y, int color) {
        drawCenteredText(component, x, 0, y, color);
    }

    default void drawCenteredText(ITextComponent component, float xStart, float areaWidth, float y, int color) {
        int textWidth = getStringWidth(component);
        float centerX = xStart + (areaWidth / 2F) - (textWidth / 2F);
        drawTextExact(component.getFormattedText(), centerX, y, color);
    }

    default void drawTitleText(ITextComponent text, float y) {
        drawCenteredTextScaledBound(text, getXSize() - 8, y, titleTextColor());
    }

    default void drawScaledCenteredText(ITextComponent text, float left, float y, int color, float scale) {
        int textWidth = getStringWidth(text);
        float centerX = left - (textWidth / 2F) * scale;
        drawTextWithScale(text.getString(), centerX, y, color, scale);
    }

    default void drawCenteredTextScaledBound(ITextComponent text, float maxLength, float y, int color) {
        float scale = Math.min(1, maxLength / getStringWidth(text));
        drawScaledCenteredText(text, getXSize() / 2F, y, color, scale);
    }

    default void drawTextExact(String text, float x, float y, int color) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, 0);
        drawString(text, 0, 0, color);
        RenderSystem.popMatrix();
    }

    default float getNeededScale(ITextComponent text, float maxLength) {
        int length = getStringWidth(text);
        return length <= maxLength ? 1 : maxLength / length;
    }

    default float getNeededScale(String text, float maxLength) {
        int length = getStringWidth(text);
        return length <= maxLength ? 1 : maxLength / length;
    }

    default void drawTextScaledBound(String text, float x, float y, int color, float maxLength) {
        int length = getFont().getStringWidth(text);

        if (length <= maxLength) {
            drawTextExact(text, x, y, color);
        } else {
            drawTextWithScale(text, x, y, color, maxLength / length);
        }
        //Make sure the color does not leak from having drawn the string
        MekanismRenderer.resetColor();
    }

    default void drawScaledText(ITextComponent component, float x, float y, int color, float maxX) {
        drawTextScaledBound(component.getFormattedText(), x, y, color, maxX);
    }

    default void drawScaledTextScaledBound(ITextComponent text, float x, float y, int color, float maxX, float textScale) {
        float width = getStringWidth(text) * textScale;
        float scale = Math.min(1, maxX / width) * textScale;
        drawTextWithScale(text.getFormattedText(), x, y, color, scale);
    }

    default void drawTextWithScale(String text, float x, float y, int color, float scale) {
        float yAdd = 4 - (scale * 8) / 2F;
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y + yAdd, 0);
        RenderSystem.scalef(scale, scale, scale);
        drawString(text, 0, 0, color);
        RenderSystem.popMatrix();
        MekanismRenderer.resetColor();
    }
}

package mekanism.client.gui.button;

import mekanism.common.util.text.TextComponentUtil;

public class TranslationButton extends MekanismButton {

    public TranslationButton(int x, int y, int width, int height, String translationKey, IPressable onPress) {
        this(x, y, width, height, TextComponentUtil.translate(translationKey).getFormattedText(), onPress, null);
    }

    public TranslationButton(int x, int y, int width, int height, String translationKey, IPressable onPress, IHoverable onHover) {
        super(x, y, width, height, TextComponentUtil.translate(translationKey).getFormattedText(), onPress, onHover);
    }
}
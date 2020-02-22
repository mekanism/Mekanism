package mekanism.client.gui.element.button;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.base.ILangEntry;

public class TranslationButton extends MekanismButton {

    public TranslationButton(IGuiWrapper gui, int x, int y, int width, int height, ILangEntry translationHelper, Runnable onPress) {
        this(gui, x, y, width, height, translationHelper, onPress, null);
    }

    public TranslationButton(IGuiWrapper gui, int x, int y, int width, int height, ILangEntry translationHelper, Runnable onPress, IHoverable onHover) {
        super(gui, x, y, width, height, translationHelper.translate().getFormattedText(), onPress, onHover);
    }
}
package mekanism.client.gui.element.button;

import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.lib.Color;

public class DigitalButton extends TranslationButton {

    public DigitalButton(IGuiWrapper gui, int x, int y, int width, int height, ILangEntry translationHelper, Runnable onPress, IHoverable onHover) {
        super(gui, x, y, width, height, translationHelper, onPress, onHover, null);
        setButtonBackground(ButtonBackground.DIGITAL);
    }

    @Override
    protected int getButtonTextColor(int mouseX, int mouseY) {
        if (active) {
            if (isMouseOverCheckWindows(mouseX, mouseY)) {
                return screenTextColor();
            }
            return Color.argb(screenTextColor()).darken(0.2).argb();
        }
        return Color.argb(screenTextColor()).darken(0.4).argb();
    }
}
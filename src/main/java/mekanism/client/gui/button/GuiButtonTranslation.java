package mekanism.client.gui.button;

import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.client.gui.widget.button.Button;

public class GuiButtonTranslation extends Button {

    public GuiButtonTranslation(int x, int y, int width, int height, String translationKey, IPressable onPress) {
        super(x, y, width, height, TextComponentUtil.getTranslationComponent(translationKey).getFormattedText(), onPress);
    }
}
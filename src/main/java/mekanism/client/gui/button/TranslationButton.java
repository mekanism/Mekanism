package mekanism.client.gui.button;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TranslationButton extends MekanismButton {

    public TranslationButton(IGuiWrapper gui, int x, int y, int width, int height, String translationKey, IPressable onPress) {
        this(gui, x, y, width, height, TextComponentUtil.translate(translationKey).getFormattedText(), onPress, null);
    }

    public TranslationButton(IGuiWrapper gui, int x, int y, int width, int height, String translationKey, IPressable onPress, IHoverable onHover) {
        super(gui, x, y, width, height, TextComponentUtil.translate(translationKey).getFormattedText(), onPress, onHover);
    }
}
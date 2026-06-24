package mekanism.client.gui.element.button;

import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

public class TranslationButton extends MekanismButton {

    @Nullable
    private final Supplier<@Nullable EnumColor> colorSupplier;

    public TranslationButton(IGuiWrapper gui, int x, int y, int width, int height, ILangEntry translationHelper, IClickable onPress) {
        this(gui, x, y, width, height, translationHelper, onPress, null);
    }

    public TranslationButton(IGuiWrapper gui, int x, int y, int width, int height, ILangEntry translationHelper, IClickable onPress,
          @Nullable Supplier<@Nullable EnumColor> colorSupplier) {
        super(gui, x, y, width, height, translationHelper.translate(), onPress);
        this.colorSupplier = colorSupplier;
    }

    @Override
    protected int getButtonBlitColor() {
        if (colorSupplier != null) {
            EnumColor color = colorSupplier.get();
            return color == null ? CommonColors.WHITE : color.getPackedColor();
        }
        return super.getButtonBlitColor();
    }
}
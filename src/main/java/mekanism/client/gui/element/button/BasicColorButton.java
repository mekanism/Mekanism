package mekanism.client.gui.element.button;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.lib.Color;
import net.minecraft.network.chat.CommonComponents;
import org.jspecify.annotations.Nullable;

public class BasicColorButton extends MekanismButton {

    public static BasicColorButton toggle(IGuiWrapper gui, int x, int y, int size, EnumColor color, BooleanSupplier toggled, IClickable onLeftClick) {
        return new BasicColorButton(gui, x, y, size, () -> toggled.getAsBoolean() ? color : null, onLeftClick, onLeftClick);
    }

    private final Supplier<@Nullable EnumColor> colorSupplier;

    public BasicColorButton(IGuiWrapper gui, int x, int y, int size, Supplier<@Nullable EnumColor> color, IClickable onLeftClick, @Nullable IClickable onRightClick) {
        super(gui, x, y, size, size, CommonComponents.EMPTY, onLeftClick, onRightClick);
        this.colorSupplier = color;
    }

    @Override
    protected int getButtonBlitColor() {
        EnumColor color = getColor();
        //TODO - 26.2: this seems silly. there should be no need for all the Color boxing/unboxing
        if (color != null) {
            Color c = Color.rgb(color.getRgbCode());
            double[] hsv = c.hsvArray();
            hsv[1] = Math.max(0, hsv[1] - 0.1);
            hsv[2] = Math.min(1, hsv[2] + 0.1);
            return Color.hsv(hsv[0], hsv[1], hsv[2]).argb();
        }
        return super.getButtonBlitColor();
    }

    @Nullable
    public EnumColor getColor() {
        return this.colorSupplier.get();
    }
}

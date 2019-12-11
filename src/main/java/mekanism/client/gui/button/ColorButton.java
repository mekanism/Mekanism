package mekanism.client.gui.button;

import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.text.TextComponentUtil;

public class ColorButton extends MekanismButton {

    private final Supplier<EnumColor> colorSupplier;

    public ColorButton(IGuiWrapper gui, int x, int y, int width, int height, Supplier<EnumColor> colorSupplier, Runnable onPress, Runnable onRightClick) {
        super(gui, x, y, width, height, "", onPress, onRightClick, (onHover, xAxis, yAxis) -> {
            EnumColor color = colorSupplier.get();
            if (color != null) {
                gui.displayTooltip(color.getColoredName(), xAxis, yAxis);
            } else {
                gui.displayTooltip(TextComponentUtil.translate("gui.mekanism.none"), xAxis, yAxis);
            }
        });
        this.colorSupplier = colorSupplier;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        //Ensure the color gets reset. The default GuiButtonImage doesn't so other GuiButton's can have the color leak out of them
        EnumColor color = colorSupplier.get();
        if (color != null) {
            fill(this.x, this.y, this.x + this.width, this.y + this.height, MekanismRenderer.getColorARGB(color, 1));
            MekanismRenderer.resetColor();
        }
    }
}
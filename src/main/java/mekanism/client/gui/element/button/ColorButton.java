package mekanism.client.gui.element.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import net.minecraft.util.text.StringTextComponent;

public class ColorButton extends MekanismButton {

    private final Supplier<EnumColor> colorSupplier;

    public ColorButton(IGuiWrapper gui, int x, int y, int width, int height, Supplier<EnumColor> colorSupplier, Runnable onPress, Runnable onRightClick) {
        super(gui, x, y, width, height, StringTextComponent.field_240750_d_, onPress, onRightClick, (onHover, matrix, xAxis, yAxis) -> {
            EnumColor color = colorSupplier.get();
            if (color != null) {
                gui.displayTooltip(matrix, color.getColoredName(), xAxis, yAxis);
            } else {
                gui.displayTooltip(matrix, MekanismLang.NONE.translate(), xAxis, yAxis);
            }
        });
        this.colorSupplier = colorSupplier;
    }

    @Override
    public void func_230431_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        //Ensure the color gets reset. The default GuiButtonImage doesn't so other GuiButton's can have the color leak out of them
        EnumColor color = colorSupplier.get();
        if (color != null) {
            func_238467_a_(matrix, this.field_230690_l_, this.field_230691_m_, this.field_230690_l_ + this.field_230688_j_, this.field_230691_m_ + this.field_230689_k_, MekanismRenderer.getColorARGB(color, 1));
            MekanismRenderer.resetColor();
        }
    }
}
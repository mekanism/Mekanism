package mekanism.client.gui.element.button;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ColorButton extends MekanismButton {

    private final Supplier<EnumColor> colorSupplier;

    public ColorButton(IGuiWrapper gui, int x, int y, int width, int height, Supplier<EnumColor> colorSupplier, Runnable onPress, Runnable onRightClick) {
        super(gui, x, y, width, height, Component.empty(), onPress, onRightClick, (onHover, matrix, mouseX, mouseY) -> {
            EnumColor color = colorSupplier.get();
            if (color != null) {
                gui.displayTooltips(matrix, mouseX, mouseY, color.getColoredName());
            } else {
                gui.displayTooltips(matrix, mouseX, mouseY, MekanismLang.NONE.translate());
            }
        });
        this.colorSupplier = colorSupplier;
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        //Ensure the color gets reset. The default GuiButtonImage doesn't so other GuiButton's can have the color leak out of them
        EnumColor color = colorSupplier.get();
        if (color != null) {
            fill(matrix, this.x, this.y, this.x + this.width, this.y + this.height, MekanismRenderer.getColorARGB(color, 1));
            MekanismRenderer.resetColor();
        }
    }
}
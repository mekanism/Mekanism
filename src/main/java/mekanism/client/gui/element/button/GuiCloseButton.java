package mekanism.client.gui.element.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class GuiCloseButton extends MekanismImageButton {

    public GuiCloseButton(IGuiWrapper gui, int x, int y, GuiWindow window) {
        super(gui, x, y, 8, MekanismUtils.getResource(ResourceType.GUI_BUTTON, "close.png"), window::close);
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        displayTooltip(matrix, MekanismLang.CLOSE.translate(), mouseX, mouseY);
    }

    @Override
    public boolean resetColorBeforeRender() {
        return false;
    }
}

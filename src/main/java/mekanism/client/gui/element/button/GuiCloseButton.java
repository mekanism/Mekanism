package mekanism.client.gui.element.button;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import org.jetbrains.annotations.NotNull;

public class GuiCloseButton extends MekanismImageButton {

    public GuiCloseButton(IGuiWrapper gui, int x, int y, GuiWindow window) {
        super(gui, x, y, 8, MekanismUtils.getResource(ResourceType.GUI_BUTTON, "close.png"), window::close);
    }

    @Override
    public void renderToolTip(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        displayTooltips(matrix, mouseX, mouseY, MekanismLang.CLOSE.translate());
    }

    @Override
    public boolean resetColorBeforeRender() {
        return false;
    }
}

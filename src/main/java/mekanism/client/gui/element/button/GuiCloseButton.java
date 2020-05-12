package mekanism.client.gui.element.button;

import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiWindow;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class GuiCloseButton extends MekanismImageButton {

    public GuiCloseButton(IGuiWrapper gui, int x, int y, GuiWindow window) {
        super(gui, x, y, 8, MekanismUtils.getResource(ResourceType.GUI_BUTTON, "close.png"), () -> window.close());
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        MekanismRenderer.color(EnumColor.RED);
        super.renderButton(mouseX, mouseY, partialTicks);
        MekanismRenderer.resetColor();
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        this.displayTooltip(MekanismLang.CLOSE.translate(), mouseX, mouseY);
    }

    @Override
    public boolean resetColorBeforeRender() {
        return false;
    }
}

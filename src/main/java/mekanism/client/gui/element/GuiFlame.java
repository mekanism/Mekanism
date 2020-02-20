package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

//TODO: Once we make the progress things its own sub type of gui element instead of being mashed together how it is now
// in a singular texture, make GuiFlame extend it
public class GuiFlame extends GuiTexturedElement {

    private final IProgressInfoHandler handler;

    public GuiFlame(IProgressInfoHandler handler, IGuiWrapper gui, int x, int y) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "flame.png"), gui, x, y, 13, 13);
        this.handler = handler;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        guiObj.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, 2 * width, height);
        if (handler.isActive()) {
            int displayInt = (int) (handler.getProgress() * height);
            guiObj.drawModalRectWithCustomSizedTexture(x, y + height - displayInt, width, height - displayInt, width, displayInt, 2 * width, height);
        }
    }
}
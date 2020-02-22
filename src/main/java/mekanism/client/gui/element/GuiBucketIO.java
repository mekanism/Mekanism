package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class GuiBucketIO extends GuiTexturedElement {

    public GuiBucketIO(IGuiWrapper gui) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "bucket.png"), gui, gui.getWidth(), 66, 26, 57);
        active = false;
        //TODO: Clean this up, the texture doesn't need the slots on it anymore, and we can use the down arrow texture we have
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        guiObj.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
    }
}
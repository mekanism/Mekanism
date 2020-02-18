package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiRobitTab extends GuiTexturedElement {

    private static final ResourceLocation TAB = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "robit_sidebar.png");

    public GuiRobitTab(IGuiWrapper gui) {
        super(TAB, gui, 176, 6, 25, 106);
        active = false;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        blit(x, y, 0, 0, width, height, width, height);
    }
}
package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiSecurityPublic extends GuiTexturedElement {

    private static final ResourceLocation PUBLIC = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "public.png");

    public GuiSecurityPublic(IGuiWrapper gui, int x, int y) {
        super(PUBLIC, gui, x, y, 18, 18);
        active = false;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        blit(x, y, 0, 0, width, height, width, height);
    }
}
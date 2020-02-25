package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiSideHolder extends GuiTexturedElement {

    private static final ResourceLocation HOLDER_LEFT = MekanismUtils.getResource(ResourceType.GUI, "holder_left.png");
    private static final ResourceLocation HOLDER_RIGHT = MekanismUtils.getResource(ResourceType.GUI, "holder_right.png");
    private static final int TEXTURE_WIDTH = 26;
    private static final int TEXTURE_HEIGHT = 9;

    protected final boolean left;

    public GuiSideHolder(IGuiWrapper gui, int x, int y, int height) {
        super(x < 0 ? HOLDER_LEFT : HOLDER_RIGHT, gui, x, y, TEXTURE_WIDTH, height);
        this.left = x < 0;
        active = false;
    }

    protected void colorTab() {
        //Don't do any coloring by default
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        colorTab();
        //Top
        blit(x, y, 0, 0, width, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Middle
        int middleHeight = height - 8;
        if (middleHeight > 0) {
            blit(x, y + 4, width, middleHeight, 0, 4, width, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
        //Bottom
        blit(x, y + 4 + middleHeight, 0, 5, width, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        MekanismRenderer.resetColor();
    }
}
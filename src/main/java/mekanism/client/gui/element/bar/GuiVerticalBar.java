package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public abstract class GuiVerticalBar<INFO extends IBarInfoHandler> extends GuiBar<INFO> {

    private static final ResourceLocation BAR = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "vertical_bar.png");

    public GuiVerticalBar(ResourceLocation resource, IGuiWrapper gui, INFO handler, int x, int y, int width, int height) {
        //TODO: Bump the width by 2? for the border of the bar image? Or maybe remove border
        super(resource, gui, handler, x, y, width, height);
    }

    @Override
    public void renderBar() {
        minecraft.textureManager.bindTexture(BAR);
        guiObj.drawModalRectWithCustomSizedTexture(x, y, width, height, 0, 0, 6, 54, 6, 54);
    }
}
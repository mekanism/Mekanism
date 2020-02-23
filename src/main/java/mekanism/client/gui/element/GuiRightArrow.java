package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiRightArrow extends GuiTextureOnlyElement {

    private static final ResourceLocation ARROW = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "right_arrow.png");

    public GuiRightArrow(IGuiWrapper gui, int x, int y) {
        super(ARROW, gui, x, y, 22, 15);
    }
}
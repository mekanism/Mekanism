package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;

public class GuiUpArrow extends GuiTextureOnlyElement {

    private static final ResourceLocation ARROW = MekanismUtils.getResource(ResourceType.GUI, "up_arrow.png");

    public GuiUpArrow(IGuiWrapper gui, int x, int y) {
        super(ARROW, gui, x, y, 8, 10);
    }
}
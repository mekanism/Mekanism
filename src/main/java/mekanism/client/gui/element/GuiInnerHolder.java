package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiInnerHolder extends GuiScalableElement {

    private static final ResourceLocation HOLDER = MekanismUtils.getResource(ResourceType.GUI, "inner_holder.png");

    public GuiInnerHolder(IGuiWrapper gui, int x, int y, int width, int height) {
        super(HOLDER, gui, x, y, width, height, 3, 3);
    }
}
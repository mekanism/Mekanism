package mekanism.client.gui.element.bar;

import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiVerticalRateBar extends GuiBar<IBarInfoHandler> {

    private static final ResourceLocation RATE_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "vertical_rate.png");
    private static final int texWidth = 6;
    private static final int texHeight = 58;

    public GuiVerticalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y) {
        super(RATE_BAR, gui, handler, x, y, texWidth, texHeight);
    }

    @Override
    protected void renderBarOverlay(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        int displayInt = (int) (getHandler().getLevel() * texHeight);
        //TODO: Should textureX be texWidth + 2
        func_238463_a_(matrix, field_230690_l_ + 1, field_230691_m_ + field_230689_k_ - 1 - displayInt, 8, field_230689_k_ - 2 - displayInt, field_230688_j_ - 2, displayInt, texWidth, texHeight);
    }
}
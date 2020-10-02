package mekanism.client.gui.element.bar;

import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.Color.ColorFunction;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiDynamicHorizontalRateBar extends GuiBar<IBarInfoHandler> {

    private static final ResourceLocation RATE_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "dynamic_rate.png");
    private static final int texWidth = 3;
    private static final int texHeight = 8;

    private final ColorFunction colorFunction;

    public GuiDynamicHorizontalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int width) {
        this(gui, handler, x, y, width, ColorFunction.HEAT);
    }

    public GuiDynamicHorizontalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int width, ColorFunction colorFunction) {
        super(RATE_BAR, gui, handler, x, y, width, texHeight);
        this.colorFunction = colorFunction;
    }

    @Override
    protected void renderBarOverlay(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        int displayInt = (int) (getHandler().getLevel() * (width - 2));
        for (int i = 0; i < displayInt; i++) {
            float level = (float) i / (float) (width - 2);
            MekanismRenderer.color(colorFunction.getColor(level));
            if (i == 0) {
                blit(matrix, x + 1, y + 1, 0, 0, 1, texHeight, texWidth, texHeight);
            } else if (i == displayInt - 1) {
                blit(matrix, x + 1 + i, y + 1, texWidth - 1, 0, 1, texHeight, texWidth, texHeight);
            } else {
                blit(matrix, x + 1 + i, y + 1, 1, 0, 1, texHeight, texWidth, texHeight);
            }
            MekanismRenderer.resetColor();
        }
    }
}

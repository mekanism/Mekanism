package mekanism.client.gui.element.bar;

import com.mojang.blaze3d.systems.RenderSystem;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Color;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiDynamicHorizontalRateBar extends GuiBar<IBarInfoHandler> {
    private static final ResourceLocation RATE_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "dynamic_rate.png");
    private static final int texWidth = 3;
    private static final int texHeight = 8;

    private ColorFunction colorFunction;

    public GuiDynamicHorizontalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int width) {
        this(gui, handler, x, y, width, ColorFunction.HEAT);
    }

    public GuiDynamicHorizontalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int width, ColorFunction colorFunction) {
        super(RATE_BAR, gui, handler, x, y, width, texHeight);
        this.colorFunction = colorFunction;
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {
        int displayInt = (int) (getHandler().getLevel() * (width - 2));
        for (int i = 0; i < displayInt; i++) {
            float level = (float) i / (float) (width - 2);
            Color color = colorFunction.getColor(level);
            RenderSystem.color4f(color.r / 255F, color.g / 255F, color.b / 255F, color.a / 255F);
            if (i == 0) {
                blit(x + 1, y + 1, 0, 0, 1, texHeight, texWidth, texHeight);
            } else if (i == displayInt-1) {
                blit(x + 1 + i, y + 1, texWidth - 1, 0, 1, texHeight, texWidth, texHeight);
            } else {
                blit(x + 1 + i, y + 1, 1, 0, 1, texHeight, texWidth, texHeight);
            }
            MekanismRenderer.resetColor();
        }
    }

    public static interface ColorFunction {

        public static final ColorFunction HEAT = (level) -> Color.rgba((int) Math.min(200, 400 * level), (int) Math.max(0, 200 - Math.max(0, -200 + 400 * level)), 0, 255);

        public static ColorFunction scale(Color from, Color to) {
            return (level) -> from.blend(to, level);
        }

        public Color getColor(float level);
    }
}

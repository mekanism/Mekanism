package mekanism.client.gui.element.bar;

import mekanism.api.energy.IEnergyContainer;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.Mekanism;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class GuiVerticalPowerBar extends GuiBar<IBarInfoHandler> {

    private static final Identifier ENERGY_BAR = Mekanism.rl("bar/vertical_power");

    public GuiVerticalPowerBar(IGuiWrapper gui, IEnergyContainer container, int x, int y) {
        this(gui, container, x, y, 52);
    }

    public GuiVerticalPowerBar(IGuiWrapper gui, IEnergyContainer container, int x, int y, int desiredHeight) {
        this(gui, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return EnergyDisplay.of(container).getTextComponent();
            }

            @Override
            public double getLevel() {
                return ContainerType.ENERGY.divideToLevel(container);
            }
        }, x, y, desiredHeight);
    }

    public GuiVerticalPowerBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y) {
        this(gui, handler, x, y, 52);
    }

    public GuiVerticalPowerBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int desiredHeight) {
        super(gui, handler, x, y, 4, desiredHeight, false);
    }

    @Override
    protected void renderBarContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        int barWidth = width - 2;
        int barHeight = height - 2;
        int targetHeight = calculateSize(handlerLevel, barHeight);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENERGY_BAR, relativeX + 1, relativeY + height - 1 - targetHeight, barWidth, targetHeight);
    }
}
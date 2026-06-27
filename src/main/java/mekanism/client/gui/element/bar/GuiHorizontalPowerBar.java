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

public class GuiHorizontalPowerBar extends GuiBar<IBarInfoHandler> {

    public static final Identifier ENERGY_BAR = Mekanism.rl("bar/horizontal_power");

    public GuiHorizontalPowerBar(IGuiWrapper gui, IEnergyContainer container, int x, int y) {
        this(gui, container, x, y, 52);
    }

    public GuiHorizontalPowerBar(IGuiWrapper gui, IEnergyContainer container, int x, int y, int desiredWidth) {
        this(gui, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return EnergyDisplay.of(container).getTextComponent();
            }

            @Override
            public double getLevel() {
                return ContainerType.ENERGY.divideToLevel(container);
            }
        }, x, y, desiredWidth);
    }

    public GuiHorizontalPowerBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y) {
        this(gui, handler, x, y, 52);
    }

    public GuiHorizontalPowerBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int desiredWidth) {
        super(gui, handler, x, y, desiredWidth, 4, true);
    }

    @Override
    protected void renderBarContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENERGY_BAR, relativeX + 1, relativeY + 1, calculateSize(handlerLevel, width - 2), height - 2);
    }
}
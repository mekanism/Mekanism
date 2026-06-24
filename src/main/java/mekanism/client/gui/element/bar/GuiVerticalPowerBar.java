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
    private static final int texHeight = 52;

    private final double heightScale;

    public GuiVerticalPowerBar(IGuiWrapper gui, IEnergyContainer container, int x, int y) {
        this(gui, container, x, y, texHeight);
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
        this(gui, handler, x, y, texHeight);
    }

    public GuiVerticalPowerBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int desiredHeight) {
        super(gui, handler, x, y, 4, desiredHeight, false);
        heightScale = desiredHeight / (double) texHeight;
    }

    @Override
    protected void renderBarOverlay(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        int displayInt = (int) (handlerLevel * texHeight);
        if (displayInt > 0) {
            int scaled = calculateScaled(heightScale, displayInt);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ENERGY_BAR, relativeX + 1, relativeY + height - 1 - scaled, width - 2, scaled);
        }
    }
}
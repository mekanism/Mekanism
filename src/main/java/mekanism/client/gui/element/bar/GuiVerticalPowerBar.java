package mekanism.client.gui.element.bar;

import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiVerticalPowerBar extends GuiBar<IBarInfoHandler> {

    private static final ResourceLocation ENERGY_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "vertical_power.png");
    private static final int texWidth = 4;
    private static final int texHeight = 52;

    private final double heightScale;

    public GuiVerticalPowerBar(IGuiWrapper gui, IStrictEnergyHandler handler, int x, int y) {
        this(gui, handler, x, y, texHeight);
    }

    public GuiVerticalPowerBar(IGuiWrapper gui, IStrictEnergyHandler handler, int x, int y, int desiredHeight) {
        this(gui, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                int energyContainerCount = handler.getEnergyContainerCount();
                if (energyContainerCount == 0) {
                    return EnergyDisplay.of(0).getTextComponent();
                } else if (energyContainerCount == 1) {
                    return EnergyDisplay.of(handler.getEnergy(0), handler.getMaxEnergy(0)).getTextComponent();
                }
                double energy = 0;
                double maxEnergy = 0;
                for (int container = 0; container < energyContainerCount; container++) {
                    //TODO: Make sure to account for double overflow
                    energy += handler.getEnergy(container);
                    maxEnergy += handler.getMaxEnergy(container);
                }
                return EnergyDisplay.of(energy, maxEnergy).getTextComponent();
            }

            @Override
            public double getLevel() {
                int energyContainerCount = handler.getEnergyContainerCount();
                if (energyContainerCount == 0) {
                    return 0;
                } else if (energyContainerCount == 1) {
                    return handler.getEnergy(0) / handler.getMaxEnergy(0);
                }
                double energy = 0;
                double maxEnergy = 0;
                for (int container = 0; container < energyContainerCount; container++) {
                    //TODO: Make sure to account for double overflow
                    energy += handler.getEnergy(container);
                    maxEnergy += handler.getMaxEnergy(container);
                }
                return energy / maxEnergy;
            }
        }, x, y, desiredHeight);
    }

    public GuiVerticalPowerBar(IGuiWrapper gui, IEnergyContainer container, int x, int y) {
        this(gui, container, x, y, texHeight);
    }

    public GuiVerticalPowerBar(IGuiWrapper gui, IEnergyContainer container, int x, int y, int desiredHeight) {
        this(gui, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return EnergyDisplay.of(container.getEnergy(), container.getMaxEnergy()).getTextComponent();
            }

            @Override
            public double getLevel() {
                return container.getEnergy() / container.getMaxEnergy();
            }
        }, x, y, desiredHeight);
    }

    public GuiVerticalPowerBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y) {
        this(gui, handler, x, y, texHeight);
    }

    public GuiVerticalPowerBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int desiredHeight) {
        super(ENERGY_BAR, gui, handler, x, y, texWidth, desiredHeight);
        heightScale = desiredHeight / (double) texHeight;
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {
        int displayInt = (int) (getHandler().getLevel() * texHeight);
        int scaled = calculateScaled(heightScale, displayInt);
        blit(x + 1, y + height - 1 - scaled, texWidth, scaled, 0, 0, texWidth, displayInt, texWidth, texHeight);
    }
}
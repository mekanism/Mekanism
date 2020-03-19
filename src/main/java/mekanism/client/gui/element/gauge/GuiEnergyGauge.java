package mekanism.client.gui.element.gauge;

import mekanism.api.energy.IEnergyContainer;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;

public class GuiEnergyGauge extends GuiGauge<Void> {

    private final IEnergyInfoHandler infoHandler;

    public GuiEnergyGauge(IEnergyContainer container, GaugeType type, IGuiWrapper gui, int x, int y) {
        this(new IEnergyInfoHandler() {
            @Override
            public double getEnergy() {
                return container.getEnergy();
            }

            @Override
            public double getMaxEnergy() {
                return container.getMaxEnergy();
            }
        }, type, gui, x, y);
    }

    public GuiEnergyGauge(IEnergyInfoHandler handler, GaugeType type, IGuiWrapper gui, int x, int y) {
        super(type, gui, x, y);
        infoHandler = handler;
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.ENERGY;
    }

    @Override
    public int getScaledLevel() {
        if (infoHandler.getEnergy() == Double.MAX_VALUE) {
            return height - 2;
        }
        return (int) (infoHandler.getEnergy() * (height - 2) / infoHandler.getMaxEnergy());
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return MekanismRenderer.energyIcon;
    }

    @Override
    public ITextComponent getTooltipText() {
        if (infoHandler.getEnergy() <= 0) {
            return MekanismLang.EMPTY.translate();
        }
        return EnergyDisplay.of(infoHandler.getEnergy(), infoHandler.getMaxEnergy()).getTextComponent();
    }

    public interface IEnergyInfoHandler {

        double getEnergy();

        double getMaxEnergy();
    }
}
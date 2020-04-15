package mekanism.client.gui.element.gauge;

import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
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
            public FloatingLong getEnergy() {
                return container.getEnergy();
            }

            @Override
            public FloatingLong getMaxEnergy() {
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
        if (infoHandler.getEnergy().equals(FloatingLong.MAX_VALUE)) {
            return height - 2;
        }
        return (int) ((height - 2) * infoHandler.getEnergy().divideToLevel(infoHandler.getMaxEnergy()));
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return MekanismRenderer.energyIcon;
    }

    @Override
    public ITextComponent getLabel() {
        return null;
    }

    @Override
    public ITextComponent getTooltipText() {
        if (infoHandler.getEnergy().isZero()) {
            return MekanismLang.EMPTY.translate();
        }
        return EnergyDisplay.of(infoHandler.getEnergy(), infoHandler.getMaxEnergy()).getTextComponent();
    }

    public interface IEnergyInfoHandler {

        FloatingLong getEnergy();

        FloatingLong getMaxEnergy();
    }
}
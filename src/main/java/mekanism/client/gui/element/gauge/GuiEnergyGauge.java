package mekanism.client.gui.element.gauge;

import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;

public class GuiEnergyGauge extends GuiGauge<Void> {

    private final IEnergyInfoHandler infoHandler;

    public GuiEnergyGauge(IEnergyInfoHandler handler, Type type, IGuiWrapper gui, int x, int y) {
        super(type, gui, x, y);
        infoHandler = handler;
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.ENERGY;
    }

    @Override
    public int getScaledLevel() {
        if (infoHandler.getEnergyStorage().getEnergy() == Double.MAX_VALUE) {
            return height - 2;
        }
        return (int) (infoHandler.getEnergyStorage().getEnergy() * (height - 2) / infoHandler.getEnergyStorage().getMaxEnergy());
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return MekanismRenderer.energyIcon;
    }

    @Override
    public ITextComponent getTooltipText() {
        if (infoHandler.getEnergyStorage().getEnergy() > 0) {
            return EnergyDisplay.of(infoHandler.getEnergyStorage().getEnergy(), infoHandler.getEnergyStorage().getMaxEnergy()).getTextComponent();
        }
        return MekanismLang.EMPTY.translate();
    }

    public interface IEnergyInfoHandler {

        IStrictEnergyStorage getEnergyStorage();
    }
}
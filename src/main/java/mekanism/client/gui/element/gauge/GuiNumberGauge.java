package mekanism.client.gui.element.gauge;

import static java.lang.Math.min;

import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;

public class GuiNumberGauge extends GuiGauge<Void> {

    private final INumberInfoHandler infoHandler;

    public GuiNumberGauge(INumberInfoHandler handler, GaugeType type, IGuiWrapper gui, int x, int y) {
        super(type, gui, x, y);
        infoHandler = handler;
    }

    @Override
    public TransmissionType getTransmission() {
        return null;
    }

    @Override
    public int getScaledLevel() {
        return (int) ((height - 2) * min(infoHandler.getLevel() / infoHandler.getMaxLevel(), 1));
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return infoHandler.getIcon();
    }

    @Override
    public ITextComponent getTooltipText() {
        return infoHandler.getText(infoHandler.getLevel());
    }


    public interface INumberInfoHandler {

        TextureAtlasSprite getIcon();

        double getLevel();

        double getMaxLevel();

        ITextComponent getText(double level);
    }
}
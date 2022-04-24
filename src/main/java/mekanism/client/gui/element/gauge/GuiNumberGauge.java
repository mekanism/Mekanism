package mekanism.client.gui.element.gauge;

import java.util.Collections;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.lib.transmitter.TransmissionType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;

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
        return (int) ((height - 2) * infoHandler.getScaledLevel());
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return infoHandler.getIcon();
    }

    @Override
    public Component getLabel() {
        return null;
    }

    @Override
    public List<Component> getTooltipText() {
        return Collections.singletonList(infoHandler.getText());
    }

    public interface INumberInfoHandler {

        TextureAtlasSprite getIcon();

        double getLevel();

        double getScaledLevel();

        Component getText();
    }
}
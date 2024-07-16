package mekanism.client.gui.element.gauge;

import java.util.Collections;
import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;

public class GuiEnergyGauge extends GuiGauge<Void> {

    private final IEnergyInfoHandler infoHandler;

    public GuiEnergyGauge(IEnergyContainer container, GaugeType type, IGuiWrapper gui, int x, int y) {
        this(new IEnergyInfoHandler() {
            @Override
            public long getEnergy() {
                return container.getEnergy();
            }

            @Override
            public long getMaxEnergy() {
                return container.getMaxEnergy();
            }
        }, type, gui, x, y);
    }

    public GuiEnergyGauge(IEnergyInfoHandler handler, GaugeType type, IGuiWrapper gui, int x, int y) {
        super(type, gui, x, y);
        infoHandler = handler;
    }

    public GuiEnergyGauge(IEnergyInfoHandler handler, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        super(type, gui, x, y, sizeX, sizeY);
        infoHandler = handler;
    }

    public static GuiEnergyGauge getDummy(GaugeType type, IGuiWrapper gui, int x, int y) {
        GuiEnergyGauge gauge = new GuiEnergyGauge((IEnergyInfoHandler) null, type, gui, x, y);
        gauge.dummy = true;
        return gauge;
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.ENERGY;
    }

    @Override
    public int getScaledLevel() {
        if (dummy) {
            return height - 2;
        }
        if (infoHandler.getEnergy() == 0L) {
            return 0;
        } else if (infoHandler.getEnergy() == Long.MAX_VALUE) {
            return height - 2;
        }
        return Math.max(1, (int) ((height - 2) * MathUtils.divideToLevel(infoHandler.getEnergy(), infoHandler.getMaxEnergy())));
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return MekanismRenderer.energyIcon;
    }

    @Override
    public Component getLabel() {
        return null;
    }

    @Override
    public List<Component> getTooltipText() {
        if (dummy) {
            return Collections.emptyList();
        } else if (infoHandler.getEnergy() == 0) {
            return Collections.singletonList(MekanismLang.EMPTY.translate());
        }
        return Collections.singletonList(EnergyDisplay.of(infoHandler.getEnergy(), infoHandler.getMaxEnergy()).getTextComponent());
    }

    public interface IEnergyInfoHandler {

        long getEnergy();

        long getMaxEnergy();
    }
}
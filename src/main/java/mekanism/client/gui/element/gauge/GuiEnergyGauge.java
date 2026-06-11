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
import org.jspecify.annotations.Nullable;

public class GuiEnergyGauge extends GuiGauge<@Nullable Void> {

    @Nullable
    private final IEnergyInfoHandler infoHandler;

    public GuiEnergyGauge(IEnergyContainer container, GaugeType type, IGuiWrapper gui, int x, int y) {
        this(getInfoHandler(container), type, gui, x, y);
    }

    public GuiEnergyGauge(IEnergyInfoHandler handler, GaugeType type, IGuiWrapper gui, int x, int y) {
        super(type, gui, x, y);
        infoHandler = handler;
    }

    private GuiEnergyGauge(GaugeType type, IGuiWrapper gui, int x, int y) {
        super(type, gui, x, y);
        infoHandler = null;
    }

    public GuiEnergyGauge(IEnergyInfoHandler handler, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        super(type, gui, x, y, sizeX, sizeY);
        infoHandler = handler;
    }

    public static GuiEnergyGauge getDummy(GaugeType type, IGuiWrapper gui, int x, int y) {
        GuiEnergyGauge gauge = new GuiEnergyGauge(type, gui, x, y);
        gauge.dummy = true;
        return gauge;
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.ENERGY;
    }

    @Override
    public int getScaledLevel() {
        if (dummy || infoHandler == null) {
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

    @Nullable
    @Override
    public Component getLabel() {
        return null;
    }

    @Override
    public List<Component> getTooltipText() {
        if (dummy || infoHandler == null) {
            return Collections.emptyList();
        } else if (infoHandler.getEnergy() == 0) {
            return Collections.singletonList(MekanismLang.EMPTY.translate());
        }
        return Collections.singletonList(EnergyDisplay.of(infoHandler.getEnergy(), infoHandler.getMaxEnergy()).getTextComponent());
    }

    public static IEnergyInfoHandler getInfoHandler(IEnergyContainer container) {
        return new IEnergyInfoHandler() {
            @Override
            public long getEnergy() {
                return container.getAmountAsLong();
            }

            @Override
            public long getMaxEnergy() {
                return container.getCapacityAsLong();
            }
        };
    }

    public interface IEnergyInfoHandler {

        IEnergyInfoHandler ALWAYS_FULL = new IEnergyInfoHandler() {

            @Override
            public long getEnergy() {
                return 1;
            }

            @Override
            public long getMaxEnergy() {
                return 1;
            }
        };

        long getEnergy();

        long getMaxEnergy();
    }
}
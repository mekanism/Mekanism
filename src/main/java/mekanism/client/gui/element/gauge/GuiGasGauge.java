package mekanism.client.gui.element.gauge;

import java.util.List;
import java.util.function.Supplier;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.PacketDropperUse.TankType;

public class GuiGasGauge extends GuiChemicalGauge<Gas, GasStack, IGasTank> {

    public GuiGasGauge(ITankInfoHandler<IGasTank> handler, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        super(handler, type, gui, x, y, sizeX, sizeY, TankType.GAS_TANK);
    }

    public GuiGasGauge(Supplier<IGasTank> tankSupplier, Supplier<List<IGasTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y) {
        super(tankSupplier, tanksSupplier, type, gui, x, y, TankType.GAS_TANK);
    }

    public GuiGasGauge(Supplier<IGasTank> tankSupplier, Supplier<List<IGasTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        super(tankSupplier, tanksSupplier, type, gui, x, y, sizeX, sizeY, TankType.GAS_TANK);
    }

    public static GuiGasGauge getDummy(GaugeType type, IGuiWrapper gui, int x, int y) {
        GuiGasGauge gauge = new GuiGasGauge(null, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
        gauge.dummy = true;
        return gauge;
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.GAS;
    }
}
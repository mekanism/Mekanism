package mekanism.client.gui.element.gauge;

import java.util.List;
import java.util.function.Supplier;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.network.PacketDropperUse.TankType;

public class GuiPigmentGauge extends GuiChemicalGauge<Pigment, PigmentStack, IPigmentTank> {

    public GuiPigmentGauge(ITankInfoHandler<IPigmentTank> handler, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        super(handler, type, gui, x, y, sizeX, sizeY, TankType.PIGMENT_TANK);
    }

    public GuiPigmentGauge(Supplier<IPigmentTank> tankSupplier, Supplier<List<IPigmentTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y) {
        super(tankSupplier, tanksSupplier, type, gui, x, y, TankType.PIGMENT_TANK);
    }

    public GuiPigmentGauge(Supplier<IPigmentTank> tankSupplier, Supplier<List<IPigmentTank>> tanksSupplier, GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY) {
        super(tankSupplier, tanksSupplier, type, gui, x, y, sizeX, sizeY, TankType.PIGMENT_TANK);
    }

    public static GuiPigmentGauge getDummy(GaugeType type, IGuiWrapper gui, int x, int y) {
        GuiPigmentGauge gauge = new GuiPigmentGauge(null, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
        gauge.dummy = true;
        return gauge;
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.PIGMENT;
    }
}
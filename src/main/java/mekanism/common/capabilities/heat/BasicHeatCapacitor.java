package mekanism.common.capabilities.heat;

import mekanism.api.NBTConstants;
import mekanism.api.heat.HeatPacket;
import mekanism.api.heat.HeatPacket.Transfer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

public class BasicHeatCapacitor implements IHeatCapacitor {

    private final double heatCapacity;
    private final double conductionCoefficient;
    private final double insulationCoefficient;

    private boolean absorbHeat;
    private boolean emitHeat;

    protected FloatingLong storedHeat = FloatingLong.ZERO;
    protected HeatPacket heatToHandle;

    public BasicHeatCapacitor(double heatCapacity, double conductionCoefficient, double insulationCoefficient, boolean absorbHeat, boolean emitHeat) {
        this.heatCapacity = heatCapacity;
        this.conductionCoefficient = conductionCoefficient;
        this.insulationCoefficient = insulationCoefficient;
        this.absorbHeat = absorbHeat;
        this.emitHeat = emitHeat;
    }

    @Override
    public FloatingLong getTemperature() {
        return storedHeat.divide(heatCapacity);
    }

    @Override
    public double getInverseConductionCoefficient() {
        return conductionCoefficient;
    }

    @Override
    public void handleHeatChange(HeatPacket transfer) {
        heatToHandle = transfer;
    }

    public void update() {
        if (heatToHandle != null) {
            if (heatToHandle.getType() == Transfer.ABSORB && absorbHeat) {
                storedHeat = storedHeat.add(heatToHandle.getAmount().divide(heatCapacity));
            } else if (heatToHandle.getType() == Transfer.EMIT && emitHeat) {
                storedHeat = storedHeat.subtract(heatToHandle.getAmount().divide(heatCapacity));
            }
        }
        // TODO run heat emission simulation if emitHeat = true?
    }

    public double getInsulationCoefficient() {
        return insulationCoefficient;
    }

    public double getHeatCapacity() {
        return heatCapacity;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        NBTUtils.setFloatingLongIfPresent(nbt, NBTConstants.STORED, (heat) -> storedHeat = heat);
    }
}

package mekanism.common.capabilities.heat;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.heat.HeatPacket;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BasicHeatCapacitor implements IHeatCapacitor {

    private final FloatingLong heatCapacity;
    private final FloatingLong conductionCoefficient;
    private final FloatingLong insulationCoefficient;

    private boolean absorbHeat;
    private boolean emitHeat;

    protected FloatingLong storedHeat = FloatingLong.ZERO;
    @Nullable
    protected HeatPacket heatToHandle;

    public BasicHeatCapacitor(FloatingLong heatCapacity, FloatingLong conductionCoefficient, FloatingLong insulationCoefficient, boolean absorbHeat, boolean emitHeat) {
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
    public FloatingLong getInverseConductionCoefficient() {
        return conductionCoefficient;
    }

    @Override
    public void handleHeat(HeatPacket transfer) {
        heatToHandle = transfer;
    }

    public void update() {
        if (heatToHandle != null) {
            if (heatToHandle.getType().absorb() && absorbHeat) {
                storedHeat = storedHeat.add(heatToHandle.getAmount().divide(heatCapacity));
            } else if (heatToHandle.getType().emit() && emitHeat) {
                storedHeat = storedHeat.subtract(heatToHandle.getAmount().divide(heatCapacity));
            }
        }
        // TODO run heat emission simulation if emitHeat = true?
    }

    @Override
    public FloatingLong getInsulationCoefficient() {
        return insulationCoefficient;
    }

    @Override
    public FloatingLong getHeatCapacity() {
        return heatCapacity;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        NBTUtils.setFloatingLongIfPresent(nbt, NBTConstants.STORED, heat -> storedHeat = heat);
    }

    // getters/setters for container syncing
    public FloatingLong getHeat() {
        return storedHeat;
    }

    public void setHeat(FloatingLong heat) {
        storedHeat = heat;
    }
}

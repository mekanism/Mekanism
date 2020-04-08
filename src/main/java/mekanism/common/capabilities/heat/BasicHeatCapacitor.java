package mekanism.common.capabilities.heat;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.TemperaturePacket;
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
    protected TemperaturePacket heatToHandle;

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
    public void handleTemperatureChange(TemperaturePacket transfer) {
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
}

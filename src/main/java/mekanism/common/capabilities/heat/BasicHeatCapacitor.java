package mekanism.common.capabilities.heat;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.heat.HeatPacket;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BasicHeatCapacitor implements IHeatCapacitor {

    @Nullable
    private final IMekanismHeatHandler heatHandler;

    private final FloatingLong heatCapacity;
    private final FloatingLong inverseConductionCoefficient;
    private final FloatingLong insulationCoefficient;

    private boolean absorbHeat;
    private boolean emitHeat;

    protected FloatingLong storedHeat = FloatingLong.ZERO;
    @Nullable
    protected HeatPacket heatToHandle;

    // double helper
    public static BasicHeatCapacitor create(double heatCapacity, double inverseInductionCoefficient, double insulationCoefficient, @Nullable IMekanismHeatHandler heatHandler) {
        return create(FloatingLong.create(heatCapacity), FloatingLong.create(inverseInductionCoefficient), FloatingLong.create(insulationCoefficient), heatHandler);
    }

    public static BasicHeatCapacitor create(FloatingLong heatCapacity, FloatingLong inverseInductionCoefficient, FloatingLong insulationCoefficient, @Nullable IMekanismHeatHandler heatHandler) {
        return new BasicHeatCapacitor(heatCapacity, inverseInductionCoefficient, insulationCoefficient, true, true, heatHandler);
    }

    public BasicHeatCapacitor(FloatingLong heatCapacity, FloatingLong inverseConductionCoefficient, FloatingLong insulationCoefficient, boolean absorbHeat, boolean emitHeat, @Nullable IMekanismHeatHandler heatHandler) {
        this.heatCapacity = heatCapacity;
        this.inverseConductionCoefficient = inverseConductionCoefficient;
        this.insulationCoefficient = insulationCoefficient;
        this.absorbHeat = absorbHeat;
        this.emitHeat = emitHeat;
        this.heatHandler = heatHandler;
    }

    @Override
    public FloatingLong getTemperature() {
        return storedHeat.divide(heatCapacity);
    }

    @Override
    public FloatingLong getInverseConduction() {
        return inverseConductionCoefficient;
    }

    @Override
    public FloatingLong getInverseInsulation() {
        return insulationCoefficient;
    }

    @Override
    public FloatingLong getHeatCapacity() {
        return heatCapacity;
    }

    @Override
    public void handleHeat(HeatPacket transfer) {
        heatToHandle = transfer;
    }

    public FloatingLong update() {
        if (heatToHandle != null) {
            if (heatToHandle.getType().absorb() && absorbHeat) {
                storedHeat = storedHeat.plusEqual(heatToHandle.getAmount().divide(heatCapacity));
            } else if (heatToHandle.getType().emit() && emitHeat) {
                storedHeat = storedHeat.minusEqual(heatToHandle.getAmount().divide(heatCapacity));
            }
        }
        // reset our heat
        heatToHandle = null;
        // notify listeners
        if (heatHandler != null) {
            heatHandler.onContentsChanged();
        }
        return storedHeat.copy();
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

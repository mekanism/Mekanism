package mekanism.common.capabilities.heat;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.heat.HeatAPI;
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
    private final FloatingLong inverseInsulationCoefficient;

    private boolean absorbHeat;
    private boolean emitHeat;

    protected FloatingLong storedHeat = FloatingLong.ZERO;
    @Nullable
    protected HeatPacket heatToHandle;

    public static BasicHeatCapacitor create(FloatingLong heatCapacity, @Nullable IMekanismHeatHandler heatHandler) {
        return create(heatCapacity, HeatAPI.DEFAULT_INVERSE_CONDUCTION, HeatAPI.DEFAULT_INVERSE_INSULATION, heatHandler);
    }

    // double helper
    public static BasicHeatCapacitor create(double heatCapacity, double inverseConductionCoefficient, double inverseInsulationCoefficient, @Nullable IMekanismHeatHandler heatHandler) {
        return create(FloatingLong.create(heatCapacity), FloatingLong.create(inverseConductionCoefficient), FloatingLong.create(inverseInsulationCoefficient), heatHandler);
    }

    public static BasicHeatCapacitor create(FloatingLong heatCapacity, FloatingLong inverseInductionCoefficient, FloatingLong inverseInsulationCoefficient, @Nullable IMekanismHeatHandler heatHandler) {
        return create(heatCapacity, inverseInductionCoefficient, inverseInsulationCoefficient, true, true, heatHandler);
    }

    public static BasicHeatCapacitor create(FloatingLong heatCapacity, FloatingLong inverseInductionCoefficient, FloatingLong inverseInsulationCoefficient, boolean absorbHeat, boolean emitHeat,
          @Nullable IMekanismHeatHandler heatHandler) {
        // TODO validation
        return new BasicHeatCapacitor(heatCapacity, inverseInductionCoefficient, inverseInsulationCoefficient, absorbHeat, emitHeat, heatHandler);
    }

    protected BasicHeatCapacitor(FloatingLong heatCapacity, FloatingLong inverseConductionCoefficient, FloatingLong inverseInsulationCoefficient, boolean absorbHeat, boolean emitHeat, @Nullable IMekanismHeatHandler heatHandler) {
        this.heatCapacity = heatCapacity;
        this.inverseConductionCoefficient = inverseConductionCoefficient;
        this.inverseInsulationCoefficient = inverseInsulationCoefficient;
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
        return inverseInsulationCoefficient;
    }

    @Override
    public FloatingLong getHeatCapacity() {
        return heatCapacity;
    }

    @Override
    public void onContentsChanged() {
        if (heatHandler != null) {
            heatHandler.onContentsChanged();
        }
    }

    @Override
    public void handleHeat(HeatPacket transfer) {
        if (heatToHandle == null) {
            heatToHandle = transfer;
        } else {
            heatToHandle.merge(transfer);
        }
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
        onContentsChanged();
        // return current heat
        return storedHeat.copy();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        NBTUtils.setFloatingLongIfPresent(nbt, NBTConstants.STORED, heat -> storedHeat = heat);
    }

    // getters/setters for container syncing
    @Override
    public FloatingLong getHeat() {
        return storedHeat;
    }

    @Override
    public void setHeat(FloatingLong heat) {
        storedHeat = heat;
    }
}

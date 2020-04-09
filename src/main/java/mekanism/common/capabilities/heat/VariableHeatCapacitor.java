package mekanism.common.capabilities.heat;

import javax.annotation.Nullable;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;

public class VariableHeatCapacitor extends BasicHeatCapacitor {

    private FloatingLongSupplier heatCapacitySupplier;
    private FloatingLongSupplier conductionCoefficientSupplier;
    private FloatingLongSupplier insulationCoefficientSupplier;

    public static VariableHeatCapacitor create(FloatingLongSupplier heatCapacitySupplier, @Nullable IMekanismHeatHandler heatHandler) {
        return create(heatCapacitySupplier, true, true, heatHandler);
    }

    public static VariableHeatCapacitor create(FloatingLongSupplier heatCapacitySupplier, boolean absorbHeat, boolean emitHeat, @Nullable IMekanismHeatHandler heatHandler) {
        return new VariableHeatCapacitor(heatCapacitySupplier, () -> HeatAPI.DEFAULT_INVERSE_CONDUCTION, () -> HeatAPI.DEFAULT_INVERSE_INSULATION, absorbHeat, emitHeat, heatHandler);
    }

    protected VariableHeatCapacitor(FloatingLongSupplier heatCapacity, FloatingLongSupplier conductionCoefficient, FloatingLongSupplier insulationCoefficient, boolean absorbHeat, boolean emitHeat, @Nullable IMekanismHeatHandler heatHandler) {
        super(heatCapacity.get(), conductionCoefficient.get(), insulationCoefficient.get(), absorbHeat, emitHeat, heatHandler);
        this.heatCapacitySupplier = heatCapacity;
        this.conductionCoefficientSupplier = conductionCoefficient;
        this.insulationCoefficientSupplier = insulationCoefficient;
    }

    @Override
    public FloatingLong getInverseConduction() {
        return conductionCoefficientSupplier != null ? conductionCoefficientSupplier.get() : super.getInverseConduction();
    }

    @Override
    public FloatingLong getInverseInsulation() {
        return insulationCoefficientSupplier != null ? insulationCoefficientSupplier.get() : super.getInverseInsulation();
    }

    @Override
    public FloatingLong getHeatCapacity() {
        return heatCapacitySupplier != null ? heatCapacitySupplier.get() : super.getHeatCapacity();
    }
}

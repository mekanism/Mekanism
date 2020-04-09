package mekanism.common.capabilities.heat;

import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;

public class VariableHeatCapacitor extends BasicHeatCapacitor {

    private FloatingLongSupplier heatCapacitySupplier;
    private FloatingLongSupplier conductionCoefficientSupplier;
    private FloatingLongSupplier insulationCoefficientSupplier;

    public static VariableHeatCapacitor create(FloatingLongSupplier heatCapacitySupplier) {
        return create(heatCapacitySupplier, true, true);
    }

    public static VariableHeatCapacitor create(FloatingLongSupplier heatCapacitySupplier, boolean absorbHeat, boolean emitHeat) {
        return new VariableHeatCapacitor(heatCapacitySupplier, null, null, absorbHeat, emitHeat);
    }

    protected VariableHeatCapacitor(FloatingLongSupplier heatCapacity, FloatingLongSupplier conductionCoefficient, FloatingLongSupplier insulationCoefficient, boolean absorbHeat, boolean emitHeat) {
        super(heatCapacity.get(), conductionCoefficient.get(), insulationCoefficient.get(), absorbHeat, emitHeat);
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

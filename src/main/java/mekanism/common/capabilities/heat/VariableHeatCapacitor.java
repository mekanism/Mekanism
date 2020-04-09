package mekanism.common.capabilities.heat;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VariableHeatCapacitor extends BasicHeatCapacitor {

    private FloatingLongSupplier conductionCoefficientSupplier;
    private FloatingLongSupplier insulationCoefficientSupplier;

    public static VariableHeatCapacitor create(FloatingLong heatCapacity, boolean absorbHeat, boolean emitHeat, @Nullable IMekanismHeatHandler heatHandler) {
        return new VariableHeatCapacitor(heatCapacity, () -> HeatAPI.DEFAULT_INVERSE_CONDUCTION, () -> HeatAPI.DEFAULT_INVERSE_INSULATION, absorbHeat, emitHeat, heatHandler);
    }

    protected VariableHeatCapacitor(FloatingLong heatCapacity, FloatingLongSupplier conductionCoefficient, FloatingLongSupplier insulationCoefficient,
          boolean absorbHeat, boolean emitHeat, @Nullable IMekanismHeatHandler heatHandler) {
        super(heatCapacity, conductionCoefficient.get(), insulationCoefficient.get(), absorbHeat, emitHeat, heatHandler);
        this.conductionCoefficientSupplier = conductionCoefficient;
        this.insulationCoefficientSupplier = insulationCoefficient;
    }

    @Override
    public FloatingLong getInverseConduction() {
        return conductionCoefficientSupplier.get().max(FloatingLong.ONE);
    }

    @Override
    public FloatingLong getInverseInsulation() {
        return insulationCoefficientSupplier.get();
    }
}

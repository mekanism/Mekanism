package mekanism.common.capabilities.heat;

import java.util.Objects;
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

    private FloatingLongSupplier heatCapacitySupplier;
    private FloatingLongSupplier conductionCoefficientSupplier;
    private FloatingLongSupplier insulationCoefficientSupplier;

    public static VariableHeatCapacitor create(FloatingLongSupplier heatCapacitySupplier, @Nullable IMekanismHeatHandler heatHandler) {
        return create(heatCapacitySupplier, true, true, heatHandler);
    }

    public static VariableHeatCapacitor create(FloatingLongSupplier heatCapacitySupplier, boolean absorbHeat, boolean emitHeat, @Nullable IMekanismHeatHandler heatHandler) {
        Objects.requireNonNull(heatCapacitySupplier, "Heat capacity supplier cannot be null");
        return new VariableHeatCapacitor(heatCapacitySupplier, () -> HeatAPI.DEFAULT_INVERSE_CONDUCTION, () -> HeatAPI.DEFAULT_INVERSE_INSULATION, absorbHeat, emitHeat, heatHandler);
    }

    protected VariableHeatCapacitor(FloatingLongSupplier heatCapacity, FloatingLongSupplier conductionCoefficient, FloatingLongSupplier insulationCoefficient,
          boolean absorbHeat, boolean emitHeat, @Nullable IMekanismHeatHandler heatHandler) {
        super(heatCapacity.get(), conductionCoefficient.get(), insulationCoefficient.get(), absorbHeat, emitHeat, heatHandler);
        this.heatCapacitySupplier = heatCapacity;
        this.conductionCoefficientSupplier = conductionCoefficient;
        this.insulationCoefficientSupplier = insulationCoefficient;
    }

    @Override
    public FloatingLong getInverseConduction() {
        return conductionCoefficientSupplier.get();
    }

    @Override
    public FloatingLong getInverseInsulation() {
        return insulationCoefficientSupplier.get();
    }

    @Override
    public FloatingLong getHeatCapacity() {
        return heatCapacitySupplier.get();
    }
}

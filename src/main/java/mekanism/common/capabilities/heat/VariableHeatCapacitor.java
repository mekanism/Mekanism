package mekanism.common.capabilities.heat;

import java.util.function.DoubleSupplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IMekanismHeatHandler;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VariableHeatCapacitor extends BasicHeatCapacitor {

    private DoubleSupplier conductionCoefficientSupplier;
    private DoubleSupplier insulationCoefficientSupplier;

    public static VariableHeatCapacitor create(double heatCapacity, boolean absorbHeat, boolean emitHeat, @Nullable IMekanismHeatHandler heatHandler) {
        return new VariableHeatCapacitor(heatCapacity, () -> HeatAPI.DEFAULT_INVERSE_CONDUCTION, () -> HeatAPI.DEFAULT_INVERSE_INSULATION, heatHandler);
    }

    protected VariableHeatCapacitor(double heatCapacity, DoubleSupplier conductionCoefficient, DoubleSupplier insulationCoefficient,
          @Nullable IMekanismHeatHandler heatHandler) {
        super(heatCapacity, conductionCoefficient.getAsDouble(), insulationCoefficient.getAsDouble(), heatHandler);
        this.conductionCoefficientSupplier = conductionCoefficient;
        this.insulationCoefficientSupplier = insulationCoefficient;
    }

    @Override
    public double getInverseConduction() {
        return Math.max(1, conductionCoefficientSupplier.getAsDouble());
    }

    @Override
    public double getInverseInsulation() {
        return insulationCoefficientSupplier.getAsDouble();
    }
}

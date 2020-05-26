package mekanism.common.capabilities.heat;

import java.util.function.DoubleSupplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.heat.HeatAPI;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VariableHeatCapacitor extends BasicHeatCapacitor {

    private final DoubleSupplier conductionCoefficientSupplier;
    private final DoubleSupplier insulationCoefficientSupplier;

    public static VariableHeatCapacitor create(double heatCapacity, boolean absorbHeat, boolean emitHeat, @Nullable IContentsListener listener) {
        return new VariableHeatCapacitor(heatCapacity, () -> HeatAPI.DEFAULT_INVERSE_CONDUCTION, () -> HeatAPI.DEFAULT_INVERSE_INSULATION, listener);
    }

    protected VariableHeatCapacitor(double heatCapacity, DoubleSupplier conductionCoefficient, DoubleSupplier insulationCoefficient,
          @Nullable IContentsListener listener) {
        super(heatCapacity, conductionCoefficient.getAsDouble(), insulationCoefficient.getAsDouble(), listener);
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

package mekanism.common.capabilities.heat;

import java.util.Objects;
import java.util.function.DoubleSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.heat.HeatAPI;
import mekanism.common.tile.prefab.TileEntityMultiblock;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockHeatCapacitor<MULTIBLOCK extends TileEntityMultiblock<?>> extends VariableHeatCapacitor {

    private final MULTIBLOCK multiblock;

    public static <MULTIBLOCK extends TileEntityMultiblock<?>> MultiblockHeatCapacitor<MULTIBLOCK> create(MULTIBLOCK multiblock, double heatCapacity) {
        return create(multiblock, heatCapacity, () -> HeatAPI.DEFAULT_INVERSE_CONDUCTION, () -> HeatAPI.DEFAULT_INVERSE_INSULATION);
    }

    public static <MULTIBLOCK extends TileEntityMultiblock<?>> MultiblockHeatCapacitor<MULTIBLOCK> create(MULTIBLOCK multiblock, double heatCapacity,
          DoubleSupplier conductionCoefficient, DoubleSupplier insulationCoefficient) {
        Objects.requireNonNull(conductionCoefficient, "Conduction coefficient supplier cannot be null");
        Objects.requireNonNull(insulationCoefficient, "Insulation coefficient supplier cannot be null");
        return new MultiblockHeatCapacitor<>(multiblock, heatCapacity, conductionCoefficient, insulationCoefficient);
    }

    protected MultiblockHeatCapacitor(MULTIBLOCK multiblock, double heatCapacity, DoubleSupplier conductionCoefficient,
          DoubleSupplier insulationCoefficient) {
        super(heatCapacity, conductionCoefficient, insulationCoefficient, null);
        this.multiblock = multiblock;
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        if (multiblock.hasWorld() && !multiblock.isRemote() && multiblock.isRendering) {
            multiblock.markDirty(false);
        }
    }
}

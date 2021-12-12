package mekanism.common.capabilities.heat;

import java.util.Objects;
import java.util.function.DoubleSupplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.heat.HeatAPI;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.prefab.TileEntityMultiblock;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockHeatCapacitor<MULTIBLOCK extends MultiblockData> extends VariableHeatCapacitor {

    protected final MULTIBLOCK multiblock;
    protected final TileEntityMultiblock<MULTIBLOCK> tile;

    public static <MULTIBLOCK extends MultiblockData> MultiblockHeatCapacitor<MULTIBLOCK> create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          double heatCapacity, @Nullable DoubleSupplier ambientTempSupplier) {
        return create(multiblock, tile, heatCapacity, () -> HeatAPI.DEFAULT_INVERSE_CONDUCTION, () -> HeatAPI.DEFAULT_INVERSE_INSULATION, ambientTempSupplier);
    }

    public static <MULTIBLOCK extends MultiblockData> MultiblockHeatCapacitor<MULTIBLOCK> create(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile,
          double heatCapacity, DoubleSupplier conductionCoefficient, DoubleSupplier insulationCoefficient, @Nullable DoubleSupplier ambientTempSupplier) {
        Objects.requireNonNull(conductionCoefficient, "Conduction coefficient supplier cannot be null");
        Objects.requireNonNull(insulationCoefficient, "Insulation coefficient supplier cannot be null");
        return new MultiblockHeatCapacitor<>(multiblock, tile, heatCapacity, conductionCoefficient, insulationCoefficient, ambientTempSupplier);
    }

    protected MultiblockHeatCapacitor(MULTIBLOCK multiblock, TileEntityMultiblock<MULTIBLOCK> tile, double heatCapacity, DoubleSupplier conductionCoefficient,
          DoubleSupplier insulationCoefficient, @Nullable DoubleSupplier ambientTempSupplier) {
        super(heatCapacity, conductionCoefficient, insulationCoefficient, ambientTempSupplier, null);
        this.multiblock = multiblock;
        this.tile = tile;
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        if (tile.hasLevel() && !tile.isRemote()) {
            tile.markDirty(false);
        }
    }
}

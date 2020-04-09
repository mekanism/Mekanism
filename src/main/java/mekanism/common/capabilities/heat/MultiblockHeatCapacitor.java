package mekanism.common.capabilities.heat;

import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.tile.TileEntityMultiblock;

public class MultiblockHeatCapacitor<MULTIBLOCK extends TileEntityMultiblock<?>> extends VariableHeatCapacitor {

    private MULTIBLOCK multiblock;

    public static <MULTIBLOCK extends TileEntityMultiblock<?>> MultiblockHeatCapacitor<MULTIBLOCK> create(MULTIBLOCK multiblock, FloatingLongSupplier heatCapacity) {
        return create(multiblock, heatCapacity, () -> HeatAPI.DEFAULT_INVERSE_CONDUCTION, () -> HeatAPI.DEFAULT_INVERSE_INSULATION, true, true, null);
    }

    public static <MULTIBLOCK extends TileEntityMultiblock<?>> MultiblockHeatCapacitor<MULTIBLOCK> create(MULTIBLOCK multiblock, FloatingLongSupplier heatCapacity, FloatingLongSupplier conductionCoefficient,
          FloatingLongSupplier insulationCoefficient, boolean absorbHeat, boolean emitHeat, IMekanismHeatHandler heatHandler) {
        return new MultiblockHeatCapacitor<>(multiblock, heatCapacity, conductionCoefficient, insulationCoefficient, absorbHeat, emitHeat, heatHandler);
    }

    protected MultiblockHeatCapacitor(MULTIBLOCK multiblock, FloatingLongSupplier heatCapacity, FloatingLongSupplier conductionCoefficient, FloatingLongSupplier insulationCoefficient, boolean absorbHeat, boolean emitHeat,
          IMekanismHeatHandler heatHandler) {
        super(heatCapacity, conductionCoefficient, insulationCoefficient, absorbHeat, emitHeat, heatHandler);
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

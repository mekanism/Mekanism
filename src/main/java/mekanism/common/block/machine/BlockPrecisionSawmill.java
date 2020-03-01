package mekanism.common.block.machine;

import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityPrecisionSawmill;

public class BlockPrecisionSawmill extends BlockFactoryMachine<TileEntityPrecisionSawmill> {

    public BlockPrecisionSawmill() {
        super(MekanismMachines.PRECISION_SAWMILL);
    }
}
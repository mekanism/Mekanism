package mekanism.common.block.machine;

import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityCombiner;

public class BlockCombiner extends BlockMachine<TileEntityCombiner> {

    public BlockCombiner() {
        super(MekanismMachines.COMBINER);
    }
}
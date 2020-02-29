package mekanism.common.block.machine;

import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityCrusher;

public class BlockCrusher extends BlockMachine<TileEntityCrusher> {

    public BlockCrusher() {
        super(MekanismMachines.CRUSHER);
    }
}
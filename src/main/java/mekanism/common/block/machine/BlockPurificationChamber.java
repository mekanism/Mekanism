package mekanism.common.block.machine;

import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityPurificationChamber;

public class BlockPurificationChamber extends BlockMachine<TileEntityPurificationChamber> {

    public BlockPurificationChamber() {
        super(MekanismMachines.PURIFICATION_CHAMBER);
    }
}
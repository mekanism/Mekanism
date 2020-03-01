package mekanism.common.block.machine;

import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityPurificationChamber;

public class BlockPurificationChamber extends BlockFactoryMachine<TileEntityPurificationChamber> {

    public BlockPurificationChamber() {
        super(MekanismMachines.PURIFICATION_CHAMBER);
    }
}
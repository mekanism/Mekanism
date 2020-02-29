package mekanism.common.block.machine;

import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityEnergizedSmelter;

public class BlockEnergizedSmelter extends BlockFactoryMachine<TileEntityEnergizedSmelter> {

    public BlockEnergizedSmelter() {
        super(MekanismMachines.ENERGIZED_SMELTER);
    }
}
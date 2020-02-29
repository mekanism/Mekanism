package mekanism.common.block.machine;

import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityOsmiumCompressor;

public class BlockOsmiumCompressor extends BlockFactoryMachine<TileEntityOsmiumCompressor> {

    public BlockOsmiumCompressor() {
        super(MekanismMachines.OSMIUM_COMPRESSOR);
    }
}
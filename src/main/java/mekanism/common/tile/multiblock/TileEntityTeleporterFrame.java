package mekanism.common.tile.multiblock;

import mekanism.common.Mekanism;
import mekanism.common.content.teleporter.TeleporterMultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityMultiblock;

public class TileEntityTeleporterFrame  extends TileEntityMultiblock<TeleporterMultiblockData> {

    public TileEntityTeleporterFrame() {
        super(MekanismBlocks.TELEPORTER_FRAME);
    }

    @Override
    public TeleporterMultiblockData createMultiblock() {
        return new TeleporterMultiblockData(this);
    }

    @Override
    public MultiblockManager<TeleporterMultiblockData> getManager() {
        return Mekanism.teleporterManager;
    }
}

package mekanism.common.tile.qio;

import mekanism.common.registries.MekanismBlocks;

public class TileEntityQIOExporter extends TileEntityQIOFilterHandler {

    public TileEntityQIOExporter() {
        super(MekanismBlocks.QIO_EXPORTER);
    }

    @Override
    public void onUpdateServer() {
        super.onUpdateServer();
    }
}

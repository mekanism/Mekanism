package mekanism.common.tile.qio;

import mekanism.common.registries.MekanismBlocks;

public class TileEntityQIOImporter extends TileEntityQIOFilterHandler {

    public TileEntityQIOImporter() {
        super(MekanismBlocks.QIO_IMPORTER);
    }

    @Override
    public void onUpdateServer() {
        super.onUpdateServer();
    }
}

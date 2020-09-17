package mekanism.common.tile.qio;

import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.registries.MekanismBlocks;

public class TileEntityQIODashboard extends TileEntityQIOComponent {

    public TileEntityQIODashboard() {
        super(MekanismBlocks.QIO_DASHBOARD);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (world.getGameTime() % 10 == 0) {
            QIOFrequency frequency = getQIOFrequency();
            setActive(frequency != null);
        }
    }
}
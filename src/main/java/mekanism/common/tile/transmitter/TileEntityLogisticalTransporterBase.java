package mekanism.common.tile.transmitter;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;

public abstract class TileEntityLogisticalTransporterBase extends TileEntityTransmitter {

    protected TileEntityLogisticalTransporterBase(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected abstract LogisticalTransporterBase createTransmitter(IBlockProvider blockProvider);

    @Override
    public LogisticalTransporterBase getTransmitter() {
        return (LogisticalTransporterBase) super.getTransmitter();
    }

    @Override
    public void tick() {
        super.tick();
        getTransmitter().tick();
    }
}
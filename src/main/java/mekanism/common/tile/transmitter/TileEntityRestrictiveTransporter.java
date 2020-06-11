package mekanism.common.tile.transmitter;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.content.network.transmitter.RestrictiveTransporter;
import mekanism.common.registries.MekanismBlocks;

public class TileEntityRestrictiveTransporter extends TileEntityLogisticalTransporterBase {

    public TileEntityRestrictiveTransporter() {
        super(MekanismBlocks.RESTRICTIVE_TRANSPORTER);
    }

    @Override
    protected RestrictiveTransporter createTransmitter(IBlockProvider blockProvider) {
        return new RestrictiveTransporter(this);
    }

    @Override
    public RestrictiveTransporter getTransmitter() {
        return (RestrictiveTransporter) super.getTransmitter();
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.RESTRICTIVE_TRANSPORTER;
    }
}
package mekanism.common.content.network.transmitter;

import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;

public class RestrictiveTransporter extends LogisticalTransporterBase {

    public RestrictiveTransporter(TileEntityTransmitter tile) {
        super(tile, TransporterTier.BASIC);
    }

    @Override
    public double getCost() {
        return 1_000;
    }
}
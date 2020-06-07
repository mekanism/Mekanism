package mekanism.common.tile.transmitter;

import mekanism.common.block.states.TransmitterType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.TransporterTier;

public class TileEntityRestrictiveTransporter extends TileEntityLogisticalTransporterBase {

    public TileEntityRestrictiveTransporter() {
        super(MekanismBlocks.RESTRICTIVE_TRANSPORTER, TransporterTier.BASIC);
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.RESTRICTIVE_TRANSPORTER;
    }

    @Override
    public double getCost() {
        return 1_000;
    }
}
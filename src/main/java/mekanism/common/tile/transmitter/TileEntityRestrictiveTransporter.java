package mekanism.common.tile.transmitter;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.content.network.transmitter.RestrictiveTransporter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityRestrictiveTransporter extends TileEntityLogisticalTransporterBase {

    public TileEntityRestrictiveTransporter(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
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
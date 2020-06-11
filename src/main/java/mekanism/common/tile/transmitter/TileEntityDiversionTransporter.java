package mekanism.common.tile.transmitter;

import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.content.network.transmitter.DiversionTransporter;
import mekanism.common.registries.MekanismBlocks;

public class TileEntityDiversionTransporter extends TileEntityLogisticalTransporterBase {

    public TileEntityDiversionTransporter() {
        super(MekanismBlocks.DIVERSION_TRANSPORTER);
    }

    @Override
    protected DiversionTransporter createTransmitter(IBlockProvider blockProvider) {
        return new DiversionTransporter(this);
    }

    @Override
    public DiversionTransporter getTransmitter() {
        return (DiversionTransporter) super.getTransmitter();
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.DIVERSION_TRANSPORTER;
    }

    @Nonnull
    @Override
    protected TransmitterModelData initModelData() {
        return new TransmitterModelData.Diversion();
    }
}
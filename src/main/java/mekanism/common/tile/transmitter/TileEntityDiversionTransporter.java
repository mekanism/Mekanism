package mekanism.common.tile.transmitter;

import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.Mekanism;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.content.network.transmitter.DiversionTransporter;
import mekanism.common.content.network.transmitter.DiversionTransporter.DiversionControl;
import mekanism.common.integration.computer.ComputerCapabilityHelper;
import mekanism.common.integration.computer.IComputerTile;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.util.Direction;

public class TileEntityDiversionTransporter extends TileEntityLogisticalTransporterBase implements IComputerTile {

    public TileEntityDiversionTransporter() {
        super(MekanismBlocks.DIVERSION_TRANSPORTER);
        if (Mekanism.hooks.computerCompatEnabled()) {
            ComputerCapabilityHelper.addComputerCapabilities(this, this::addCapabilityResolver);
        }
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

    //Methods relating to IComputerTile
    @Override
    public String getComputerName() {
        return "diversionTransporter";
    }

    @ComputerMethod
    private DiversionControl getMode(Direction side) {
        return getTransmitter().modes[side.ordinal()];
    }

    @ComputerMethod
    private void setMode(Direction side, DiversionControl mode) {
        getTransmitter().updateMode(side, mode);
    }

    @ComputerMethod
    private void incrementMode(Direction side) {
        DiversionTransporter transmitter = getTransmitter();
        transmitter.updateMode(side, transmitter.modes[side.ordinal()].getNext());
    }

    @ComputerMethod
    private void decrementMode(Direction side) {
        DiversionTransporter transmitter = getTransmitter();
        transmitter.updateMode(side, transmitter.modes[side.ordinal()].getPrevious());
    }
    //End methods IComputerTile
}
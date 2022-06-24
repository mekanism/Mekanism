package mekanism.common.tile.transmitter;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.util.TransporterUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityLogisticalTransporterBase extends TileEntityTransmitter {

    protected TileEntityLogisticalTransporterBase(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    protected abstract LogisticalTransporterBase createTransmitter(IBlockProvider blockProvider);

    @Override
    public LogisticalTransporterBase getTransmitter() {
        return (LogisticalTransporterBase) super.getTransmitter();
    }

    public static void tickClient(Level level, BlockPos pos, BlockState state, TileEntityLogisticalTransporterBase transmitter) {
        transmitter.getTransmitter().onUpdateClient();
    }

    @Override
    public void onUpdateServer() {
        super.onUpdateServer();
        getTransmitter().onUpdateServer();
    }

    @Override
    public void blockRemoved() {
        super.blockRemoved();
        if (!isRemote()) {
            LogisticalTransporterBase transporter = getTransmitter();
            if (!transporter.isUpgrading()) {
                //If the transporter is not currently being upgraded, drop the contents
                for (TransporterStack stack : transporter.getTransit()) {
                    TransporterUtils.drop(transporter, stack);
                }
            }
        }
    }
}
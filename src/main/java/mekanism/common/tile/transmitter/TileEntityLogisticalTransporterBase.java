package mekanism.common.tile.transmitter;

import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.item.TransporterCapabilityResolver;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityLogisticalTransporterBase extends TileEntityTransmitter {

    protected TileEntityLogisticalTransporterBase(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        addCapabilityResolver(new TransporterCapabilityResolver(getTransmitter()));
    }

    @Override
    protected abstract LogisticalTransporterBase createTransmitter(Holder<Block> blockProvider);

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
    public void preRemoveSideEffects(@NotNull BlockPos pos, @NotNull BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (!isRemote()) {
            LogisticalTransporterBase transporter = getTransmitter();
            //TODO - 26.1: Evaluate overriding Block#shouldChangedStateKeepBlockEntity to make it so that upgrading lets the entity persist?
            // Also evaluate if there are any other cases where we might want to override that method
            if (!transporter.isUpgrading()) {
                //If the transporter is not currently being upgraded, drop the contents
                //Note: Protect against the block being broken by an auto clicker that might have already checked if it can extract energy
                try (Transaction transaction = MekanismUtils.openTransactionSafe()) {
                    for (TransporterStack stack : transporter.getTransit()) {
                        transporter.drop(stack, transaction);
                    }
                    transaction.commit();
                }
            }
        }
    }

    @Override
    public void sideChanged(@NotNull Direction side, @NotNull ConnectionType old, @NotNull ConnectionType type) {
        super.sideChanged(side, old, type);
        //Note: We don't expose a cap for when the connection type is none or push and this method only gets called if type != old,
        // so we can check to ensure that if we are one of the two that the other isn't the other one we don't have a cap for
        if (type == ConnectionType.NONE && old != ConnectionType.PUSH || type == ConnectionType.PUSH && old != ConnectionType.NONE) {
            //We no longer have a capability, invalidate it, which will also notify the level
            invalidateCapability(Capabilities.ITEM.block(), side);
        } else if (old == ConnectionType.NONE && type != ConnectionType.PUSH || old == ConnectionType.PUSH && type != ConnectionType.NONE) {
            //Notify any listeners to our position that we now do have a capability
            //Note: We don't invalidate our impls because we know they are already invalid, so we can short circuit setting them to null from null
            invalidateCapabilities();
        }
    }
}
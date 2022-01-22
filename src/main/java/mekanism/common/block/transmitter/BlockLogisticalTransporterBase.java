package mekanism.common.block.transmitter;

import javax.annotation.Nonnull;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockLogisticalTransporterBase<TILE extends TileEntityLogisticalTransporterBase> extends BlockLargeTransmitter implements IHasTileEntity<TILE> {

    @Override
    @Deprecated
    public void onRemove(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!world.isClientSide && !state.is(newState.getBlock())) {
            //If the block changed (so not just becoming fluid logged)
            TileEntityLogisticalTransporterBase tile = WorldUtils.getTileEntity(TileEntityLogisticalTransporterBase.class, world, pos, true);
            if (tile != null) {
                LogisticalTransporterBase transporter = tile.getTransmitter();
                if (!transporter.isUpgrading()) {
                    //If the transporter is not currently being upgraded, drop the contents
                    for (TransporterStack stack : transporter.getTransit()) {
                        TransporterUtils.drop(transporter, stack);
                    }
                }
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }
}
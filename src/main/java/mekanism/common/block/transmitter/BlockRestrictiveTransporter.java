package mekanism.common.block.transmitter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.tile.transmitter.logistical_transporter.TileEntityRestrictiveTransporter;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockRestrictiveTransporter extends BlockLargeTransmitter implements IHasTileEntity<TileEntityRestrictiveTransporter> {

    public BlockRestrictiveTransporter() {
        super("restrictive_transporter");
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull BlockState state) {
        return new TileEntityRestrictiveTransporter();
    }

    @Nullable
    @Override
    public Class<? extends TileEntityRestrictiveTransporter> getTileClass() {
        return TileEntityRestrictiveTransporter.class;
    }
}
package mekanism.common.block.transmitter;

import javax.annotation.Nonnull;
import mekanism.common.tile.transmitter.TileEntityRestrictiveTransporter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockRestrictiveTransporter extends BlockLargeTransmitter {

    public BlockRestrictiveTransporter() {
        super("restrictive_transporter");
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityRestrictiveTransporter();
    }
}
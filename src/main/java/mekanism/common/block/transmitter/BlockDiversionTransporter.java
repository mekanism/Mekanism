package mekanism.common.block.transmitter;

import javax.annotation.Nonnull;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;

public class BlockDiversionTransporter extends BlockLargeTransmitter {

    public BlockDiversionTransporter() {
        super("diversion_transporter");
    }

    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileEntityDiversionTransporter();
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.CUTOUT;
    }
}
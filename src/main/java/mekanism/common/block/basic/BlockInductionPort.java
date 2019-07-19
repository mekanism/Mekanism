package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.tile.TileEntityInductionPort;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockInductionPort extends BlockBasicMultiblock {

    public BlockInductionPort() {
        super("induction_port");
    }

    @Override
    public boolean hasActiveTexture() {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityInductionPort();
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState blockState) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IComparatorSupport) {
            return ((IComparatorSupport) tile).getRedstoneLevel();
        }
        return 0;
    }
}
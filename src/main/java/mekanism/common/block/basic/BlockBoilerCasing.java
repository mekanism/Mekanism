package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBoilerCasing extends BlockBasicMultiblock {

    public BlockBoilerCasing() {
        super("boiler_casing");
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityBoilerCasing();
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return 0;
    }
}
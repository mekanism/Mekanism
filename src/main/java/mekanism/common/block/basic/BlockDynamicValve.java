package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.tile.TileEntityDynamicValve;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockDynamicValve extends BlockBasicMultiblock implements IHasModel {

    public BlockDynamicValve() {
        super("dynamic_valve");
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityDynamicValve();
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
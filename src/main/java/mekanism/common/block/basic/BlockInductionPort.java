package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.base.IActiveState;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateActive;
import mekanism.common.tile.TileEntityInductionPort;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockInductionPort extends BlockBasicMultiblock implements IStateActive, IHasInventory, IHasTileEntity<TileEntityInductionPort> {

    public BlockInductionPort() {
        super("induction_port");
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
        if (tileEntity instanceof IActiveState) {
            if (((IActiveState) tileEntity).getActive() && ((IActiveState) tileEntity).lightUpdate()) {
                return 15;
            }
        }
        return super.getLightValue(state, world, pos);
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return BlockStateHelper.getBlockState(this);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        //TODO
        return 0;
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        return BlockStateHelper.getActualState(this, state, MekanismUtils.getTileEntitySafe(world, pos));
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
        return ((TileEntityInductionPort) world.getTileEntity(pos)).getRedstoneLevel();
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityInductionPort> getTileClass() {
        return TileEntityInductionPort.class;
    }
}
package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.tile.TileEntityDynamicValve;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockDynamicValve extends BlockBasicMultiblock implements IHasModel, IHasInventory, IHasTileEntity<TileEntityDynamicValve> {

    public BlockDynamicValve() {
        super("dynamic_valve");
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        return new TileEntityDynamicValve();
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState blockState) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
        return ((TileEntityDynamicValve) world.getTileEntity(pos)).getRedstoneLevel();
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityDynamicValve> getTileClass() {
        return TileEntityDynamicValve.class;
    }
}
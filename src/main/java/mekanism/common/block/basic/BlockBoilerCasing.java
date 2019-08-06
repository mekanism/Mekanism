package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockBoilerCasing extends BlockBasicMultiblock implements IHasInventory, IHasTileEntity<TileEntityBoilerCasing> {

    public BlockBoilerCasing() {
        super("boiler_casing");
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull BlockState state) {
        return new TileEntityBoilerCasing();
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public int getLightOpacity(BlockState state, IWorldReader world, BlockPos pos) {
        return 0;
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityBoilerCasing> getTileClass() {
        return TileEntityBoilerCasing.class;
    }
}
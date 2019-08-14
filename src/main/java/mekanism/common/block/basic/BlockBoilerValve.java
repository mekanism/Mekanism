package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ISupportsComparator;
import mekanism.common.tile.TileEntityBoilerValve;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BlockBoilerValve extends BlockBasicMultiblock implements IHasInventory, IHasTileEntity<TileEntityBoilerValve>, ISupportsComparator {

    public BlockBoilerValve() {
        super("boiler_valve");
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        return new TileEntityBoilerValve();
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityBoilerValve> getTileClass() {
        return TileEntityBoilerValve.class;
    }
}
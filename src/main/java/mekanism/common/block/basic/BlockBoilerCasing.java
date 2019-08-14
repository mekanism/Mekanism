package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BlockBoilerCasing extends BlockBasicMultiblock implements IHasInventory, IHasTileEntity<TileEntityBoilerCasing> {

    public BlockBoilerCasing() {
        super("boiler_casing");
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        return new TileEntityBoilerCasing();
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
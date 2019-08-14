package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ISupportsComparator;
import mekanism.common.tile.TileEntityDynamicValve;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BlockDynamicValve extends BlockBasicMultiblock implements IHasModel, IHasInventory, IHasTileEntity<TileEntityDynamicValve>, ISupportsComparator {

    public BlockDynamicValve() {
        super("dynamic_valve");
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        return new TileEntityDynamicValve();
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
package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ISupportsComparator;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.fluid.DynamicTankContainer;
import mekanism.common.tile.TileEntityDynamicValve;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BlockDynamicValve extends BlockBasicMultiblock implements IHasModel, IHasInventory, IHasTileEntity<TileEntityDynamicValve>, ISupportsComparator, IHasGui<TileEntityDynamicValve> {

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

    @Override
    public INamedContainerProvider getProvider(TileEntityDynamicValve tile) {
        return new ContainerProvider("mekanism.container.dynamic_tank", (i, inv, player) -> new DynamicTankContainer(i, inv, tile));
    }
}
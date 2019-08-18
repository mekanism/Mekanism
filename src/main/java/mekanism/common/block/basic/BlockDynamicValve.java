package mekanism.common.block.basic;

import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ISupportsComparator;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.fluid.DynamicTankContainer;
import mekanism.common.tile.TileEntityDynamicValve;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;

public class BlockDynamicValve extends BlockBasicMultiblock implements IHasModel, IHasInventory, IHasTileEntity<TileEntityDynamicValve>, ISupportsComparator, IHasGui<TileEntityDynamicValve> {

    public BlockDynamicValve() {
        super("dynamic_valve");
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityDynamicValve tile) {
        return new ContainerProvider("mekanism.container.dynamic_tank", (i, inv, player) -> new DynamicTankContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityDynamicValve> getTileType() {
        return MekanismTileEntityTypes.DYNAMIC_VALVE;
    }
}
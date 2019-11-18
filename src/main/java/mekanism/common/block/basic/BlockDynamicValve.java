package mekanism.common.block.basic;

import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.DynamicTankContainer;
import mekanism.common.tile.TileEntityDynamicValve;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;

public class BlockDynamicValve extends BlockBasicMultiblock implements IHasModel, IHasInventory, IHasTileEntity<TileEntityDynamicValve>, ISupportsComparator, IHasGui<TileEntityDynamicValve> {

    @Override
    public INamedContainerProvider getProvider(TileEntityDynamicValve tile) {
        return new ContainerProvider("mekanism.container.dynamic_tank", (i, inv, player) -> new DynamicTankContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityDynamicValve> getTileType() {
        return MekanismTileEntityTypes.DYNAMIC_VALVE.getTileEntityType();
    }
}
package mekanism.common.block.basic;

import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.DynamicTankContainer;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;

public class BlockDynamicTank extends BlockBasicMultiblock implements IHasModel, IHasTileEntity<TileEntityDynamicTank>, IHasGui<TileEntityDynamicTank> {

    @Override
    public INamedContainerProvider getProvider(TileEntityDynamicTank tile) {
        return new ContainerProvider("mekanism.container.dynamic_tank", (i, inv, player) -> new DynamicTankContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityDynamicTank> getTileType() {
        return MekanismTileEntityTypes.DYNAMIC_TANK.getTileEntityType();
    }
}
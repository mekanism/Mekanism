package mekanism.common.block.basic;

import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.fluid.DynamicTankContainer;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;

public class BlockDynamicTank extends BlockBasicMultiblock implements IHasModel, IHasInventory, IHasTileEntity<TileEntityDynamicTank>, IHasGui<TileEntityDynamicTank> {

    public BlockDynamicTank() {
        super("dynamic_tank");
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityDynamicTank tile) {
        return new ContainerProvider("mekanism.container.dynamic_tank", (i, inv, player) -> new DynamicTankContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityDynamicTank> getTileType() {
        return MekanismTileEntityTypes.DYNAMIC_TANK;
    }
}
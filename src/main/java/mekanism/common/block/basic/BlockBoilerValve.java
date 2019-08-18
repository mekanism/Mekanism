package mekanism.common.block.basic;

import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ISupportsComparator;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.ThermoelectricBoilerContainer;
import mekanism.common.tile.TileEntityBoilerValve;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;

public class BlockBoilerValve extends BlockBasicMultiblock implements IHasInventory, IHasTileEntity<TileEntityBoilerValve>, ISupportsComparator, IHasGui<TileEntityBoilerValve> {

    public BlockBoilerValve() {
        super("boiler_valve");
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityBoilerValve tile) {
        return new ContainerProvider("mekanism.container.thermoelectric_boiler", (i, inv, player) -> new ThermoelectricBoilerContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityBoilerValve> getTileType() {
        return MekanismTileEntityTypes.BOILER_VALVE;
    }
}
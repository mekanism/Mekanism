package mekanism.common.block.basic;

import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.ThermoelectricBoilerContainer;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;

public class BlockBoilerCasing extends BlockBasicMultiblock implements IHasInventory, IHasTileEntity<TileEntityBoilerCasing>, IHasGui<TileEntityBoilerCasing> {

    public BlockBoilerCasing() {
        super("boiler_casing");
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityBoilerCasing tile) {
        return new ContainerProvider("mekanism.container.thermoelectric_boiler", (i, inv, player) -> new ThermoelectricBoilerContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityBoilerCasing> getTileType() {
        return MekanismTileEntityTypes.BOILER_CASING;
    }
}
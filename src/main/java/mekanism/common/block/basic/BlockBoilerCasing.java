package mekanism.common.block.basic;

import mekanism.api.block.IHasTileEntity;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.ThermoelectricBoilerContainer;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;

public class BlockBoilerCasing extends BlockBasicMultiblock implements IHasTileEntity<TileEntityBoilerCasing>, IHasGui<TileEntityBoilerCasing> {

    @Override
    public INamedContainerProvider getProvider(TileEntityBoilerCasing tile) {
        return new ContainerProvider("mekanism.container.thermoelectric_boiler", (i, inv, player) -> new ThermoelectricBoilerContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityBoilerCasing> getTileType() {
        return MekanismTileEntityTypes.BOILER_CASING;
    }
}
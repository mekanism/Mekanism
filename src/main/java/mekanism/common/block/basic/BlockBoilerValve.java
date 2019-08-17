package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ISupportsComparator;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.ThermoelectricBoilerContainer;
import mekanism.common.tile.TileEntityBoilerValve;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BlockBoilerValve extends BlockBasicMultiblock implements IHasInventory, IHasTileEntity<TileEntityBoilerValve>, ISupportsComparator, IHasGui<TileEntityBoilerValve> {

    public BlockBoilerValve() {
        super("boiler_valve");
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        return new TileEntityBoilerValve();
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityBoilerValve> getTileClass() {
        return TileEntityBoilerValve.class;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityBoilerValve tile) {
        return new ContainerProvider("mekanism.container.thermoelectric_boiler", (i, inv, player) -> new ThermoelectricBoilerContainer(i, inv, tile));
    }
}
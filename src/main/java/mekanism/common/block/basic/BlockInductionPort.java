package mekanism.common.block.basic;

import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.IStateActive;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.energy.InductionMatrixContainer;
import mekanism.common.tile.TileEntityInductionPort;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntityType;

public class BlockInductionPort extends BlockBasicMultiblock implements IStateActive, IHasInventory, IHasTileEntity<TileEntityInductionPort>, ISupportsComparator,
      IHasGui<TileEntityInductionPort> {

    public BlockInductionPort() {
        super("induction_port");
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityInductionPort tile) {
        return new ContainerProvider("mekanism.container.induction_matrix", (i, inv, player) -> new InductionMatrixContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityInductionPort> getTileType() {
        return MekanismTileEntityTypes.INDUCTION_PORT;
    }
}
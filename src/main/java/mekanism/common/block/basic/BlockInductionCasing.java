package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.energy.InductionMatrixContainer;
import mekanism.common.tile.TileEntityInductionCasing;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class BlockInductionCasing extends BlockBasicMultiblock implements IHasTileEntity<TileEntityInductionCasing>, IHasGui<TileEntityInductionCasing> {

    public BlockInductionCasing() {
        super("induction_casing");
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        return new TileEntityInductionCasing();
    }

    @Nullable
    @Override
    public Class<? extends TileEntityInductionCasing> getTileClass() {
        return TileEntityInductionCasing.class;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityInductionCasing tile) {
        return new ContainerProvider("mekanism.container.induction_matrix", (i, inv, player) -> new InductionMatrixContainer(i, inv, tile));
    }
}
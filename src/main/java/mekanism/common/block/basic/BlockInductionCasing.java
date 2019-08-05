package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.tile.TileEntityInductionCasing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockInductionCasing extends BlockBasicMultiblock implements IHasTileEntity<TileEntityInductionCasing> {

    public BlockInductionCasing() {
        super("induction_casing");
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityInductionCasing();
    }

    @Nullable
    @Override
    public Class<? extends TileEntityInductionCasing> getTileClass() {
        return TileEntityInductionCasing.class;
    }
}
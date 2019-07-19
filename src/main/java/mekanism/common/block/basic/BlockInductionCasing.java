package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.common.tile.TileEntityInductionCasing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockInductionCasing extends BlockBasicMultiblock {

    public BlockInductionCasing() {
        super("induction_casing");
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityInductionCasing();
    }
}
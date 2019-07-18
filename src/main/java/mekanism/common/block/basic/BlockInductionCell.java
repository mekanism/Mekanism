package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.common.block.BlockBasic;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.TileEntityInductionCell;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockInductionCell extends BlockBasic {

    private final InductionCellTier tier;

    public BlockInductionCell(InductionCellTier tier) {
        super(tier.getBaseTier().getSimpleName() + "_induction_cell");
        this.tier = tier;
    }

    @Override
    public boolean hasDescription() {
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityInductionCell();
    }
}
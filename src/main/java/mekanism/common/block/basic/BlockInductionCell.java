package mekanism.common.block.basic;

import mekanism.common.block.BlockBasic;
import mekanism.common.tier.InductionCellTier;

public class BlockInductionCell extends BlockBasic {

    private final InductionCellTier tier;

    public BlockInductionCell(InductionCellTier tier) {
        super(tier.getBaseTier().getSimpleName() + "_induction_cell");
        this.tier = tier;
    }
}
package mekanism.common.block.basic;

import mekanism.common.block.BlockBasic;
import mekanism.common.tier.BinTier;

public class BlockBin extends BlockBasic {

    private final BinTier tier;

    public BlockBin(BinTier tier) {
        super(tier.getBaseTier().getSimpleName() + "_bin");
        this.tier = tier;
    }
}
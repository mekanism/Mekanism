package mekanism.common.block.basic;

import mekanism.common.block.BlockBasic;
import mekanism.common.tier.InductionProviderTier;

public class BlockInductionProvider extends BlockBasic {

    private final InductionProviderTier tier;

    public BlockInductionProvider(InductionProviderTier tier) {
        super(tier.getBaseTier().getSimpleName() + "_induction_provider");
        this.tier = tier;
    }
}
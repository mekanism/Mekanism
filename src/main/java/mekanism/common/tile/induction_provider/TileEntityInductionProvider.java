package mekanism.common.tile.induction_provider;

import mekanism.common.base.IBlockProvider;
import mekanism.common.block.basic.BlockInductionProvider;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.base.TileEntityMekanism;

public abstract class TileEntityInductionProvider extends TileEntityMekanism {

    public InductionProviderTier tier;

    public TileEntityInductionProvider(IBlockProvider blockProvider) {
        super(blockProvider);
        this.tier = ((BlockInductionProvider) blockProvider.getBlock()).getTier();
    }

    @Override
    public void onUpdate() {
    }
}
package mekanism.common.tile.multiblock;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.base.TileEntityMekanism;

public class TileEntityInductionProvider extends TileEntityMekanism {

    public InductionProviderTier tier;

    public TileEntityInductionProvider(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        tier = Attribute.getTier(getBlockType(), InductionProviderTier.class);
    }
}
package mekanism.common.tile;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.basic.BlockInductionProvider;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.block.Block;

public class TileEntityInductionProvider extends TileEntityMekanism {

    public InductionProviderTier tier;

    public TileEntityInductionProvider(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void setSupportedTypes(Block block) {
        super.setSupportedTypes(block);
        //TODO: Do this in a better way, but currently we need to hijack this to set our tier earlier
        this.tier = ((BlockInductionProvider) block).getTier();
    }

    @Override
    public void onUpdate() {
    }
}
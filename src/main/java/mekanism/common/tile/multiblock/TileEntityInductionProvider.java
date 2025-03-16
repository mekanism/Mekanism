package mekanism.common.tile.multiblock;

import mekanism.common.block.attribute.Attribute;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityInductionProvider extends TileEntityInternalMultiblock {

    public InductionProviderTier tier;

    public TileEntityInductionProvider(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        tier = Attribute.getTier(getBlockHolder(), InductionProviderTier.class);
    }
}
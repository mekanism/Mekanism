package mekanism.common.tile.multiblock;

import java.util.Objects;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityInductionProvider extends TileEntityInternalMultiblock {

    public final InductionProviderTier tier;

    public TileEntityInductionProvider(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        tier = Objects.requireNonNull(Attribute.getTier(blockProvider, InductionProviderTier.class));
        super(blockProvider, pos, state);
    }
}
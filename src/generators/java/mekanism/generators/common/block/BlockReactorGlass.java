package mekanism.generators.common.block;

import mekanism.common.block.basic.BlockStructuralGlass;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.prefab.TileEntityStructuralMultiblock;
import mekanism.generators.common.block.fusion.BlockLaserFocusMatrix;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockReactorGlass<TILE extends TileEntityStructuralMultiblock> extends BlockStructuralGlass<TILE> {

    public BlockReactorGlass(BlockTypeTile<TILE> type, BlockBehaviour.Properties properties) {
        super(type, properties);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return super.skipRendering(state, adjacentBlockState, side) || adjacentBlockState.getBlock() instanceof BlockLaserFocusMatrix;
    }
}
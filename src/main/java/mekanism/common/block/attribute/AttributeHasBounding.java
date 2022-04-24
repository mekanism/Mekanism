package mekanism.common.block.attribute;

import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import mekanism.api.functions.TriConsumer;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

//TODO: Currently requires the block to also have a tile and to implement IBoundingBlock for functionality of things
// at some point that should be cleaned up some
public class AttributeHasBounding implements Attribute {

    private final TriConsumer<BlockPos, BlockState, Builder<BlockPos>> boundingPositions;

    public AttributeHasBounding(TriConsumer<BlockPos, BlockState, Builder<BlockPos>> boundingPositions) {
        this.boundingPositions = boundingPositions;
    }

    public void removeBoundingBlocks(Level level, BlockPos pos, BlockState state) {
        getPositions(pos, state).forEach(p -> {
            BlockState boundingState = level.getBlockState(p);
            if (!boundingState.isAir()) {
                //The state might be air if we broke a bounding block first
                if (boundingState.is(MekanismBlocks.BOUNDING_BLOCK.getBlock())) {
                    level.removeBlock(p, false);
                } else {
                    Mekanism.logger.warn("Skipping removing block, expected bounding block but the block at {} in {} was {}", p, level.dimension().location(),
                          boundingState.getBlock().getRegistryName());
                }
            }
        });
    }

    public void placeBoundingBlocks(Level level, BlockPos orig, BlockState state) {
        getPositions(orig, state).forEach(boundingLocation -> {
            BlockBounding boundingBlock = MekanismBlocks.BOUNDING_BLOCK.getBlock();
            BlockState newState = BlockStateHelper.getStateForPlacement(boundingBlock, boundingBlock.defaultBlockState(), level, boundingLocation, null, Direction.NORTH);
            level.setBlock(boundingLocation, newState, Block.UPDATE_ALL);
            if (!level.isClientSide()) {
                TileEntityBoundingBlock tile = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, level, boundingLocation);
                if (tile != null) {
                    tile.setMainLocation(orig);
                } else {
                    Mekanism.logger.warn("Unable to find Bounding Block Tile at: {}", boundingLocation);
                }
            }
        });
    }

    public Stream<BlockPos> getPositions(BlockPos pos, BlockState state) {
        Stream.Builder<BlockPos> builder = Stream.builder();
        boundingPositions.accept(pos, state, builder);
        return builder.build();
    }
}
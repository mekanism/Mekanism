package mekanism.common.block.attribute;

import mekanism.common.Mekanism;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

//TODO: Currently requires the block to also have a tile and to implement IBoundingBlock for functionality of things
// at some point that should be cleaned up some
public class AttributeHasBounding implements Attribute {

    public static final AttributeHasBounding ABOVE_ONLY = new AttributeHasBounding(new HandleBoundingBlock() {
        @Override
        public <DATA> boolean handle(Level level, BlockPos pos, BlockState state, DATA data, TriBooleanFunction<Level, BlockPos, DATA> consumer) {
            return consumer.accept(level, pos.above(), data);
        }
    });

    private final HandleBoundingBlock boundingPosHandlers;

    public AttributeHasBounding(HandleBoundingBlock boundingPosHandlers) {
        this.boundingPosHandlers = boundingPosHandlers;
    }

    public void removeBoundingBlocks(Level world, BlockPos pos, BlockState state) {
        handle(world, pos, state, null, (level, p, ignored) -> {
            BlockState boundingState = level.getBlockState(p);
            if (!boundingState.isAir()) {
                //The state might be air if we broke a bounding block first
                if (boundingState.is(MekanismBlocks.BOUNDING_BLOCK)) {
                    //Start by removing the block entity, so it doesn't try to proxy removal back to the main position
                    level.removeBlockEntity(p);
                    level.removeBlock(p, false);
                } else {
                    Mekanism.logger.warn("Skipping removing block, expected bounding block but the block at {} in {} was {}", p, level.dimension().location(),
                          boundingState.getBlockHolder().getRegisteredName());
                }
            }
            return true;
        });
    }

    public void placeBoundingBlocks(Level world, BlockPos orig, BlockState state) {
        handle(world, orig, state, orig, (level, boundingLocation, data) -> {
            BlockState newState = BlockStateHelper.getStateForPlacement(MekanismBlocks.BOUNDING_BLOCK.defaultState(), level, boundingLocation, null, Direction.NORTH);
            if (level.setBlockAndUpdate(boundingLocation, newState)) {
                TileEntityBoundingBlock tile = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, level, boundingLocation, true);
                if (tile != null) {
                    //Note: As we place it on both server and client, we don't need to sync the main location here as it should already be set on both ends
                    tile.setMainLocation(data, false);
                } else {
                    Mekanism.logger.warn("Unable to find Bounding Block Tile at: {} in {}", boundingLocation, level.dimension().location());
                }
            } else {
                Mekanism.logger.warn("Unable to set Bounding Block at: {} in {}", boundingLocation, level.dimension().location());
            }
            return true;//todo decide if this should bail on failure even though we likely have already partially placed some of the bounding blocks
        });
    }

    public void syncMasterPosition(Level world, BlockPos orig, BlockState state) {
        if (!world.isClientSide()) {
            handle(world, orig, state, orig, (level, boundingLocation, data) -> {
                TileEntityBoundingBlock tile = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, level, boundingLocation, true);
                if (tile != null) {
                    tile.setMainLocation(data, true);
                } else {
                    Mekanism.logger.warn("Unable to find Bounding Block Tile for sync at: {} in {}", boundingLocation, level.dimension().location());
                }
                return true;
            });
        }
    }

    public <DATA> boolean handle(Level level, BlockPos pos, BlockState state, DATA data, TriBooleanFunction<Level, BlockPos, DATA> predicate) {
        return boundingPosHandlers.handle(level, pos, state, data, predicate);
    }

    public interface HandleBoundingBlock {

        <DATA> boolean handle(Level level, BlockPos pos, BlockState state, DATA data, TriBooleanFunction<Level, BlockPos, DATA> predicate);
    }

    @FunctionalInterface
    public interface TriBooleanFunction<PARAM1, PARAM2, PARAM3> {

        boolean accept(PARAM1 param1, PARAM2 param2, PARAM3 param3);
    }
}
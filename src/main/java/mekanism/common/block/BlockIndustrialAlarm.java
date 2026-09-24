package mekanism.common.block;

import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityIndustrialAlarm;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockIndustrialAlarm extends BlockTileModel<TileEntityIndustrialAlarm, BlockTypeTile<TileEntityIndustrialAlarm>> {

    private static final VoxelShape[] MIN_SHAPES = new VoxelShape[EnumUtils.DIRECTIONS.length];

    static {
        VoxelShapeUtils.setShape(box(5, 11, 5, 11, 16, 11), MIN_SHAPES, true);
    }

    public BlockIndustrialAlarm(BlockBehaviour.Properties properties) {
        super(MekanismBlockTypes.INDUSTRIAL_ALARM, properties.strength(2, 2.4F).mapColor(MapColor.COLOR_RED));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighborPos,
          BlockState neighborState, RandomSource random) {
        if (directionToNeighbour.getOpposite() == Attribute.getFacing(state) && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighborPos, neighborState, random);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction side = Attribute.getFacing(state);
        if (side == null) {
            //Something went wrong, and we were passed an invalid state, return that we can't survive
            return false;
        }
        BlockPos offsetPos = pos.relative(side.getOpposite());
        BlockState offsetState = level.getBlockState(offsetPos);
        if (side == Direction.DOWN && offsetState.is(BlockTags.UNSTABLE_BOTTOM_CENTER)) {
            //Mirror vanilla's canSupportCenter check
            return false;
        }
        //Like SupportType and BlockState#isFaceSturdy except without support for the block state cache and with our own custom shapes
        VoxelShape projected = offsetState.getBlockSupportShape(level, offsetPos).getFaceShape(side);
        //Don't allow placing on blocks that are too small; same restrictions as vanilla except we have a better check for placing against the side
        return !Shapes.joinIsNotEmpty(projected, MIN_SHAPES[side.ordinal()], BooleanOp.ONLY_SECOND);
    }
}

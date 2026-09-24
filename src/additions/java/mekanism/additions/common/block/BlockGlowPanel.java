package mekanism.additions.common.block;

import mekanism.additions.common.registries.AdditionsBlockTypes;
import mekanism.api.text.EnumColor;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.prefab.BlockBase.BlockBaseModel;
import mekanism.common.content.blocktype.BlockType;
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
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockGlowPanel extends BlockBaseModel<BlockType> implements IColoredBlock {

    private static final VoxelShape[] MIN_SHAPES = new VoxelShape[EnumUtils.DIRECTIONS.length];

    static {
        VoxelShapeUtils.setShape(box(4, 13, 4, 12, 16, 12), MIN_SHAPES, true);
    }

    private final EnumColor color;

    public BlockGlowPanel(BlockBehaviour.Properties properties, EnumColor color) {
        super(AdditionsBlockTypes.GLOW_PANEL, properties.mapColor(color.getMapColor()).strength(1, 6));
        this.color = color;
    }

    @Override
    public EnumColor getColor() {
        return color;
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
        Direction sideOn = side.getOpposite();
        BlockPos offsetPos = pos.relative(sideOn);
        BlockState offsetState = level.getBlockState(offsetPos);
        if (side == Direction.DOWN && offsetState.is(BlockTags.UNSTABLE_BOTTOM_CENTER)) {
            //Mirror vanilla's canSupportCenter check
            return false;
        }
        //TODO: Is there a way for us to work around that walls return a smaller size even when wall post override is true?
        // Torches, and industrial alarms can be placed on walls and it will create the post, but glow panels need the post to be present first in order to place
        //TODO - 26.3: PR to neo making SupportType be an extendable enum so that we can just directly call isFaceSturdy?
        //Like SupportType and BlockState#isFaceSturdy except without support for the block state cache and with our own custom shapes
        VoxelShape projected = offsetState.getBlockSupportShape(level, offsetPos).getFaceShape(side);
        //Don't allow placing on blocks that are too small; same restrictions as vanilla except we have a better check for placing against the side
        return !Shapes.joinIsNotEmpty(projected, MIN_SHAPES[side.ordinal()], BooleanOp.ONLY_SECOND);
    }
}
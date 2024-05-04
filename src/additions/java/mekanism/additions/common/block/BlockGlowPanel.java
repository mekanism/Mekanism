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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class BlockGlowPanel extends BlockBaseModel<BlockType> implements IColoredBlock {

    private static final VoxelShape[] MIN_SHAPES = new VoxelShape[EnumUtils.DIRECTIONS.length];

    static {
        VoxelShapeUtils.setShape(box(4, 0, 4, 12, 16, 12), MIN_SHAPES, true);
    }

    private final EnumColor color;

    public BlockGlowPanel(EnumColor color) {
        super(AdditionsBlockTypes.GLOW_PANEL, BlockBehaviour.Properties.of().mapColor(color.getMapColor()).strength(1, 6));
        this.color = color;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @NotNull
    @Override
    protected BlockState updateShape(BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor world,
          @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
        if (facing.getOpposite() == Attribute.getFacing(state) && !state.canSurvive(world, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    protected boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader world, @NotNull BlockPos pos) {
        Direction side = Attribute.getFacing(state);
        Direction sideOn = side.getOpposite();
        BlockPos offsetPos = pos.relative(sideOn);
        VoxelShape projected = world.getBlockState(offsetPos).getBlockSupportShape(world, offsetPos).getFaceShape(side);
        //hasEnoughSolidSide does not quite work for us, as the shape is incorrect
        //Don't allow placing on leaves or a block that is too small
        // same restrictions as vanilla except we have a better check for placing against the side
        return !state.is(BlockTags.LEAVES) && !Shapes.joinIsNotEmpty(projected, MIN_SHAPES[sideOn.ordinal()], BooleanOp.ONLY_SECOND);
    }
}
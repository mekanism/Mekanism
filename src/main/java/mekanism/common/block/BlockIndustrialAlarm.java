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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class BlockIndustrialAlarm extends BlockTileModel<TileEntityIndustrialAlarm, BlockTypeTile<TileEntityIndustrialAlarm>> {

    private static final VoxelShape[] MIN_SHAPES = new VoxelShape[EnumUtils.DIRECTIONS.length];

    static {
        VoxelShapeUtils.setShape(box(5, 0, 5, 11, 16, 11), MIN_SHAPES, true);
    }

    public BlockIndustrialAlarm() {
        super(MekanismBlockTypes.INDUSTRIAL_ALARM, BlockBehaviour.Properties.of().strength(2, 2.4F).mapColor(MapColor.COLOR_RED));
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

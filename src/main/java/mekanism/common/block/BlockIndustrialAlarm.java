package mekanism.common.block;

import javax.annotation.Nonnull;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityIndustrialAlarm;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;

public class BlockIndustrialAlarm extends BlockTile<TileEntityIndustrialAlarm, BlockTypeTile<TileEntityIndustrialAlarm>> {

    private static final VoxelShape[] MIN_SHAPES = new VoxelShape[EnumUtils.DIRECTIONS.length];

    static {
        VoxelShapeUtils.setShape(box(5, 0, 5, 11, 16, 11), MIN_SHAPES, true);
    }

    public BlockIndustrialAlarm() {
        super(MekanismBlockTypes.INDUSTRIAL_ALARM, AbstractBlock.Properties.of(Material.GLASS).strength(2, 2.4F));
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState updateShape(BlockState state, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull IWorld world,
          @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos) {
        if (facing.getOpposite() == Attribute.get(state, AttributeStateFacing.class).getDirection(state) && !state.canSurvive(world, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    @Deprecated
    public boolean canSurvive(@Nonnull BlockState state, @Nonnull IWorldReader world, @Nonnull BlockPos pos) {
        Direction side = Attribute.get(state, AttributeStateFacing.class).getDirection(state);
        Direction sideOn = side.getOpposite();
        BlockPos offsetPos = pos.relative(sideOn);
        VoxelShape projected = world.getBlockState(offsetPos).getBlockSupportShape(world, offsetPos).getFaceShape(side);
        //hasEnoughSolidSide does not quite work for us, as the shape is incorrect
        //Don't allow placing on leaves or a block that is too small
        // same restrictions as vanilla except we have a better check for placing against the side
        return !state.is(BlockTags.LEAVES) && !VoxelShapes.joinIsNotEmpty(projected, MIN_SHAPES[sideOn.ordinal()], IBooleanFunction.ONLY_SECOND);
    }
}

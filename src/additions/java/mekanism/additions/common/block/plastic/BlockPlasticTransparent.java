package mekanism.additions.common.block.plastic;

import mekanism.api.text.EnumColor;
import mekanism.common.block.attribute.Attributes.AttributeMobSpawn;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockPlasticTransparent extends BlockPlastic {

    public BlockPlasticTransparent(BlockBehaviour.Properties properties, EnumColor color) {
        super(properties.strength(5, 6)
                    .noOcclusion()
                    .isValidSpawn(AttributeMobSpawn.NEVER_PREDICATE)
                    .isSuffocating(BlockStateHelper.NEVER_PREDICATE).isViewBlocking(BlockStateHelper.NEVER_PREDICATE),
              color);
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 0.8F;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return isSideInvisible(this, state, adjacentBlockState, side);
    }

    @Override
    public Integer getBeaconColorMultiplier(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos) {
        return getColor().getPackedColor();
    }

    @SuppressWarnings("DataFlowIssue")
    public static boolean isSideInvisible(IColoredBlock block, BlockState state, BlockState adjacentBlockState, Direction side) {
        Block adjacentBlock = adjacentBlockState.getBlock();
        if (adjacentBlock instanceof BlockPlasticTransparent || adjacentBlock instanceof BlockPlasticTransparentSlab
            || adjacentBlock instanceof BlockPlasticTransparentStairs) {
            IColoredBlock plastic = (IColoredBlock) adjacentBlock;
            if (plastic.getColor() == block.getColor()) {
                try {
                    VoxelShape shape = state.getShape(null, null);
                    VoxelShape adjacentShape = adjacentBlockState.getShape(null, null);

                    VoxelShape faceShape = shape.getFaceShape(side);
                    VoxelShape adjacentFaceShape = adjacentShape.getFaceShape(side.getOpposite());
                    return !Shapes.joinIsNotEmpty(faceShape, adjacentFaceShape, BooleanOp.ONLY_FIRST);
                } catch (Exception _) {
                    //Something might have errored due to the null world and position
                }
            }
        }
        return false;
    }
}
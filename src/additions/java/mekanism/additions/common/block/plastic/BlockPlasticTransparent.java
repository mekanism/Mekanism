package mekanism.additions.common.block.plastic;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.block.attribute.Attributes.AttributeMobSpawn;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockPlasticTransparent extends BlockPlastic {

    public BlockPlasticTransparent(EnumColor color) {
        super(color, properties -> properties.strength(5, 6).noOcclusion().isValidSpawn(AttributeMobSpawn.NEVER_PREDICATE)
              .isSuffocating(BlockStateHelper.NEVER_PREDICATE).isViewBlocking(BlockStateHelper.NEVER_PREDICATE));
    }

    @Override
    @Deprecated
    public float getShadeBrightness(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        return 0.8F;
    }

    @Override
    @Deprecated
    public boolean useShapeForLightOcclusion(@Nonnull BlockState state) {
        return true;
    }

    @Override
    public boolean propagatesSkylightDown(@Nonnull BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos) {
        return true;
    }

    @Override
    @Deprecated
    public boolean skipRendering(@Nonnull BlockState state, @Nonnull BlockState adjacentBlockState, @Nonnull Direction side) {
        return isSideInvisible(this, state, adjacentBlockState, side);
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos) {
        return getColor().getRgbCodeFloat();
    }

    public static boolean isSideInvisible(@Nonnull IColoredBlock block, @Nonnull BlockState state, @Nonnull BlockState adjacentBlockState, @Nonnull Direction side) {
        Block adjacentBlock = adjacentBlockState.getBlock();
        if (adjacentBlock instanceof BlockPlasticTransparent || adjacentBlock instanceof BlockPlasticTransparentSlab
            || adjacentBlock instanceof BlockPlasticTransparentStairs) {
            IColoredBlock plastic = ((IColoredBlock) adjacentBlock);
            if (plastic.getColor() == block.getColor()) {
                try {
                    VoxelShape shape = state.getShape(null, null);
                    VoxelShape adjacentShape = adjacentBlockState.getShape(null, null);

                    VoxelShape faceShape = shape.getFaceShape(side);
                    VoxelShape adjacentFaceShape = adjacentShape.getFaceShape(side.getOpposite());
                    return !Shapes.joinIsNotEmpty(faceShape, adjacentFaceShape, BooleanOp.ONLY_FIRST);
                } catch (Exception ignored) {
                    //Something might have errored due to the null world and position
                }
            }
        }
        return false;
    }
}
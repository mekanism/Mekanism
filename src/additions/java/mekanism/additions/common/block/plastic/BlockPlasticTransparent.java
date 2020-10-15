package mekanism.additions.common.block.plastic;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class BlockPlasticTransparent extends BlockPlastic {

    public BlockPlasticTransparent(EnumColor color) {
        super(color, properties -> properties.hardnessAndResistance(5F, 10F).notSolid());
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return 0.8F;
    }

    @Override
    @Deprecated
    public boolean isTransparent(@Nonnull BlockState state) {
        return true;
    }

    @Override
    public boolean propagatesSkylightDown(@Nonnull BlockState state, @Nonnull IBlockReader reader, @Nonnull BlockPos pos) {
        return true;
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, PlacementType type, EntityType<?> entityType) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isSideInvisible(@Nonnull BlockState state, @Nonnull BlockState adjacentBlockState, @Nonnull Direction side) {
        return isSideInvisible(this, state, adjacentBlockState, side);
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos) {
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

                    VoxelShape faceShape = shape.project(side);
                    VoxelShape adjacentFaceShape = adjacentShape.project(side.getOpposite());
                    return !VoxelShapes.compare(faceShape, adjacentFaceShape, IBooleanFunction.ONLY_FIRST);
                } catch (Exception ignored) {
                    //Something might have errored due to the null world and position
                }
            }
        }
        return false;
    }
}
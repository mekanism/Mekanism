package mekanism.generators.common.block.fusion;

import javax.annotation.Nonnull;
import mekanism.common.block.basic.BlockStructuralGlass;
import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import mekanism.generators.common.tile.fusion.TileEntityLaserFocusMatrix;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockLaserFocusMatrix extends BlockBasicMultiblock<TileEntityLaserFocusMatrix> {

    public BlockLaserFocusMatrix() {
        super(GeneratorsBlockTypes.LASER_FOCUS_MATRIX, BlockBehaviour.Properties.of(Material.GLASS).strength(3.5F, 4.8F).noOcclusion()
              .isSuffocating(BlockStateHelper.NEVER_PREDICATE).isViewBlocking(BlockStateHelper.NEVER_PREDICATE));
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter world, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    @Deprecated
    public boolean skipRendering(@Nonnull BlockState state, @Nonnull BlockState adjacentBlockState, @Nonnull Direction side) {
        Block blockOffset = adjacentBlockState.getBlock();
        if (blockOffset instanceof BlockStructuralGlass || blockOffset instanceof BlockLaserFocusMatrix) {
            return true;
        }
        return super.skipRendering(state, adjacentBlockState, side);
    }

    @Override
    @Deprecated
    public float getShadeBrightness(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(@Nonnull BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos) {
        return true;
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getVisualShape(@Nonnull BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        return Shapes.empty();
    }
}
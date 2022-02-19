package mekanism.generators.common.block.fusion;

import javax.annotation.Nonnull;
import mekanism.common.block.basic.BlockStructuralGlass;
import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import mekanism.generators.common.tile.fusion.TileEntityLaserFocusMatrix;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;

public class BlockLaserFocusMatrix extends BlockBasicMultiblock<TileEntityLaserFocusMatrix> {

    public BlockLaserFocusMatrix() {
        super(GeneratorsBlockTypes.LASER_FOCUS_MATRIX, AbstractBlock.Properties.of(Material.GLASS).strength(3.5F, 4.8F).noOcclusion());
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, IBlockDisplayReader world, BlockPos pos, FluidState fluidState) {
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
    public float getShadeBrightness(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(@Nonnull BlockState state, @Nonnull IBlockReader reader, @Nonnull BlockPos pos) {
        return true;
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getVisualShape(@Nonnull BlockState state, @Nonnull IBlockReader reader, @Nonnull BlockPos pos, @Nonnull ISelectionContext ctx) {
        return VoxelShapes.empty();
    }
}
package mekanism.generators.common.block.fusion;

import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.generators.common.block.BlockReactorGlass;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import mekanism.generators.common.tile.fusion.TileEntityLaserFocusMatrix;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class BlockLaserFocusMatrix extends BlockBasicMultiblock<TileEntityLaserFocusMatrix> {

    public BlockLaserFocusMatrix() {
        super(GeneratorsBlockTypes.LASER_FOCUS_MATRIX, BlockBehaviour.Properties.of().sound(SoundType.GLASS).strength(3.5F, 4.8F)
              .requiresCorrectToolForDrops().noOcclusion().isSuffocating(BlockStateHelper.NEVER_PREDICATE).isViewBlocking(BlockStateHelper.NEVER_PREDICATE)
              .instrument(NoteBlockInstrument.HAT));
    }

    @Override
    public boolean shouldDisplayFluidOverlay(@NotNull BlockState state, @NotNull BlockAndTintGetter world, @NotNull BlockPos pos, @NotNull FluidState fluidState) {
        return true;
    }

    @Override
    protected boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        Block blockOffset = adjacentBlockState.getBlock();
        if (blockOffset instanceof BlockReactorGlass || blockOffset instanceof BlockLaserFocusMatrix) {
            return true;
        }
        return super.skipRendering(state, adjacentBlockState, side);
    }

    @Override
    protected float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter reader, @NotNull BlockPos pos) {
        return true;
    }

    @NotNull
    @Override
    protected VoxelShape getVisualShape(@NotNull BlockState state, @NotNull BlockGetter reader, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return Shapes.empty();
    }
}
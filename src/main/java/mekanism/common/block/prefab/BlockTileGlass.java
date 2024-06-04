package mekanism.common.block.prefab;

import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class BlockTileGlass<TILE extends TileEntityMekanism, TYPE extends BlockTypeTile<TILE>> extends BlockTile<TILE, TYPE> {

    public BlockTileGlass(TYPE type) {
        super(type, BlockBehaviour.Properties.of().sound(SoundType.GLASS).strength(3.5F, 9.6F).noOcclusion().requiresCorrectToolForDrops()
              .isSuffocating(BlockStateHelper.NEVER_PREDICATE).isViewBlocking(BlockStateHelper.NEVER_PREDICATE).instrument(NoteBlockInstrument.HAT));
    }

    @Override
    public boolean shouldDisplayFluidOverlay(@NotNull BlockState state, @NotNull BlockAndTintGetter world, @NotNull BlockPos pos, @NotNull FluidState fluidState) {
        return true;
    }

    @Override
    protected boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        return adjacentBlockState.getBlock() instanceof BlockTileGlass;
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
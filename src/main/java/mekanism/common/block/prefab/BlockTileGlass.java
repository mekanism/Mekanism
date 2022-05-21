package mekanism.common.block.prefab;

import javax.annotation.Nonnull;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockTileGlass<TILE extends TileEntityMekanism, TYPE extends BlockTypeTile<TILE>> extends BlockTile<TILE, TYPE> {

    public BlockTileGlass(TYPE type) {
        super(type, BlockBehaviour.Properties.of(Material.GLASS).strength(3.5F, 9.6F).noOcclusion().requiresCorrectToolForDrops()
              .isSuffocating(BlockStateHelper.NEVER_PREDICATE).isViewBlocking(BlockStateHelper.NEVER_PREDICATE));
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter world, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    @Deprecated
    public boolean skipRendering(@Nonnull BlockState state, @Nonnull BlockState adjacentBlockState, @Nonnull Direction side) {
        return adjacentBlockState.getBlock() instanceof BlockTileGlass;
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
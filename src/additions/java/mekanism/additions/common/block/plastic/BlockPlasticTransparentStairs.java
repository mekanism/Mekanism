package mekanism.additions.common.block.plastic;

import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.block.attribute.Attributes.AttributeMobSpawn;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class BlockPlasticTransparentStairs extends BlockPlasticStairs {

    public BlockPlasticTransparentStairs(IBlockProvider blockProvider, EnumColor color) {
        super(blockProvider, color, properties -> properties.notSolid().setAllowsSpawn(AttributeMobSpawn.NEVER_PREDICATE));
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
    @Deprecated
    public boolean isSideInvisible(@Nonnull BlockState state, @Nonnull BlockState adjacentBlockState, @Nonnull Direction side) {
        return BlockPlasticTransparent.isSideInvisible(this, state, adjacentBlockState, side);
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos) {
        return getColor().getRgbCodeFloat();
    }
}
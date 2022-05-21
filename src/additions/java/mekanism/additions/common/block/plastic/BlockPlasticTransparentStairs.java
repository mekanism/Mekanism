package mekanism.additions.common.block.plastic;

import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.block.attribute.Attributes.AttributeMobSpawn;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPlasticTransparentStairs extends BlockPlasticStairs {

    public BlockPlasticTransparentStairs(IBlockProvider blockProvider, EnumColor color) {
        super(blockProvider, color, properties -> properties.noOcclusion().isValidSpawn(AttributeMobSpawn.NEVER_PREDICATE).isSuffocating(BlockStateHelper.NEVER_PREDICATE)
              .isViewBlocking(BlockStateHelper.NEVER_PREDICATE));
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
        return BlockPlasticTransparent.isSideInvisible(this, state, adjacentBlockState, side);
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos) {
        return getColor().getRgbCodeFloat();
    }
}
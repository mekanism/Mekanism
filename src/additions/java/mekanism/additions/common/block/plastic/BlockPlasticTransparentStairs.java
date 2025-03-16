package mekanism.additions.common.block.plastic;

import mekanism.api.text.EnumColor;
import mekanism.common.block.attribute.Attributes.AttributeMobSpawn;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BlockPlasticTransparentStairs extends BlockPlasticStairs {

    public BlockPlasticTransparentStairs(BlockState baseState, EnumColor color) {
        super(baseState, color, properties -> properties.noOcclusion().isValidSpawn(AttributeMobSpawn.NEVER_PREDICATE).isSuffocating(BlockStateHelper.NEVER_PREDICATE)
              .isViewBlocking(BlockStateHelper.NEVER_PREDICATE));
    }

    @Override
    protected float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
        return 0.8F;
    }

    @Override
    protected boolean useShapeForLightOcclusion(@NotNull BlockState state) {
        return true;
    }

    @Override
    protected boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter reader, @NotNull BlockPos pos) {
        return true;
    }

    @Override
    protected boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        return BlockPlasticTransparent.isSideInvisible(this, state, adjacentBlockState, side);
    }

    @Override
    public Integer getBeaconColorMultiplier(@NotNull BlockState state, @NotNull LevelReader world, @NotNull BlockPos pos, @NotNull BlockPos beaconPos) {
        return getColor().getPackedColor();
    }
}
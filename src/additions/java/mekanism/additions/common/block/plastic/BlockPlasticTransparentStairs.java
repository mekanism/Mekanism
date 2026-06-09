package mekanism.additions.common.block.plastic;

import mekanism.api.text.EnumColor;
import mekanism.common.block.attribute.Attributes.AttributeMobSpawn;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPlasticTransparentStairs extends BlockPlasticStairs {

    public BlockPlasticTransparentStairs(BlockState baseState, BlockBehaviour.Properties properties, EnumColor color) {
        super(baseState, properties.noOcclusion()
                    .isValidSpawn(AttributeMobSpawn.NEVER_PREDICATE)
                    .isSuffocating(BlockStateHelper.NEVER_PREDICATE)
                    .isViewBlocking(BlockStateHelper.NEVER_PREDICATE),
              color);
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 0.8F;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        return BlockPlasticTransparent.isSideInvisible(this, state, adjacentBlockState, side);
    }

    @Override
    public Integer getBeaconColorMultiplier(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos) {
        return getColor().getPackedColor();
    }
}
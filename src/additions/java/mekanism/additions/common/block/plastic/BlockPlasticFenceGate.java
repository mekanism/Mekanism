package mekanism.additions.common.block.plastic;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFluidLoggable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;

public class BlockPlasticFenceGate extends FenceGateBlock implements IColoredBlock, IStateFluidLoggable {

    private final EnumColor color;

    public BlockPlasticFenceGate(EnumColor color) {
        super(BlockStateHelper.applyLightLevelAdjustments(BlockBehaviour.Properties.of(BlockPlastic.PLASTIC, color.getMapColor())
              .strength(5, 6)));
        this.color = color;
        //Uses getDefaultState as starting state to take into account the stuff from super
        registerDefaultState(BlockStateHelper.getDefaultState(defaultBlockState()));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        return BlockStateHelper.getStateForPlacement(this, super.getStateForPlacement(context), context);
    }

    @Nonnull
    @Override
    @Deprecated
    public FluidState getFluidState(@Nonnull BlockState state) {
        return getFluid(state);
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState updateShape(@Nonnull BlockState state, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull LevelAccessor world,
          @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos) {
        updateFluids(state, world, currentPos);
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        BlockStateHelper.fillBlockStateContainer(this, builder);
    }
}
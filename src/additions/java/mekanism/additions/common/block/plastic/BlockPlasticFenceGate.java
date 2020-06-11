package mekanism.additions.common.block.plastic;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFluidLoggable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class BlockPlasticFenceGate extends FenceGateBlock implements IColoredBlock, IStateFluidLoggable {

    private final EnumColor color;

    public BlockPlasticFenceGate(EnumColor color) {
        super(Properties.create(BlockPlastic.PLASTIC, color.getMapColor()).hardnessAndResistance(5F, 10F));
        this.color = color;
        this.setDefaultState(getDefaultState().with(BlockStateHelper.FLUID_LOGGED, false));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @Override
    public BlockState getStateForPlacement(@Nonnull BlockItemUseContext context) {
        return BlockStateHelper.getStateForPlacement(this, super.getStateForPlacement(context), context);
    }

    @Nonnull
    @Override
    @Deprecated
    public IFluidState getFluidState(@Nonnull BlockState state) {
        return getFluid(state);
    }

    @Nonnull
    @Override
    public BlockState updatePostPlacement(@Nonnull BlockState state, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull IWorld world,
          @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos) {
        updateFluids(state, world, currentPos);
        return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        BlockStateHelper.fillBlockStateContainer(this, builder);
    }
}
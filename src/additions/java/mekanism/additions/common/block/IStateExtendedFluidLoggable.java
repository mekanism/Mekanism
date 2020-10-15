package mekanism.additions.common.block;

import javax.annotation.Nonnull;
import mekanism.common.block.states.IStateFluidLoggable;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.Constants.BlockFlags;

/**
 * Helper interface for implementation of smashing vanilla's water logging system with our own fluid logging system to allow easier implementation on blocks that extend
 * something that is already water loggable (fences, stairs, slabs)
 */
public interface IStateExtendedFluidLoggable extends IStateFluidLoggable {

    Fluid[] VANILLA_EXTENSION = new Fluid[]{Fluids.LAVA};
    IntegerProperty FLUID_LOGGED_EXTENSION = IntegerProperty.create("fluid_logged_extension", 0, VANILLA_EXTENSION.length);

    @Override
    default boolean isValidFluid(@Nonnull Fluid fluid) {
        return fluid == Fluids.WATER || IStateFluidLoggable.super.isValidFluid(fluid);
    }

    @Nonnull
    @Override
    default Fluid[] getSupportedFluids() {
        return VANILLA_EXTENSION;
    }

    @Nonnull
    @Override
    default IntegerProperty getFluidLoggedProperty() {
        return FLUID_LOGGED_EXTENSION;
    }

    @Nonnull
    @Override
    default FluidState getFluid(@Nonnull BlockState state) {
        if (state.get(BlockStateProperties.WATERLOGGED)) {
            return Fluids.WATER.getDefaultState();
        }
        return IStateFluidLoggable.super.getFluid(state);
    }

    @Override
    default boolean canContainFluid(@Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Fluid fluid) {
        return !state.get(BlockStateProperties.WATERLOGGED) && IStateFluidLoggable.super.canContainFluid(world, pos, state, fluid);
    }

    @Override
    default boolean receiveFluid(@Nonnull IWorld world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull FluidState fluidState) {
        Fluid fluid = fluidState.getFluid();
        if (canContainFluid(world, pos, state, fluid)) {
            if (!world.isRemote()) {
                if (fluid == Fluids.WATER) {
                    world.setBlockState(pos, state.with(BlockStateProperties.WATERLOGGED, true), BlockFlags.DEFAULT);
                } else {
                    world.setBlockState(pos, state.with(getFluidLoggedProperty(), getSupportedFluidPropertyIndex(fluid)), BlockFlags.DEFAULT);
                }
                world.getPendingFluidTicks().scheduleTick(pos, fluid, fluid.getTickRate(world));
            }
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    default Fluid pickupFluid(@Nonnull IWorld world, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        if (state.get(BlockStateProperties.WATERLOGGED)) {
            world.setBlockState(pos, state.with(BlockStateProperties.WATERLOGGED, false), BlockFlags.DEFAULT);
            return Fluids.WATER;
        }
        return IStateFluidLoggable.super.pickupFluid(world, pos, state);
    }
}
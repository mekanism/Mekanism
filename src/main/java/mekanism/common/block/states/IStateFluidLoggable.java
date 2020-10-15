package mekanism.common.block.states;

import java.util.Arrays;
import javax.annotation.Nonnull;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.Constants.BlockFlags;

public interface IStateFluidLoggable extends IBucketPickupHandler, ILiquidContainer {

    Fluid[] VANILLA_FLUIDS = new Fluid[]{Fluids.WATER, Fluids.LAVA};

    default boolean isValidFluid(@Nonnull Fluid fluid) {
        return Arrays.stream(getSupportedFluids()).anyMatch(supportedFluid -> supportedFluid == fluid);
    }

    /**
     * Gets the fluids this fluid loggable block supports. Overriding this is an easy way to change the block from supporting water and lava logging to supporting
     * specific different types of fluid, but dynamic fluid stuff cannot be done without a sizeable patch to forge/a change in vanilla so that {@link
     * BlockState#getFluidState()} has position information.
     *
     * @apiNote If overriding to a different amount of fluids make sure to also override {@link #getFluidLoggedProperty()}
     */
    @Nonnull
    default Fluid[] getSupportedFluids() {
        return VANILLA_FLUIDS;
    }

    /**
     * @return BlockState property for representing fluid loggable blocks
     *
     * @apiNote Ovveride this if changing the number of fluids {@link #getSupportedFluids()} returns.
     */
    @Nonnull
    default IntegerProperty getFluidLoggedProperty() {
        return BlockStateHelper.FLUID_LOGGED;
    }

    @Nonnull
    default FluidState getFluid(@Nonnull BlockState state) {
        int fluidLogged = state.get(getFluidLoggedProperty());
        if (fluidLogged > 0) {
            Fluid fluid = getSupportedFluids()[fluidLogged - 1];
            if (fluid instanceof FlowingFluid) {
                return ((FlowingFluid) fluid).getStillFluidState(false);
            }
            return fluid.getDefaultState();
        }
        return Fluids.EMPTY.getDefaultState();
    }

    default void updateFluids(@Nonnull BlockState state, @Nonnull IWorld world, @Nonnull BlockPos currentPos) {
        int fluidLogged = state.get(getFluidLoggedProperty());
        if (fluidLogged > 0) {
            Fluid fluid = getSupportedFluids()[fluidLogged - 1];
            world.getPendingFluidTicks().scheduleTick(currentPos, fluid, fluid.getTickRate(world));
        }
    }

    @Override
    default boolean canContainFluid(@Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Fluid fluid) {
        return state.get(getFluidLoggedProperty()) == 0 && isValidFluid(fluid);
    }

    default int getSupportedFluidPropertyIndex(@Nonnull Fluid fluid) {
        int fluidLogged = 0;
        Fluid[] supportedFluids = getSupportedFluids();
        for (int i = 0; i < supportedFluids.length; i++) {
            if (supportedFluids[i] == fluid) {
                fluidLogged = i + 1;
                break;
            }
        }
        return fluidLogged;
    }

    /**
     * Overwritten to check against canContainFluid instead of inlining the check to water directly.
     */
    @Override
    default boolean receiveFluid(@Nonnull IWorld world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull FluidState fluidState) {
        Fluid fluid = fluidState.getFluid();
        if (canContainFluid(world, pos, state, fluid)) {
            if (!world.isRemote()) {
                world.setBlockState(pos, state.with(getFluidLoggedProperty(), getSupportedFluidPropertyIndex(fluid)), BlockFlags.DEFAULT);
                world.getPendingFluidTicks().scheduleTick(pos, fluid, fluid.getTickRate(world));
            }
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    default Fluid pickupFluid(@Nonnull IWorld world, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        IntegerProperty fluidLoggedProperty = getFluidLoggedProperty();
        int fluidLogged = state.get(fluidLoggedProperty);
        if (fluidLogged > 0) {
            world.setBlockState(pos, state.with(fluidLoggedProperty, 0), BlockFlags.DEFAULT);
            return getSupportedFluids()[fluidLogged - 1];
        }
        return Fluids.EMPTY;
    }
}
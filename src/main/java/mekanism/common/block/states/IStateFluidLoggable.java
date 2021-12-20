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
     * @apiNote If overriding to a different amount of fluids, make sure to also override {@link #getFluidLoggedProperty()}
     */
    @Nonnull
    default Fluid[] getSupportedFluids() {
        return VANILLA_FLUIDS;
    }

    /**
     * @return BlockState property for representing fluid loggable blocks
     *
     * @apiNote Override this if changing the number of fluids {@link #getSupportedFluids()} returns.
     */
    @Nonnull
    default IntegerProperty getFluidLoggedProperty() {
        //TODO - 1.18: When removing CorrectingIntegerProperty, evaluate changing this entire thing to being an EnumProperty
        // so that F3 can show slightly better debug of what it is fluid logged with
        return BlockStateHelper.FLUID_LOGGED;
    }

    @Nonnull
    default FluidState getFluid(@Nonnull BlockState state) {
        int fluidLogged = state.getValue(getFluidLoggedProperty());
        if (fluidLogged > 0) {
            Fluid fluid = getSupportedFluids()[fluidLogged - 1];
            if (fluid instanceof FlowingFluid) {
                return ((FlowingFluid) fluid).getSource(false);
            }
            return fluid.defaultFluidState();
        }
        return Fluids.EMPTY.defaultFluidState();
    }

    default void updateFluids(@Nonnull BlockState state, @Nonnull IWorld world, @Nonnull BlockPos currentPos) {
        int fluidLogged = state.getValue(getFluidLoggedProperty());
        if (fluidLogged > 0) {
            Fluid fluid = getSupportedFluids()[fluidLogged - 1];
            world.getLiquidTicks().scheduleTick(currentPos, fluid, fluid.getTickDelay(world));
        }
    }

    @Override
    default boolean canPlaceLiquid(@Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Fluid fluid) {
        return state.getValue(getFluidLoggedProperty()) == 0 && isValidFluid(fluid);
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
    default boolean placeLiquid(@Nonnull IWorld world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull FluidState fluidState) {
        Fluid fluid = fluidState.getType();
        if (canPlaceLiquid(world, pos, state, fluid)) {
            if (!world.isClientSide()) {
                world.setBlock(pos, state.setValue(getFluidLoggedProperty(), getSupportedFluidPropertyIndex(fluid)), BlockFlags.DEFAULT);
                world.getLiquidTicks().scheduleTick(pos, fluid, fluid.getTickDelay(world));
            }
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    default Fluid takeLiquid(@Nonnull IWorld world, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        IntegerProperty fluidLoggedProperty = getFluidLoggedProperty();
        int fluidLogged = state.getValue(fluidLoggedProperty);
        if (fluidLogged > 0) {
            world.setBlock(pos, state.setValue(fluidLoggedProperty, 0), BlockFlags.DEFAULT);
            return getSupportedFluids()[fluidLogged - 1];
        }
        return Fluids.EMPTY;
    }
}
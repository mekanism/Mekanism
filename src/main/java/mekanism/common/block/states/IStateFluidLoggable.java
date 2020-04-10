package mekanism.common.block.states;

import javax.annotation.Nonnull;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.Constants.BlockFlags;

//TODO: The below TODOs go off an assumption of there being some form of forge patch first to support position information for fluid states
public interface IStateFluidLoggable extends IBucketPickupHandler, ILiquidContainer {

    default boolean isValidFluid(@Nonnull Fluid fluid) {
        //TODO: If we support a tile entity then return true, otherwise only allow water
        return fluid == getSupportedFluid();
    }

    /**
     * Gets the fluid this fluid loggable block supports. Overriding this is an easy way to change the block from supporting water logging to supporting a specific
     * different type of fluid, but dynamic fluid stuff cannot be done without a sizeable patch to forge/a change in vanilla so that {@link
     * net.minecraft.block.Block#getFluidState(BlockState)} has position information.
     */
    default Fluid getSupportedFluid() {
        return Fluids.WATER;
    }

    @Nonnull
    default IFluidState getFluid(@Nonnull BlockState state) {
        if (state.get(BlockStateHelper.FLUID_LOGGED)) {
            //TODO: Proxy this via the TileEntity if there is one, rather than using a hard coded getSupportedFluid
            Fluid fluid = getSupportedFluid();
            if (fluid instanceof FlowingFluid) {
                return ((FlowingFluid) fluid).getStillFluidState(false);
            }
            return fluid.getDefaultState();
        }
        return Fluids.EMPTY.getDefaultState();
    }

    default void updateFluids(@Nonnull BlockState state, @Nonnull IWorld world, @Nonnull BlockPos currentPos) {
        if (state.get(BlockStateHelper.FLUID_LOGGED)) {
            //TODO: Get proper fluid from the TileEntity
            Fluid fluid = getSupportedFluid();
            world.getPendingFluidTicks().scheduleTick(currentPos, fluid, fluid.getTickRate(world));
        }
    }

    @Override
    default boolean canContainFluid(@Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Fluid fluid) {
        return !state.get(BlockStateHelper.FLUID_LOGGED) && isValidFluid(fluid);
    }

    /**
     * Overwritten to check against canContainFluid instead of inlining the check to water directly.
     */
    @Override
    default boolean receiveFluid(@Nonnull IWorld world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IFluidState fluidState) {
        Fluid fluid = fluidState.getFluid();
        if (canContainFluid(world, pos, state, fluid)) {
            if (!world.isRemote()) {
                world.setBlockState(pos, state.with(BlockStateHelper.FLUID_LOGGED, true), BlockFlags.DEFAULT);
                world.getPendingFluidTicks().scheduleTick(pos, fluid, fluid.getTickRate(world));
                //TODO: Update the TileEntity if there is one with the proper fluid type
            }
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    default Fluid pickupFluid(@Nonnull IWorld world, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        if (state.get(BlockStateHelper.FLUID_LOGGED)) {
            world.setBlockState(pos, state.with(BlockStateHelper.FLUID_LOGGED, false), BlockFlags.DEFAULT);
            //TODO: Get proper fluid from block
            return getSupportedFluid();
        }
        return Fluids.EMPTY;
    }
}
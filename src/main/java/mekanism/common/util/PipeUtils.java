package mekanism.common.util;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.common.base.target.FluidHandlerTarget;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public final class PipeUtils {

    public static final IFluidTank[] EMPTY = new IFluidTank[]{};

    public static boolean isValidAcceptorOnSide(TileEntity tile, Direction side) {
        if (tile == null || CapabilityUtils.getCapabilityHelper(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite()).isPresent()) {
            return false;
        }
        return CapabilityUtils.getCapabilityHelper(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()).matches(container -> {
            //TODO: Check this
            int tanks = container.getTanks();
            for (int i = 0; i < tanks; i++) {
                FluidStack fluidInTank = container.getFluidInTank(i);
                if (!fluidInTank.isEmpty()) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Gets all the acceptors around a tile entity.
     *
     * @return array of IFluidHandlers
     */
    public static IFluidHandler[] getConnectedAcceptors(BlockPos pos, World world) {
        final IFluidHandler[] acceptors = new IFluidHandler[]{null, null, null, null, null, null};
        EmitUtils.forEachSide(world, pos, EnumSet.allOf(Direction.class), (tile, side) ->
              acceptors[side.ordinal()] = CapabilityUtils.getCapabilityHelper(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()).getValue());
        return acceptors;
    }

    /**
     * Emits fluid from a central block by splitting the received stack among the sides given.
     *
     * @param sides - the list of sides to output from
     * @param stack - the stack to output
     * @param from  - the TileEntity to output from
     *
     * @return the amount of gas emitted
     */
    public static int emit(Set<Direction> sides, @Nonnull FluidStack stack, TileEntity from) {
        if (stack.isEmpty()) {
            return 0;
        }
        //Fake that we have one target given we know that no sides will overlap
        // This allows us to have slightly better performance
        final FluidHandlerTarget target = new FluidHandlerTarget(stack);
        EmitUtils.forEachSide(from.getWorld(), from.getPos(), sides, (acceptor, side) -> {

            //Insert to access side
            final Direction accessSide = side.getOpposite();

            //Collect cap
            CapabilityUtils.getCapabilityHelper(acceptor, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, accessSide).ifPresent(handler -> {
                if (canFill(handler, stack)) {
                    target.addHandler(accessSide, handler);
                }
            });
        });

        int curHandlers = target.getHandlers().size();
        if (curHandlers > 0) {
            Set<FluidHandlerTarget> targets = new HashSet<>();
            targets.add(target);
            return EmitUtils.sendToAcceptors(targets, curHandlers, stack.getAmount(), stack);
        }
        return 0;
    }

    public static FluidStack copy(@Nonnull FluidStack fluid, int amount) {
        FluidStack ret = fluid.copy();
        ret.setAmount(amount);
        return ret;
    }

    public static boolean canFill(IFluidHandler handler, @Nonnull FluidStack stack) {
        //TODO: Check this
        return handler.fill(stack, FluidAction.SIMULATE) > 0;
    }

    public static boolean canDrain(IFluidHandler handler, @Nonnull FluidStack stack) {
        //TODO: Check this
        return !handler.drain(stack, FluidAction.SIMULATE).isEmpty();
    }
}
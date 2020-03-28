package mekanism.common.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.AutomationType;
import mekanism.common.base.target.FluidHandlerTarget;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public final class PipeUtils {

    /**
     * Gets all the acceptors around a tile entity.
     *
     * @return array of IFluidHandlers
     */
    public static IFluidHandler[] getConnectedAcceptors(BlockPos pos, World world, Set<Direction> sides) {
        final IFluidHandler[] acceptors = new IFluidHandler[EnumUtils.DIRECTIONS.length];
        EmitUtils.forEachSide(world, pos, sides, (tile, side) ->
              CapabilityUtils.getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()).ifPresent(handler -> acceptors[side.ordinal()] = handler));
        return acceptors;
    }

    public static void emit(IExtendedFluidTank tank, TileEntity from) {
        emit(EnumSet.allOf(Direction.class), tank, from);
    }

    public static void emit(Set<Direction> outputSides, IExtendedFluidTank tank, TileEntity from) {
        emit(outputSides, tank, from, tank.getCapacity());
    }

    public static void emit(Set<Direction> outputSides, IExtendedFluidTank tank, TileEntity from, int maxOutput) {
        if (!tank.isEmpty() && maxOutput > 0) {
            tank.extract(emit(outputSides, tank.extract(maxOutput, Action.SIMULATE, AutomationType.INTERNAL), from), Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    /**
     * Emits fluid from a central block by splitting the received stack among the sides given.
     *
     * @param sides - the list of sides to output from
     * @param stack - the stack to output
     * @param from  - the TileEntity to output from
     *
     * @return the amount of fluid emitted
     */
    public static int emit(Set<Direction> sides, @Nonnull FluidStack stack, TileEntity from) {
        if (stack.isEmpty() || sides.isEmpty()) {
            return 0;
        }
        FluidStack toSend = stack.copy();
        //Fake that we have one target given we know that no sides will overlap
        // This allows us to have slightly better performance
        FluidHandlerTarget target = new FluidHandlerTarget(stack);
        EmitUtils.forEachSide(from.getWorld(), from.getPos(), sides, (acceptor, side) -> {
            //Insert to access side
            Direction accessSide = side.getOpposite();
            //Collect cap
            CapabilityUtils.getCapability(acceptor, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, accessSide).ifPresent(handler -> {
                if (canFill(handler, toSend)) {
                    target.addHandler(accessSide, handler);
                }
            });
        });

        int curHandlers = target.getHandlers().size();
        if (curHandlers > 0) {
            Set<FluidHandlerTarget> targets = new ObjectOpenHashSet<>();
            targets.add(target);
            return EmitUtils.sendToAcceptors(targets, curHandlers, stack.getAmount(), toSend);
        }
        return 0;
    }

    public static boolean canFill(IFluidHandler handler, @Nonnull FluidStack stack) {
        return handler.fill(stack, FluidAction.SIMULATE) > 0;
    }
}
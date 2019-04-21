package mekanism.common.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.common.base.FluidHandlerTarget;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public final class PipeUtils {

    public static final FluidTankInfo[] EMPTY = new FluidTankInfo[]{};

    public static boolean isValidAcceptorOnSide(TileEntity tile, EnumFacing side) {
        if (tile == null || CapabilityUtils
              .hasCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite()) ||
              !CapabilityUtils
                    .hasCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite())) {
            return false;
        }

        IFluidHandler container = CapabilityUtils
              .getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());

        if (container == null) {
            return false;
        }

        IFluidTankProperties[] infoArray = container.getTankProperties();

        if (infoArray != null && infoArray.length > 0) {
            for (IFluidTankProperties info : infoArray) {
                if (info != null) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets all the acceptors around a tile entity.
     *
     * @return array of IFluidHandlers
     */
    public static IFluidHandler[] getConnectedAcceptors(BlockPos pos, World world) {
        IFluidHandler[] acceptors = new IFluidHandler[]{null, null, null, null, null, null};

        for (EnumFacing orientation : EnumFacing.VALUES) {
            TileEntity acceptor = world.getTileEntity(pos.offset(orientation));

            if (CapabilityUtils.hasCapability(acceptor, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                  orientation.getOpposite())) {
                IFluidHandler handler = CapabilityUtils
                      .getCapability(acceptor, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                            orientation.getOpposite());
                acceptors[orientation.ordinal()] = handler;
            }
        }

        return acceptors;
    }

    public static int sendToAcceptors(Set<FluidHandlerTarget> availableAcceptors, int totalAcceptors,
          FluidStack fluidToSend) {
        if (availableAcceptors.isEmpty() || totalAcceptors == 0) {
            return 0;
        }
        int sent = 0;
        int amountToSplit = fluidToSend.amount;
        int toSplitAmong = totalAcceptors;
        int amountPer = amountToSplit / toSplitAmong;
        boolean amountPerChanged = false;

        //Simulate addition
        for (FluidHandlerTarget target : availableAcceptors) {
            Map<EnumFacing, IFluidHandler> wrappers = target.getWrappers();
            for (Entry<EnumFacing, IFluidHandler> entry : wrappers.entrySet()) {
                EnumFacing side = entry.getKey();
                int amountNeeded = entry.getValue().fill(fluidToSend, false);
                boolean canGive = amountNeeded <= amountPer;
                //Add the amount
                target.addAmount(side, amountNeeded, canGive);
                if (canGive) {
                    //If we are giving it, then lower the amount we are checking/splitting
                    amountToSplit -= amountNeeded;
                    toSplitAmong--;
                    //Only recalculate it if it is not willing to accept/doesn't want the
                    // full per side split
                    if (amountNeeded != amountPer && toSplitAmong != 0) {
                        amountPer = amountToSplit / toSplitAmong;
                        amountPerChanged = true;
                    }
                }
            }
        }

        //Only run this if we changed the amountPer from when we first ran things
        while (amountPerChanged) {
            amountPerChanged = false;
            double amountPerLast = amountPer;
            for (FluidHandlerTarget target : availableAcceptors) {
                if (target.noneNeeded()) {
                    continue;
                }
                //Use an iterator rather than a copy of the keyset of the needed submap
                // This allows for us to remove it once we find it without  having to
                // start looping again or make a large number of copies of the set
                Iterator<Entry<EnumFacing, Integer>> iterator = target.getNeededIterator();
                while (iterator.hasNext()) {
                    Entry<EnumFacing, Integer> needInfo = iterator.next();
                    Integer amountNeeded = needInfo.getValue();
                    if (amountNeeded <= amountPer) {
                        target.addGiven(needInfo.getKey(), amountNeeded);
                        //Remove it as it no longer valid
                        iterator.remove();
                        //Adjust the energy split
                        amountToSplit -= amountNeeded;
                        toSplitAmong--;
                        //Only recalculate it if it is not willing to accept/doesn't want the
                        // full per side split
                        if (amountNeeded != amountPer && toSplitAmong != 0) {
                            amountPer = amountToSplit / toSplitAmong;
                            if (!amountPerChanged && amountPer != amountPerLast) {
                                //We changed our amount so set it back to true so that we know we need
                                // to loop over things again
                                amountPerChanged = true;
                                //Continue checking things in case we happen to be
                                // getting things in a bad order so that we don't recheck
                                // the same values many times
                            }
                        }
                    }
                }
            }
        }

        //Give them all the energy we calculated they deserve/want
        for (FluidHandlerTarget target : availableAcceptors) {
            sent += target.sendGivenWithDefault(amountPer);
        }
        return sent;
    }

    /**
     * Emits fluid from a central block by splitting the received stack among the sides given.
     *
     * @param sides - the list of sides to output from
     * @param stack - the stack to output
     * @param from - the TileEntity to output from
     * @return the amount of gas emitted
     */
    public static int emit(Set<EnumFacing> sides, FluidStack stack, TileEntity from) {
        if (stack == null || stack.amount == 0) {
            return 0;
        }
        //Fake that we have one target given we know that no sides will overlap
        // This allows us to have slightly better performance
        FluidHandlerTarget target = new FluidHandlerTarget(stack);
        int totalAcceptors = 0;
        for (EnumFacing orientation : sides) {
            TileEntity acceptor = from.getWorld().getTileEntity(from.getPos().offset(orientation));
            if (acceptor == null) {
                continue;
            }
            EnumFacing opposite = orientation.getOpposite();
            if (CapabilityUtils.hasCapability(acceptor, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opposite)) {
                IFluidHandler handler = CapabilityUtils.getCapability(acceptor,
                      CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opposite);
                if (handler != null && canFill(handler, stack)) {
                    target.addSide(opposite, handler);
                    totalAcceptors++;
                }
            }
        }
        if (target.hasAcceptors()) {
            Set<FluidHandlerTarget> targets = new HashSet<>();
            targets.add(target);
            return sendToAcceptors(targets, totalAcceptors, stack);
        }
        return 0;
    }

    public static FluidStack copy(FluidStack fluid, int amount) {
        FluidStack ret = fluid.copy();
        ret.amount = amount;

        return ret;
    }

    public static boolean canFill(IFluidHandler handler, FluidStack stack) {
        for (IFluidTankProperties props : handler.getTankProperties()) {
            if (props.canFillFluidType(stack)) {
                return true;
            }
        }

        return false;
    }

    public static boolean canDrain(IFluidHandler handler, FluidStack stack) {
        for (IFluidTankProperties props : handler.getTankProperties()) {
            if (props.canDrainFluidType(stack)) {
                return true;
            }
        }

        return false;
    }
}

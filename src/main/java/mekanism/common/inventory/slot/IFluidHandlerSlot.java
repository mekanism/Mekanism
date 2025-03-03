package mekanism.common.inventory.slot;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import java.util.Collection;
import java.util.Map;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackLinkedSet;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public interface IFluidHandlerSlot extends IInventorySlot {

    IExtendedFluidTank getFluidTank();

    boolean isDraining();

    boolean isFilling();

    void setDraining(boolean draining);

    void setFilling(boolean filling);

    default void handleTank(IInventorySlot outputSlot, ContainerEditMode editMode) {
        if (!isEmpty()) {
            if (editMode == ContainerEditMode.FILL) {
                drainTank(outputSlot);
            } else if (editMode == ContainerEditMode.EMPTY) {
                fillTank(outputSlot);
            } else if (editMode == ContainerEditMode.BOTH) {
                ItemStack stack = getStack();
                IFluidHandlerItem fluidHandlerItem = FluidInventorySlot.tryGetFluidHandlerUnstacked(stack);
                if (fluidHandlerItem != null) {
                    boolean hasEmpty = false;
                    for (int tank = 0, tanks = fluidHandlerItem.getTanks(); tank < tanks; tank++) {
                        FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tank);
                        if (fluidInTank.isEmpty()) {
                            hasEmpty = true;
                        } else if (!isDraining() && getFluidTank().insert(fluidInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < fluidInTank.getAmount()) {
                            //If we support either mode and our container is not empty or currently being filled, then drain the item into the tank
                            fillTank(outputSlot);
                            return;
                        }
                    }
                    if (isFilling()) {
                        //if we were filling, but can no longer fill the tank, attempt to move the item to the output slot
                        if (moveItem(outputSlot, stack)) {
                            setFilling(false);
                        }
                    }
                    //If we have no valid fluids/can't fill the tank with it, we return if there is at least
                    // one empty tank in the item so that we can then drain into it
                    else if (getFluidTank().isEmpty() && hasEmpty || isDraining() || fluidHandlerItem.fill(getFluidTank().getFluid().copy(), FluidAction.SIMULATE) > 0) {
                        //we return if there is at least one empty tank in the item so that we can then drain into it
                        drainTank(outputSlot);
                    }
                }
            }
        }
    }

    /**
     * Fills tank from slot
     *
     * @param outputSlot The slot to move our container to after draining the item.
     */
    default void fillTank(IInventorySlot outputSlot) {
        //Try filling from the tank's item
        IFluidHandlerItem itemFluidHandler = Capabilities.FLUID.getCapability(getStack());
        if (itemFluidHandler != null) {
            int itemTanks = itemFluidHandler.getTanks();
            if (itemTanks == 1) {
                //If we only have one tank just directly check against that fluid instead of performing extra calculations to properly handle multiple tanks
                FluidStack fluidInItem = itemFluidHandler.getFluidInTank(0);
                if (!fluidInItem.isEmpty() && getFluidTank().isFluidValid(fluidInItem)) {
                    //If we have a fluid that is valid for our fluid handler, attempt to drain it into our fluid handler
                    drainItemAndMove(outputSlot, fluidInItem);
                }
            } else if (itemTanks > 1) {
                //If we have more than one tank in our item then handle calculating the different drains that will occur for filling our fluid handler
                // We start by gathering all the fluids in the item that we are able to drain and are valid for the tank,
                // combining same fluid types into a single fluid stack
                //If we found any fluids that we can drain, attempt to drain them into our item
                for (FluidStack knownFluid : gatherKnownFluids(itemFluidHandler, itemTanks)) {
                    if (drainItemAndMove(outputSlot, knownFluid) && isEmpty()) {
                        //If we moved the item after draining it and we now don't have an item to try and fill
                        // then just exit instead of checking the other types of fluids
                        //TODO: Eventually fix the case where the item we are draining has multiple
                        // types of fluids so we may not actually want to move it immediately
                        // Note: Not sure what a good middle ground is because if the item can stack like buckets
                        // then how do we know when to move it
                        break;
                    }
                }
            }
        }
    }

    /**
     * Drains tank into slot
     *
     * @param outputSlot The slot to move our container to after draining the tank.
     */
    default void drainTank(IInventorySlot outputSlot) {
        //Verify we have an item, we have tanks that may need to be drained, and that our item is a fluid handler
        // This handles making sure it has a fluid handler currently, even if it may have one when it isn't stacked
        if (Capabilities.FLUID.hasCapability(getStack())) {
            FluidStack fluidInTank = getFluidTank().getFluid();
            if (!fluidInTank.isEmpty()) {
                //If we have a fluid attempt to drain it into our item
                FluidStack simulatedDrain = getFluidTank().extract(fluidInTank.getAmount(), Action.SIMULATE, AutomationType.INTERNAL);
                if (simulatedDrain.isEmpty()) {
                    //If we cannot actually drain from our fluid handler then just exit early
                    return;
                }
                ItemStack inputCopy = getStack().copyWithCount(1);
                IFluidHandlerItem fluidHandlerItem = Capabilities.FLUID.getCapability(inputCopy);
                //The capability should be present based on checks that happen before this method, but verify to make sure it is present
                if (fluidHandlerItem != null) {
                    //Fill the stack, note our stack is a copy so this is how we simulate to get the proper "container" item,
                    // and it does not actually matter that we are directly executing on the item
                    int toDrain = fluidHandlerItem.fill(fluidInTank.copy(), FluidAction.EXECUTE);
                    if (toDrain == 0) {
                        //If we cannot actually fill the item then just exit early
                        return;
                    }
                    if (getCount() == 1) {
                        IFluidHandlerItem containerCap = Capabilities.FLUID.getCapability(fluidHandlerItem.getContainer());
                        if (containerCap != null && containerCap.fill(fluidInTank.copy(), FluidAction.SIMULATE) > 0) {
                            //If we have a single item in the input slot, and we can continue to fill it after
                            // our current fill, then mark that we don't want to move it to the output slot, yet
                            // Additionally we replace our input item with its container
                            setStack(fluidHandlerItem.getContainer());
                            //Mark that we are currently draining
                            setDraining(true);
                            //Actually remove the fluid from our handler
                            MekanismUtils.logMismatchedStackSize(getFluidTank().shrinkStack(toDrain, Action.EXECUTE), toDrain);
                            return;
                        }
                    }
                    //If we can move it to the output slot then actually drain our tank
                    if (moveItem(outputSlot, fluidHandlerItem.getContainer())) {
                        //Actually remove the fluid from our handler
                        MekanismUtils.logMismatchedStackSize(getFluidTank().shrinkStack(toDrain, Action.EXECUTE), toDrain);
                        //Mark we are no longer draining (as we have moved the item to the output slot)
                        setDraining(false);
                    }
                }
            }
        }
    }

    /**
     * Fills our fluid handler from the item and then moves the item to the given output slot. If it won't be able to move to the output slot, then we do not move it or
     * drain our item into the fluid handler.
     *
     * @param outputSlot      The slot our item will be moved to afterwards
     * @param fluidToTransfer The fluid we are draining from the item. This should be known to not be empty, and to have passed any validity checks.
     *
     * @return True if we can drain the fluid from the item and the item after being drained can (and was) moved to the output slot, false otherwise
     */
    private boolean drainItemAndMove(IInventorySlot outputSlot, FluidStack fluidToTransfer) {
        FluidStack simulatedRemainder = getFluidTank().insert(fluidToTransfer, Action.SIMULATE, AutomationType.INTERNAL);
        int remainder = simulatedRemainder.getAmount();
        int toTransfer = fluidToTransfer.getAmount();
        if (remainder == toTransfer) {
            //If we cannot actually fill our fluid handler then just exit early
            return false;
        }
        ItemStack stack = getStack();
        if (Capabilities.FLUID.getCapability(stack) == null) {
            //If the stack doesn't have a capability just exit. There may be cases like our fluid tank where it will have a capability
            // if the stack size is one, but not when the stack size is greater
            return false;
        }
        ItemStack input = stack.copyWithCount(1);
        IFluidHandlerItem fluidHandlerItem = Capabilities.FLUID.getCapability(input);
        if (fluidHandlerItem == null) {
            //The capability should be present based on checks that happen before this method, but if for some reason it isn't just exit
            return false;
        }
        //Drain the stack, note our stack is a copy so this is how we simulate to get the proper "container" item,
        // and it does not actually matter that we are directly executing on the item
        FluidStack drained = fluidHandlerItem.drain(fluidToTransfer.copyWithAmount(toTransfer - remainder), FluidAction.EXECUTE);
        if (drained.isEmpty()) {
            //If we cannot actually drain from the item then just exit early
            return false;
        }
        if (getCount() == 1) {
            IFluidHandlerItem containerCap = Capabilities.FLUID.getCapability(fluidHandlerItem.getContainer());
            if (containerCap != null && !containerCap.drain(Integer.MAX_VALUE, FluidAction.SIMULATE).isEmpty()) {
                //If we have a single item in the input slot, and we can continue to drain from it
                // after our current drain, then we allow for draining and actually fill our handler
                // Additionally we replace our input item with its container
                setStack(fluidHandlerItem.getContainer());
                getFluidTank().insert(drained, Action.EXECUTE, AutomationType.INTERNAL);
                //Mark that we are currently filling
                setFilling(true);
                return true;
            }
        }
        //Otherwise, we try to move the item to the output and then actually fill it
        if (moveItem(outputSlot, fluidHandlerItem.getContainer())) {
            //Actually fill our handler with the fluid
            getFluidTank().insert(drained, Action.EXECUTE, AutomationType.INTERNAL);
            return true;
        }
        return false;
    }

    /**
     * Tries to move a stack from our slot to the output slot
     *
     * @param outputSlot  The slot we are trying to move our item to
     * @param stackToMove The stack we are moving, this is our container
     *
     * @return True if we are able to move the stack and did so, false otherwise
     */
    private boolean moveItem(IInventorySlot outputSlot, ItemStack stackToMove) {
        if (outputSlot.isEmpty()) {
            outputSlot.setStack(stackToMove);
        } else {
            ItemStack outputStack = outputSlot.getStack();
            if (!ItemStack.isSameItemSameComponents(outputStack, stackToMove) || outputStack.getCount() >= outputSlot.getLimit(outputStack)) {
                //We won't be able to move our container to the output slot so exit
                return false;
            }
            MekanismUtils.logMismatchedStackSize(outputSlot.growStack(1, Action.EXECUTE), 1);
        }
        //Note: We do not need to call onContentsChanged, because it will be done due to the stack changing from calling shrinkStack
        MekanismUtils.logMismatchedStackSize(shrinkStack(1, Action.EXECUTE), 1);
        return true;
    }

    /**
     * Fills tank from slot, ensuring the stack's count is one, and does not move it to an output slot afterwards
     */
    default boolean fillTank() {
        if (getCount() == 1) {
            //Try filling from the tank's item
            IFluidHandlerItem itemFluidHandler = Capabilities.FLUID.getCapability(getStack());
            if (itemFluidHandler != null) {
                int tanks = itemFluidHandler.getTanks();
                if (tanks == 1) {
                    //If we only have one tank just directly check against that fluid instead of performing extra calculations to properly handle multiple tanks
                    FluidStack fluidInItem = itemFluidHandler.getFluidInTank(0);
                    if (!fluidInItem.isEmpty() && getFluidTank().isFluidValid(fluidInItem)) {
                        //If we have a fluid that is valid for our fluid handler, attempt to drain it into our fluid handler
                        if (fillHandlerFromOther(getFluidTank(), itemFluidHandler, fluidInItem)) {
                            //Update the stack to the empty container
                            setStack(itemFluidHandler.getContainer());
                            return true;
                        }
                    }
                } else if (tanks > 1) {
                    //If we have more than one tank in our item then handle calculating the different drains that will occur for filling our fluid handler
                    // We start by gathering all the fluids in the item that we are able to drain and are valid for the tank,
                    // combining same fluid types into a single fluid stack
                    Collection<FluidStack> knownFluids = gatherKnownFluids(itemFluidHandler, tanks);
                    if (!knownFluids.isEmpty()) {
                        //If we found any fluids that we can drain, attempt to drain them into our item
                        boolean changed = false;
                        for (FluidStack knownFluid : knownFluids) {
                            if (fillHandlerFromOther(getFluidTank(), itemFluidHandler, knownFluid)) {
                                changed = true;
                            }
                        }
                        if (changed) {
                            //Update the stack to the empty container
                            setStack(itemFluidHandler.getContainer());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private Collection<FluidStack> gatherKnownFluids(IFluidHandlerItem itemFluidHandler, int tanks) {
        Map<FluidStack, FluidStack> knownFluids = new Object2ObjectOpenCustomHashMap<>(FluidStackLinkedSet.TYPE_AND_COMPONENTS);
        for (int tank = 0; tank < tanks; tank++) {
            FluidStack fluidInItem = itemFluidHandler.getFluidInTank(tank);
            if (!fluidInItem.isEmpty()) {
                //Note: We use fluid directly for looking it up as a key as they only compare on equals and hashcode
                FluidStack knownFluid = knownFluids.get(fluidInItem);
                //If we have a fluid that can be drained from the item and is valid then we add it to our known fluids
                if (knownFluid == null) {
                    if (!itemFluidHandler.drain(fluidInItem.copy(), FluidAction.SIMULATE).isEmpty() && getFluidTank().isFluidValid(fluidInItem)) {
                        //Note: While theoretically we could store the initial fluidInItem as they key as we don't mutate it...
                        // doing it this way allows for us to return the keySet from this method as the only thing we change (the amount)
                        // is not part of the hashCode or equals, so it will not cause things to break by mutating the key as well
                        FluidStack copy = fluidInItem.copy();
                        knownFluids.put(copy, copy);
                    }
                } else {
                    knownFluid.grow(fluidInItem.getAmount());
                }
            }
        }
        return knownFluids.keySet();
    }

    /**
     * Tries to drain the specified fluid from one fluid handler, while filling another fluid handler.
     *
     * @param handlerToFill  The fluid handler to fill
     * @param handlerToDrain The fluid handler to drain
     * @param fluid          The fluid to attempt to transfer
     *
     * @return True if we managed to transfer any contents, false otherwise
     */
    private boolean fillHandlerFromOther(IExtendedFluidTank handlerToFill, IFluidHandler handlerToDrain, FluidStack fluid) {
        //Check how much of this fluid type we are actually able to drain from the handler we are draining
        FluidStack simulatedDrain = handlerToDrain.drain(fluid.copy(), FluidAction.SIMULATE);
        if (!simulatedDrain.isEmpty()) {
            //Check how much of it we will be able to put into the handler we are filling
            FluidStack simulatedRemainder = getFluidTank().insert(simulatedDrain, Action.SIMULATE, AutomationType.INTERNAL);
            int remainder = simulatedRemainder.getAmount();
            int drained = simulatedDrain.getAmount();
            if (remainder < drained) {
                //Drain the handler to drain, filling the handler to fill while we are at it
                handlerToFill.insert(handlerToDrain.drain(fluid.copyWithAmount(drained - remainder), FluidAction.EXECUTE), Action.EXECUTE, AutomationType.INTERNAL);
                return true;
            }
        }
        return false;
    }
}
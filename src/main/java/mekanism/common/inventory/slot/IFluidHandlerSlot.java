package mekanism.common.inventory.slot;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import mekanism.api.AutomationType;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.access.InOutSlotItemAccess;
import mekanism.api.inventory.access.InventorySlotItemAccess;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public interface IFluidHandlerSlot extends IInventorySlot {

    IFluidTank getFluidTank();

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
                IFluidTank fluidTank = getFluidTank();
                //TODO - 26.1: validate this makes sense, and see if we need to do anything about oneByOne item access?
                ItemAccess access = new InOutSlotItemAccess(this, outputSlot);
                ResourceHandler<FluidResource> handler = Capabilities.FLUID.getCapability(access);
                if (handler != null) {
                    boolean hasEmpty = false;
                    for (int tank = 0, tanks = handler.size(); tank < tanks; tank++) {
                        FluidResource fluidInTank = handler.getResource(tank);
                        if (fluidInTank.isEmpty()) {
                            hasEmpty = true;
                        } else if (!isDraining()) {
                            boolean canFill;
                            try (Transaction simulation = Transaction.openRoot()) {
                                canFill = fluidTank.insert(fluidInTank, handler.getAmountAsInt(tank), simulation, AutomationType.INTERNAL) > 0;
                            }
                            if (canFill) {
                                //If we support either mode and our container is not empty or currently being filled, then drain the item into the tank
                                fillTank(outputSlot);
                                return;
                            }
                        }
                    }
                    if (isFilling()) {
                        //if we were filling, but can no longer fill the tank, attempt to move the item to the output slot
                        //TODO - 26.1: Figure out what the logic should be here, as moving the item is part of the item access' job now
                        //if (moveItem(outputSlot, getStack(), transaction)) {
                            setFilling(false);
                        //}
                    } else if (fluidTank.isEmpty() && hasEmpty || isDraining()) {
                        //If we have no valid fluids, we return if there is at least one empty tank in the item so that we can then drain into it
                        drainTank(outputSlot);
                    } else {
                        //try to fill the item from the fluid in the tank
                        boolean canDrain;
                        try (Transaction simulation = Transaction.openRoot()) {
                            canDrain = handler.insert(fluidTank.resource(), fluidTank.amountAsInt(), simulation) > 0;
                        }
                        if (canDrain) {
                            //If we can drain anything into it, then drain
                            drainTank(outputSlot);
                        }
                    }
                }
            }
        }
    }

    /// Fills tank from slot
    ///
    /// @param outputSlot The slot to move our container to after draining the item.
    default void fillTank(IInventorySlot outputSlot) {
        //Try filling from the tank's item
        //TODO - 26.1: validate this makes sense, and see if we need to do anything about oneByOne item access?
        ItemAccess access = new InOutSlotItemAccess(this, outputSlot);
        ResourceHandler<FluidResource> handler = Capabilities.FLUID.getCapability(access);
        if (handler != null) {
            //Start by gathering all the fluids in the item that are valid for the tank
            Object2IntMap<FluidResource> knownFluids = gatherKnownFluids(handler);
            for (ObjectIterator<Object2IntMap.Entry<FluidResource>> iterator = Object2IntMaps.fastIterator(knownFluids); iterator.hasNext(); ) {
                Object2IntMap.Entry<FluidResource> knownFluid = iterator.next();
                if (drainItemAndMove(outputSlot, knownFluid.getKey(), knownFluid.getIntValue()) && isEmpty()) {
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

    /// Drains tank into slot
    ///
    /// @param outputSlot The slot to move our container to after draining the tank.
    default void drainTank(IInventorySlot outputSlot) {
        //Verify we have an item, we have tanks that may need to be drained, and that our item is a fluid handler
        // This handles making sure it has a fluid handler currently, even if it may have one when it isn't stacked
        //TODO - 26.1: validate this makes sense, and see if we need to do anything about oneByOne item access?
        // I am guessing we should potentially be doing a oneByOne access here
        ItemAccess access = new InOutSlotItemAccess(this, outputSlot);
        ResourceHandler<FluidResource> handler = Capabilities.FLUID.getCapability(access);
        if (handler == null) {
            return;
        }
        IFluidTank fluidTank = getFluidTank();
        if (fluidTank.isEmpty()) {
            return;
        }
        FluidResource fluidType = fluidTank.resource();
        int fluidAmount = fluidTank.amountAsInt();
        try (Transaction transaction = Transaction.openRoot()) {
            //Fill the stack, note our stack is a copy so this is how we simulate to get the proper "container" item,
            // and it does not actually matter that we are directly executing on the item
            int toDrain = handler.insert(fluidType, fluidAmount, transaction);
            if (toDrain == 0) {
                //If we cannot actually fill the item then just exit early
                return;
            }
            //If we have a fluid attempt to drain it into our item
            int extractable = fluidTank.extract(fluidType, toDrain, transaction, AutomationType.INTERNAL);
            if (extractable < toDrain) {
                //If we cannot actually drain enough fluid from our fluid handler then just exit early
                return;
            }

            boolean draining = false;
            if (amountAsInt() == 1) {//TODO - 26.1: This amount check is AFTER it might have already moved out, so we likely need to capture the value before hand?
                //TODO - 26.1: Do we need to look up the cap again? In case the item got moved out? How do caps with item access work
                //ResourceHandler<FluidResource> containerCap = Capabilities.FLUID.getCapability(access);
                //if (containerCap != null) {
                try (Transaction simulation = Transaction.open(transaction)) {
                    //TODO - 26.1: Do we want to just pass a bucket's volume for the amount to transfer?
                    if (handler.insert(fluidType, fluidAmount, simulation) > 0) {
                        //If we have a single item in the input slot, and we can continue to fill it after
                        // our current fill, then mark that we are currently draining
                        draining = true;
                    }
                }
            }
            //TODO - 26.1: Do we want to do this in a custom item access so it happens when things actually move?
            //Mark we are no longer draining (as we have moved the item to the output slot)
            setDraining(draining);
            //Actually remove the fluid from our handler and fill the item handler
            transaction.commit();
        }
    }

    /// Fills our fluid handler from the item and then moves the item to the given output slot. If it won't be able to move to the output slot, then we do not move it or
    /// drain our item into the fluid handler.
    ///
    /// @param outputSlot      The slot our item will be moved to afterward
    /// @param fluidToTransfer The fluid we are draining from the item. This should be known to not be empty, and to have passed any validity checks.
    ///
    /// @return True if we can drain the fluid from the item and the item after being drained can (and was) moved to the output slot, false otherwise
    private boolean drainItemAndMove(IInventorySlot outputSlot, FluidResource fluidToTransfer, int amountToTransfer) {
        IFluidTank fluidTank = getFluidTank();
        int roomFor;
        try (Transaction simulation = Transaction.openRoot()) {
            roomFor = fluidTank.insert(fluidToTransfer, amountToTransfer, simulation, AutomationType.INTERNAL);
            if (roomFor == 0) {
                //If we cannot actually fill our fluid handler then just exit early
                return false;
            }
        }
        //TODO - 26.1: validate this makes sense, and see if we need to do anything about oneByOne item access?
        // I am guessing we should potentially be doing a oneByOne access here
        ItemAccess access = new InOutSlotItemAccess(this, outputSlot);
        ResourceHandler<FluidResource> handler = Capabilities.FLUID.getCapability(access);
        if (handler == null) {
            //If the stack doesn't have a capability just exit. There may be cases like our fluid tank where it will have a capability
            // if the stack size is one, but not when the stack size is greater
            return false;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            //Drain the stack, note our stack is a copy so this is how we simulate to get the proper "container" item,
            // and it does not actually matter that we are directly executing on the item
            int drained = handler.extract(fluidToTransfer, roomFor, transaction);
            if (drained == 0 || fluidTank.insert(fluidToTransfer, drained, transaction, AutomationType.INTERNAL) != drained) {
                //If we cannot actually drain from the item, or fill our tank with all of what we extracted from the item then just exit early
                return false;
            }
            if (amountAsInt() == 1) {//TODO - 26.1: This amount check is AFTER it might have already moved out, so we likely need to capture the value before hand?
                //TODO - 26.1: Do we need to look up the cap again? In case the item got moved out? How do caps with item access work
                //ResourceHandler<FluidResource> containerCap = Capabilities.FLUID.getCapability(access);
                //if (containerCap != null) {
                try (Transaction simulation = Transaction.open(transaction)) {
                    //TODO - 26.1: Do we want to just pass a bucket's volume for the amount to transfer?
                    if (handler.extract(fluidToTransfer, amountToTransfer, simulation) > 0) {
                        //If we have a single item in the input slot, and we can continue to drain from it
                        // after our current drain, then we are currently filling
                        setFilling(true);
                    }
                }
            }
            //Actually fill our handler with the fluid
            transaction.commit();
            return true;
        }
    }

    /// Tries to move a stack from our slot to the output slot
    ///
    /// @param outputSlot  The slot we are trying to move our item to
    /// @param stackToMove The stack we are moving, this is our container
    ///
    /// @return True if we are able to move the stack and did so, false otherwise
    private boolean moveItem(IInventorySlot outputSlot, ItemStack stackToMove, TransactionContext transaction) {//TODO - 26.1: Have this be handled by the item access?
        if (isEmpty() || stackToMove.isEmpty()) {
            //Nothing is stored so we can't extract it, or we aren't actually trying to move anything
            return false;
        }
        try (Transaction subTransaction = Transaction.open(transaction)) {
            ItemResource typeToMove = ItemResource.of(stackToMove);
            //TODO - 26.1: Should amount be gotten from the stack?
            int inserted = outputSlot.insert(typeToMove, 1, subTransaction, AutomationType.INTERNAL);
            if (inserted == 0) {
                //We won't be able to move our container to the output slot so exit
                return false;
            }
            //TODO - 26.1: Should we be passing in what the resource is to here?
            int removed = extract(resource(), 1, subTransaction, AutomationType.INTERNAL);
            if (removed == 0) {
                //Something failed extracting the item from our current slot, fail and roll back
                return false;
            }
            subTransaction.commit();
            return true;
        }
    }

    /// Fills tank from slot, ensuring the stack's count is one, and does not move it to an output slot afterward
    default boolean fillTank() {
        if (amountAsInt() == 1) {
            //TODO - 26.1: validate this makes sense, and see if we need to do anything about oneByOne item access? My guess is for this case
            // we don't need to but for others we will
            ItemAccess access = new InventorySlotItemAccess(this, AutomationType.INTERNAL);
            //Try filling from the tank's item
            ResourceHandler<FluidResource> handler = Capabilities.FLUID.getCapability(access);
            if (handler != null) {
                IFluidTank fluidTank = getFluidTank();
                //Start by gathering all the fluids in the item that are valid for the tank
                Object2IntMap<FluidResource> knownFluids = gatherKnownFluids(handler);
                if (!knownFluids.isEmpty()) {
                    //If we found any fluids that we can support if they are able to be drained, attempt to drain them into our item
                    for (ObjectIterator<Object2IntMap.Entry<FluidResource>> iterator = Object2IntMaps.fastIterator(knownFluids); iterator.hasNext(); ) {
                        Object2IntMap.Entry<FluidResource> knownFluid = iterator.next();
                        fillHandlerFromOther(fluidTank, handler, knownFluid.getKey(), knownFluid.getIntValue());
                    }
                }
            }
        }
        return false;
    }

    /// Gathers the total count of each fluid type stored in the handler that is valid for our tank. This does not make any determinations about whether the fluid can be
    /// extracted from the handler.
    private Object2IntMap<FluidResource> gatherKnownFluids(ResourceHandler<FluidResource> handler) {
        IFluidTank fluidTank = getFluidTank();
        Object2IntMap<FluidResource> knownFluids = new Object2IntOpenHashMap<>();
        for (int tank = 0, tanks = handler.size(); tank < tanks; tank++) {
            FluidResource fluidType = handler.getResource(tank);
            if (!fluidType.isEmpty() && fluidTank.isValid(fluidType)) {
                int stored = handler.getAmountAsInt(tank);
                //TODO - 26.1: Do we need to worry about integer overflow?
                knownFluids.mergeInt(fluidType, stored, Integer::sum);
            }
        }
        return knownFluids;
    }

    /// Tries to drain the specified fluid from one fluid handler, while filling another fluid handler.
    ///
    /// @param handlerToFill  The fluid handler to fill
    /// @param handlerToDrain The fluid handler to drain
    /// @param fluidType      The type of fluid to attempt to transfer
    /// @param amount         The amount of fluid to attempt to transfer
    ///
    /// @return True if we managed to transfer any contents, false otherwise
    private boolean fillHandlerFromOther(IFluidTank handlerToFill, ResourceHandler<FluidResource> handlerToDrain, FluidResource fluidType, int amount) {
        //Check how much of this fluid type we are actually able to drain from the handler we are draining
        int extracted;
        try (Transaction simulation = Transaction.openRoot()) {
            extracted = handlerToDrain.extract(fluidType, amount, simulation);
            if (extracted == 0) {
                return false;
            }
        }
        try (Transaction transaction = Transaction.openRoot()) {
            //Check how much of it we will be able to put into the handler we are filling
            int inserted = handlerToFill.insert(fluidType, extracted, transaction, AutomationType.INTERNAL);
            if (inserted == 0 || handlerToDrain.extract(fluidType, inserted, transaction) != inserted) {
                //If we failed to insert anything, or something went wrong extracting from the original handler the amount that we determined should fit,
                // roll back the transaction and bail
                return false;
            }
            //Commit the changes and return that we were able to transfer some contents
            transaction.commit();
            return true;
        }
    }
}
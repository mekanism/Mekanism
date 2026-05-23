package mekanism.common.inventory.slot;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Locale;
import mekanism.api.AutomationType;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.access.InventorySlotItemAccess;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.access.InOutSlotResourceItemAccess;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface IFluidHandlerSlot extends IInventorySlot {

    IFluidTank getFluidTank();

    default void resetLastTransferDirection() {
        setLastTransferDirection(LastTransferDirection.UNKNOWN);
    }

    LastTransferDirection getLastTransferDirection();

    void setLastTransferDirection(LastTransferDirection direction);

    @Nullable
    private <RESOURCE extends Resource> ResourceHandler<RESOURCE> getHandler(InOutSlotResourceItemAccess<RESOURCE> access) {
        ItemCapability<ResourceHandler<RESOURCE>, @NonNull ItemAccess> capability = access.getCapability();
        //TODO - 26.1: Can we somehow predict whether we should use a one by one access or not? The issue with not using a oneByOne access for things like buckets
        // is that their max stack size when filled is one, so then we can't move it all into the output slot (and have a successful exchange by doing so)
        ResourceHandler<RESOURCE> handler = access.oneByOne().getCapability(capability);
        /*if (access.getAmount() > 1) {
            if (handler == null) {
                //If there isn't a resource handler for the full access, and the access isn't already effectively a one by one access
                // See if the item exposes a capability only when it is not stacked
                return access.oneByOne().getCapability(capability);
            }
            //TODO - 26.1: Determine if we should try extracting/inserting based on what we have available to see if we should potentially try the one by one access regardless?
            // Or maybe we should just always use a one by one access?
        }*/
        return handler;
    }

    default void handleTank(IInventorySlot outputSlot, ContainerEditMode editMode) {
        if (!isEmpty()) {
            IFluidTank fluidTank = getFluidTank();
            InOutSlotResourceItemAccess<FluidResource> access = new InOutSlotResourceItemAccess<>(this, outputSlot, Capabilities.FLUID.item(), this::getLastTransferDirection, fluidTank.resource());
            ResourceHandler<FluidResource> handler = getHandler(access);
            if (handler == null) {
                return;
            }
            if (editMode == ContainerEditMode.FILL) {
                setLastTransferDirection(LastTransferDirection.DRAIN_INTO_ITEM);
                drainTankIntoSlot(fluidTank, handler);
            } else if (editMode == ContainerEditMode.EMPTY) {
                setLastTransferDirection(LastTransferDirection.FILL_FROM_ITEM);
                fillTankFromSlot(fluidTank, handler);
            } else if (editMode == ContainerEditMode.BOTH) {
                switch (getLastTransferDirection()) {
                    case UNKNOWN -> {
                        setLastTransferDirection(LastTransferDirection.DRAIN_INTO_ITEM);
                        if (!drainTankIntoSlot(fluidTank, handler)) {
                            //If we can't fill the slot from our tank, try to drain the slot into the tank, and if that fails move it into the output slot
                            setLastTransferDirection(LastTransferDirection.FILL_FROM_ITEM);
                            if (!tryFillOrMove(fluidTank, handler, outputSlot)) {
                                //If we weren't able to fill from it or move it to the output slot, just reset the last transfer direction to unknown,
                                // in case we are able to drain it before we can fill it
                                resetLastTransferDirection();
                            }
                        }
                    }
                    case FILL_FROM_ITEM -> tryFillOrMove(fluidTank, handler, outputSlot);
                    case DRAIN_INTO_ITEM -> {
                        if (!drainTankIntoSlot(fluidTank, handler)) {
                            //TODO - 26.1: If the handler is full (of the type we can provide), we should try to move it? Is it possible for it to get into this state?
                        }
                    }
                }
            }
        }
    }

    private boolean tryFillOrMove(IFluidTank fluidTank, ResourceHandler<FluidResource> handler, IInventorySlot outputSlot) {
        if (!fillTankFromSlot(fluidTank, handler)) {
            FluidResource storedFluid = fluidTank.resource();
            boolean invalid;
            if (storedFluid.isEmpty()) {
                invalid = true;
            } else {
                try (Transaction simulation = Transaction.openRoot()) {
                    //Note: This is a naive check as our fluid tank might have a rate limit that is lower than its max capacity
                    invalid = handler.extract(storedFluid, fluidTank.capacityAsInt(storedFluid), simulation) == 0;
                }
            }
            if (invalid) {
                //We can't fill the tank from the stored item, and we either:
                // - don't currently have any fluid stored so will never be able to accept any of the items
                // - even if we could accept our maximum capacity of the fluid, would not be able to extract any of it from the item
                // move the item to the output slot as we can't process it
                try (Transaction transaction = Transaction.openRoot()) {
                    //TODO: Do we have to handle if we have more than max int stored? None of our fluid slots currently support that, so for now it is fine
                    ItemResource storedType = resource();
                    int stored = amountAsInt();
                    int extracted = extract(storedType, stored, transaction, AutomationType.INTERNAL);
                    if (extracted == stored && outputSlot.insert(storedType, stored, transaction, AutomationType.INTERNAL) == stored) {
                        //If we managed to move it mark that we are no longer filling from the slot
                        transaction.commit();
                        return true;
                    }
                    //TODO - 26.1: Should we be resetting it to unknown if we failed to move it?
                    return false;
                }
            }
        }
        return true;
    }

    /// Fills tank from slot
    ///
    /// @param outputSlot The slot to move our container to after draining the item.
    default void fillTankFromSlot(IInventorySlot outputSlot) {
        if (!isEmpty()) {
            //Try filling from the tank's item
            IFluidTank fluidTank = getFluidTank();
            InOutSlotResourceItemAccess<FluidResource> access = new InOutSlotResourceItemAccess<>(this, outputSlot, Capabilities.FLUID.item(), this::getLastTransferDirection, fluidTank.resource());
            ResourceHandler<FluidResource> handler = getHandler(access);
            if (handler != null) {
                setLastTransferDirection(LastTransferDirection.FILL_FROM_ITEM);
                fillTankFromSlot(fluidTank, handler);
            }
        }
    }

    private boolean fillTankFromSlot(IFluidTank fluidTank, ResourceHandler<FluidResource> handler) {
        if (!fluidTank.isEmpty()) {
            return fillNonEmptyTank(fluidTank, handler);
        }
        //Start by gathering all the fluids in the item that are valid for the tank
        Object2IntMap<FluidResource> knownFluids = gatherKnownFluids(fluidTank, handler);
        for (ObjectIterator<Object2IntMap.Entry<FluidResource>> iterator = Object2IntMaps.fastIterator(knownFluids); iterator.hasNext(); ) {
            Object2IntMap.Entry<FluidResource> knownFluid = iterator.next();
            FluidResource fluidToTransfer = knownFluid.getKey();
            int roomFor;
            try (Transaction simulation = Transaction.openRoot()) {
                roomFor = fluidTank.insert(fluidToTransfer, knownFluid.getIntValue(), simulation, AutomationType.INTERNAL);
                if (roomFor == 0) {
                    //If we cannot actually fill our fluid handler then just exit early
                    continue;
                }
            }
            try (Transaction transaction = Transaction.openRoot()) {
                //Drain the stack, note our stack is a copy so this is how we simulate to get the proper "container" item,
                // and it does not actually matter that we are directly executing on the item
                int drained = handler.extract(fluidToTransfer, roomFor, transaction);
                if (drained > 0 && fluidTank.insert(fluidToTransfer, drained, transaction, AutomationType.INTERNAL) == drained) {
                    //If we were able to extract something, and insert the corresponding amount from the original handler
                    //Commit the changes to the transaction
                    transaction.commit();
                    //Note: We can just exit as if we inserted something into our singular fluid tank, we can't insert a different type as well
                    //TODO - 26.1: Validate we don't have any "voiding" fluid tanks that are exposed to this, namely the creative fluid tank
                    return true;
                }
            }
        }
        return false;
    }

    /// Drains tank into slot
    ///
    /// @param outputSlot The slot to move our container to after draining the tank.
    default void drainTankIntoSlot(IInventorySlot outputSlot) {
        if (!isEmpty()) {
            //Verify we have an item, we have tanks that may need to be drained, and that our item is a fluid handler
            // This handles making sure it has a fluid handler currently, even if it may have one when it isn't stacked
            IFluidTank fluidTank = getFluidTank();
            InOutSlotResourceItemAccess<FluidResource> access = new InOutSlotResourceItemAccess<>(this, outputSlot, Capabilities.FLUID.item(), this::getLastTransferDirection, fluidTank.resource());
            ResourceHandler<FluidResource> handler = getHandler(access);
            if (handler != null) {
                setLastTransferDirection(LastTransferDirection.DRAIN_INTO_ITEM);
                drainTankIntoSlot(fluidTank, handler);
            }
        }
    }

    private boolean drainTankIntoSlot(IFluidTank fluidTank, ResourceHandler<FluidResource> handler) {
        if (fluidTank.isEmpty()) {
            return false;
        }
        FluidResource fluidType = fluidTank.resource();
        int fluidAmount = fluidTank.amountAsInt();
        try (Transaction transaction = Transaction.openRoot()) {
            //Fill the stack, note our stack is a copy so this is how we simulate to get the proper "container" item,
            // and it does not actually matter that we are directly executing on the item
            int toDrain = handler.insert(fluidType, fluidAmount, transaction);
            if (toDrain > 0 && fluidTank.extract(fluidType, toDrain, transaction, AutomationType.INTERNAL) == toDrain) {
                //If we were able to extract something, and insert the corresponding amount from the original handler
                //Commit the changes to the transaction
                transaction.commit();
                return true;
            }
        }
        return false;
    }

    /// Fills tank from slot, and does not move it to an output slot afterward
    default boolean fillTankFromSlot() {
        if (!isEmpty()) {
            ItemAccess access = new InventorySlotItemAccess(this, AutomationType.INTERNAL);
            //Note: We explicitly do not bother getting a one by one access here, as we only have the single slot,
            // so either we can act on the whole stack or we can't, doing one by one won't change anything
            ResourceHandler<FluidResource> handler = Capabilities.FLUID.getCapability(access);
            if (handler != null) {
                //Unused, but we set it anyway
                setLastTransferDirection(LastTransferDirection.FILL_FROM_ITEM);
                //Try filling from the tank's item
                IFluidTank fluidTank = getFluidTank();
                if (!fluidTank.isEmpty()) {
                    return fillNonEmptyTank(fluidTank, handler);
                }
                //Start by gathering all the fluids in the item that are valid for the tank
                Object2IntMap<FluidResource> knownFluids = gatherKnownFluids(fluidTank, handler);
                if (!knownFluids.isEmpty()) {
                    //If we found any fluids that we can support if they are able to be drained, attempt to drain them into our item
                    for (ObjectIterator<Object2IntMap.Entry<FluidResource>> iterator = Object2IntMaps.fastIterator(knownFluids); iterator.hasNext(); ) {
                        Object2IntMap.Entry<FluidResource> knownFluid = iterator.next();
                        FluidResource fluidToTransfer = knownFluid.getKey();
                        //Check how much of this fluid type we are actually able to drain from the handler we are draining
                        int extracted;
                        try (Transaction simulation = Transaction.openRoot()) {
                            extracted = handler.extract(fluidToTransfer, knownFluid.getIntValue(), simulation);
                            if (extracted == 0) {
                                continue;
                            }
                        }
                        try (Transaction transaction = Transaction.openRoot()) {
                            //Check how much of it we will be able to put into the handler we are filling
                            int inserted = fluidTank.insert(fluidToTransfer, extracted, transaction, AutomationType.INTERNAL);
                            if (inserted > 0 && handler.extract(fluidToTransfer, inserted, transaction) == inserted) {
                                //If we were able to insert something, and extract the corresponding amount from the original handler
                                //Commit the changes to the sub transaction
                                transaction.commit();
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean fillNonEmptyTank(IFluidTank fluidTank, ResourceHandler<FluidResource> handler) {
        FluidResource fluidType = fluidTank.resource();
        int amountNeeded = fluidTank.getNeededAsInt(fluidType);
        if (amountNeeded == 0) {
            return false;
        }
        int available;
        try (Transaction simulation = Transaction.openRoot()) {
            available = handler.extract(fluidType, amountNeeded, simulation);
            if (available == 0) {
                return false;
            }
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int accepted = fluidTank.insert(fluidType, available, transaction, AutomationType.INTERNAL);
            if (accepted > 0 && handler.extract(fluidType, accepted, transaction) == accepted) {
                transaction.commit();
                return true;
            }
            return false;
        }
    }

    /// Gathers the total amount of each fluid type stored in the handler that is valid for our tank, clamped at the amount we need for the tank. This does not make any
    /// determinations about whether the fluid can be extracted from the handler, but does assume the passed fluid tank is currently empty.
    private Object2IntMap<FluidResource> gatherKnownFluids(IFluidTank fluidTank, ResourceHandler<FluidResource> handler) {
        Object2IntMap<FluidResource> knownFluids = new Object2IntOpenHashMap<>();
        for (int tank = 0, tanks = handler.size(); tank < tanks; tank++) {
            FluidResource fluidType = handler.getResource(tank);
            if (!fluidType.isEmpty() && fluidTank.isValid(fluidType)) {
                int amountKnown = knownFluids.getOrDefault(fluidType, 0);
                int tankCapacity = fluidTank.capacityAsInt(fluidType);
                if (amountKnown < tankCapacity) {
                    int stored = handler.getAmountAsInt(tank);
                    if (stored <= tankCapacity - amountKnown) {
                        knownFluids.put(fluidType, tankCapacity);
                    } else {
                        //Note: We know this can't overflow, as tank capacity fits within an int
                        knownFluids.put(fluidType, stored + amountKnown);
                    }
                }
            }
        }
        return knownFluids;
    }

    enum LastTransferDirection implements StringRepresentable {
        UNKNOWN,
        FILL_FROM_ITEM,
        DRAIN_INTO_ITEM;

        public static final Codec<LastTransferDirection> CODEC = StringRepresentable.fromEnum(LastTransferDirection::values);

        private final String serializedName;

        LastTransferDirection() {
            this.serializedName = name().toLowerCase(Locale.ROOT);
        }

        @NonNull
        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
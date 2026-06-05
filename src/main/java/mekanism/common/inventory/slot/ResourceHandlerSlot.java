package mekanism.common.inventory.slot;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.resource.ResourceContainerWrapper;
import mekanism.common.attachments.containers.type.ResourceContainerType;
import mekanism.common.inventory.access.InOutSlotResourceItemAccess;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public abstract class ResourceHandlerSlot extends BasicInventorySlot {

    private LastTransferDirection lastTransferDirection = LastTransferDirection.UNKNOWN;

    protected ResourceHandlerSlot(BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert,
          @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, ConstantPredicates.alwaysTrue(), listener, x, y);
        //Note: We pass alwaysTrue as the validator, so that if a mod only exposes a resource handler on the filled item or when the item isn't stacked
        // then we don't have it all of a sudden being invalid after it is emptied
        //TODO: Eventually maybe we want to somehow enforce what the max stack size is for a given item and mark it as able to be accepted
        // but only a single one of it so that we can provide the short circuit "is ever valid" check to mods querying our item handlers
        // but at least for now given we fail fast, it shouldn't be *that* big a deal
        // Similarly, this also means we don't currently allow inserting stacked items, which is probably correct, though if something tries to
        // insert it stacked, and it would have a capability and be valid if they tried with only one item, we don't accept it
        // (instead of only accepting a single item). This is the potentially more important reason why to address this comment
    }

    public void resetLastTransferDirection() {
        setLastTransferDirection(LastTransferDirection.UNKNOWN);
    }

    public LastTransferDirection getLastTransferDirection() {
        return lastTransferDirection;
    }

    public void setLastTransferDirection(LastTransferDirection direction) {
        this.lastTransferDirection = direction;
    }

    @Override
    public void onContentsChanged(LargeResourceStack<ItemResource> originalState) {
        super.onContentsChanged(originalState);
        if (isEmpty()) {
            //If we are now empty, reset the last transfer direction as it is no longer valid
            resetLastTransferDirection();
        }
    }

    @Override
    public void copyContents(IResourceContainer<ItemResource> other, @Nullable TransactionContext transaction) {
        if (other instanceof ResourceContainerWrapper<ItemResource, ?> wrapper) {
            other = wrapper.getInternal();
        }
        super.copyContents(other, transaction);
        if (other instanceof ResourceHandlerSlot otherSlot) {
            setLastTransferDirection(otherSlot.getLastTransferDirection());
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        super.serialize(output);
        //TODO - 1.21: This doesn't get persisted anymore when breaking blocks that have fluid inventory slots
        if (lastTransferDirection != LastTransferDirection.UNKNOWN) {
            output.store(SerializationConstants.LAST_TRANSFER_DIRECTION, LastTransferDirection.CODEC, lastTransferDirection);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        super.deserialize(input);
        setLastTransferDirection(input.read(SerializationConstants.LAST_TRANSFER_DIRECTION, LastTransferDirection.CODEC).orElse(LastTransferDirection.UNKNOWN));
    }

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

    protected <RESOURCE extends Resource> void handleContainer(IResourceContainer<RESOURCE> resourceContainer, IInventorySlot outputSlot, ContainerEditMode editMode,
          ItemCapability<ResourceHandler<RESOURCE>, @NonNull ItemAccess> itemCapability) {
        if (!isEmpty()) {
            InOutSlotResourceItemAccess<RESOURCE> access = new InOutSlotResourceItemAccess<>(this, outputSlot, itemCapability, this::getLastTransferDirection,
                  resourceContainer.resource());
            ResourceHandler<RESOURCE> handler = getHandler(access);
            if (handler != null) {
                switch (editMode) {
                    case FILL -> {
                        setLastTransferDirection(LastTransferDirection.DRAIN_INTO_ITEM);
                        drainContainerIntoSlot(resourceContainer, handler);
                    }
                    case EMPTY -> {
                        setLastTransferDirection(LastTransferDirection.FILL_FROM_ITEM);
                        fillContainerFromSlot(resourceContainer, handler);
                    }
                    case BOTH -> {
                        switch (getLastTransferDirection()) {
                            case UNKNOWN -> {
                                setLastTransferDirection(LastTransferDirection.DRAIN_INTO_ITEM);
                                if (!drainContainerIntoSlot(resourceContainer, handler)) {
                                    //If we can't fill the slot from our container, try to drain the slot into the container, and if that fails move it into the output slot
                                    setLastTransferDirection(LastTransferDirection.FILL_FROM_ITEM);
                                    if (!tryFillOrMove(resourceContainer, handler, outputSlot)) {
                                        //If we weren't able to fill from it or move it to the output slot, just reset the last transfer direction to unknown,
                                        // in case we are able to drain it before we can fill it
                                        resetLastTransferDirection();
                                    }
                                }
                            }
                            case FILL_FROM_ITEM -> tryFillOrMove(resourceContainer, handler, outputSlot);
                            case DRAIN_INTO_ITEM -> {
                                if (!drainContainerIntoSlot(resourceContainer, handler)) {
                                    //TODO - 26.1: If the handler is full (of the type we can provide), we should try to move it? Is it possible for it to get into this state?
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private <RESOURCE extends Resource> boolean tryFillOrMove(IResourceContainer<RESOURCE> resourceContainer, ResourceHandler<RESOURCE> handler, IInventorySlot outputSlot) {
        if (!fillContainerFromSlot(resourceContainer, handler)) {
            RESOURCE storedResource = resourceContainer.resource();
            boolean invalid;
            if (storedResource.isEmpty()) {
                invalid = true;
            } else {
                try (Transaction simulation = Transaction.openRoot()) {
                    //Note: This is a naive check as our resource container might have a rate limit that is lower than its max capacity
                    invalid = handler.extract(storedResource, resourceContainer.capacityAsInt(storedResource), simulation) == 0;
                }
            }
            if (invalid) {
                //We can't fill the container from the stored item, and we either:
                // - don't currently have any resource stored so will never be able to accept any of the items
                // - even if we could accept our maximum capacity of the resource, would not be able to extract any of it from the item
                // move the item to the output slot as we can't process it
                try (Transaction transaction = Transaction.openRoot()) {
                    //TODO: Do we have to handle if we have more than max int stored? None of our resource slots currently support that, so for now it is fine
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

    /// Fills the container from the slot, and does not move it to an output slot afterward
    protected <RESOURCE extends Resource> boolean fillContainerFromSlot(IResourceContainer<RESOURCE> resourceContainer,
          ItemCapability<ResourceHandler<RESOURCE>, @NonNull ItemAccess> itemCapability) {
        if (!isEmpty()) {
            //Note: We explicitly do not bother getting a one by one access here, as we only have the single slot,
            // so either we can act on the whole stack or we can't, doing one by one won't change anything
            ResourceHandler<RESOURCE> handler = asItemAccess().getCapability(itemCapability);
            if (handler != null) {
                //Unused, but we set it anyway
                setLastTransferDirection(LastTransferDirection.FILL_FROM_ITEM);
                return fillContainerFromSlot(resourceContainer, handler);
            }
        }
        return false;
    }

    /// Fills the container from the slot
    ///
    /// @param outputSlot The slot to move our container to after draining the item.
    protected <RESOURCE extends Resource> void fillContainerFromSlot(IResourceContainer<RESOURCE> resourceContainer, IInventorySlot outputSlot,
          ItemCapability<ResourceHandler<RESOURCE>, @NonNull ItemAccess> itemCapability) {
        if (!isEmpty()) {
            //Try filling from the slot's item
            InOutSlotResourceItemAccess<RESOURCE> access = new InOutSlotResourceItemAccess<>(this, outputSlot, itemCapability, this::getLastTransferDirection,
                  resourceContainer.resource());
            ResourceHandler<RESOURCE> handler = getHandler(access);
            if (handler != null) {
                setLastTransferDirection(LastTransferDirection.FILL_FROM_ITEM);
                fillContainerFromSlot(resourceContainer, handler);
            }
        }
    }

    private <RESOURCE extends Resource> boolean fillContainerFromSlot(IResourceContainer<RESOURCE> resourceContainer, ResourceHandler<RESOURCE> handler) {
        if (!resourceContainer.isEmpty()) {
            RESOURCE resource = resourceContainer.resource();
            int amountNeeded = resourceContainer.getNeededAsInt(resource);
            return amountNeeded > 0 && fillContainerFromSlot(resourceContainer, handler, resource, amountNeeded);
        }
        //Start by gathering all the resources in the item that are valid for the container
        Set<RESOURCE> knownResources = new HashSet<>();
        for (int container = 0, containers = handler.size(); container < containers; container++) {
            RESOURCE resource = handler.getResource(container);
            if (!resource.isEmpty() && knownResources.add(resource) && resourceContainer.isValidForInsertion(resource, AutomationType.INTERNAL)) {
                //If we haven't tried to process this resource yet, and it is valid for insertion into our container
                if (fillContainerFromSlot(resourceContainer, handler, resource, resourceContainer.capacityAsInt(resource))) {
                    //Note: We can just exit as if we inserted something into our singular resource container, we can't insert a different type as well
                    //TODO - 26.1: Validate we don't have any "voiding" resource containers that are exposed to this, namely the creative fluid tank
                    return true;
                }
            }
        }
        return false;
    }

    private <RESOURCE extends Resource> boolean fillContainerFromSlot(IResourceContainer<RESOURCE> resourceContainer, ResourceHandler<RESOURCE> handler, RESOURCE resource,
          int amountNeeded) {
        if (amountNeeded == 0) {
            return false;
        }
        int roomFor;
        try (Transaction simulation = Transaction.openRoot()) {
            //Check how much we can actually insert into our container in case it has a rate limit and can't accept everything it needs at once
            roomFor = resourceContainer.insert(resource, amountNeeded, simulation, AutomationType.INTERNAL);
            if (roomFor == 0) {
                return false;
            }
        }
        try (Transaction transaction = Transaction.openRoot()) {
            //Extract the amount we simulated we can accept from the handler. It is important this happens before we then insert into our rate limit
            // based container as if the handler a stacked item, then it might only be able to provide things in discrete increments
            int extracted = handler.extract(resource, roomFor, transaction);
            if (extracted > 0 && resourceContainer.insert(resource, extracted, transaction, AutomationType.INTERNAL) == extracted) {
                //If we were able to accept  something, and extract the corresponding amount from the original handler
                //Commit the changes to the transaction
                transaction.commit();
                return true;
            }
            return false;
        }
    }

    /// Fills the container from the slot, and does not move it to an output slot afterward
    protected <RESOURCE extends Resource> boolean drainContainerIntoSlot(IResourceContainer<RESOURCE> resourceContainer,
          ItemCapability<ResourceHandler<RESOURCE>, @NonNull ItemAccess> itemCapability) {
        if (!isEmpty()) {
            //Note: We explicitly do not bother getting a one by one access here, as we only have the single slot,
            // so either we can act on the whole stack or we can't, doing one by one won't change anything
            ResourceHandler<RESOURCE> handler = asItemAccess().getCapability(itemCapability);
            if (handler != null) {
                //Unused, but we set it anyway
                setLastTransferDirection(LastTransferDirection.DRAIN_INTO_ITEM);
                return drainContainerIntoSlot(resourceContainer, handler);
            }
        }
        return false;
    }

    /// Drains the container into the slot
    ///
    /// @param outputSlot The slot to move our container to after draining the resource container.
    protected <RESOURCE extends Resource> void drainContainerIntoSlot(IResourceContainer<RESOURCE> resourceContainer, IInventorySlot outputSlot,
          ItemCapability<ResourceHandler<RESOURCE>, @NonNull ItemAccess> itemCapability) {
        if (!isEmpty()) {
            //Verify we have an item, we have tanks that may need to be drained, and that our item is a resource handler
            // This handles making sure it has a resource handler currently, even if it may have one when it isn't stacked
            InOutSlotResourceItemAccess<RESOURCE> access = new InOutSlotResourceItemAccess<>(this, outputSlot, itemCapability, this::getLastTransferDirection, resourceContainer.resource());
            ResourceHandler<RESOURCE> handler = getHandler(access);
            if (handler != null) {
                setLastTransferDirection(LastTransferDirection.DRAIN_INTO_ITEM);
                drainContainerIntoSlot(resourceContainer, handler);
            }
        }
    }

    private <RESOURCE extends Resource> boolean drainContainerIntoSlot(IResourceContainer<RESOURCE> resourceContainer, ResourceHandler<RESOURCE> handler) {
        if (resourceContainer.isEmpty()) {
            return false;
        }
        RESOURCE resource = resourceContainer.resource();
        int availableResource;
        try (Transaction simulation = Transaction.openRoot()) {
            //Check how much we can extract from the container to ensure we follow any transfer rate limits
            availableResource = resourceContainer.extract(resource, resourceContainer.amountAsInt(), simulation, AutomationType.INTERNAL);
            if (availableResource == 0) {
                //Short circuit if nothing can actually be extracted
                return false;
            }
        }
        try (Transaction transaction = Transaction.openRoot()) {
            //Fill the stack, note our stack is a copy so this is how we simulate to get the proper "container" item,
            // and it does not actually matter that we are directly executing on the item
            int inserted = handler.insert(resource, availableResource, transaction);
            if (inserted > 0 && resourceContainer.extract(resource, inserted, transaction, AutomationType.INTERNAL) == inserted) {
                //If we were able to insert something into the original handler and extract the same amount from our container
                //Commit the changes to the transaction
                transaction.commit();
                return true;
            }
            return false;
        }
    }

    protected static <RESOURCE extends Resource> boolean canInput(IResourceContainer<RESOURCE> resourceContainer, ItemAccess itemAccess,
          ItemCapability<ResourceHandler<RESOURCE>, @NonNull ItemAccess> itemCapability) {
        ResourceHandler<RESOURCE> resourceHandler = itemAccess.getCapability(itemCapability);
        return resourceHandler != null && canInput(resourceContainer, resourceHandler);
    }

    public static <RESOURCE extends Resource> boolean canInput(ItemAccess attachedAccess, ResourceContainerType<RESOURCE, ?> containerType, int containerIndex,
          ItemResource itemType) {
        ResourceHandler<RESOURCE> resourceHandler = containerType.capability().getCapability(ItemAccessUtils.queryOnlyAccess(itemType));
        if (resourceHandler == null) {
            return false;
        }
        return canInput(containerType.createContainer(attachedAccess, containerIndex), resourceHandler);
    }

    public static <RESOURCE extends Resource> boolean canInput(IResourceContainer<RESOURCE> resourceContainer, ResourceHandler<RESOURCE> resourceHandler) {
        boolean hasEmpty = false;
        for (int tank = 0, tanks = resourceHandler.size(); tank < tanks; tank++) {
            RESOURCE resource = resourceHandler.getResource(tank);
            if (resource.isEmpty()) {
                hasEmpty = true;
            } else if (simulateCanInsert(resourceContainer, resource)) {
                //True if the items contents are valid, and we can fill the tank with any of our contents
                return true;
            }
        }
        //If we have no valid fluids/can't fill the tank with it
        if (resourceContainer.isEmpty()) {
            //we return if there is at least one empty tank in the item so that we can then drain into it
            return hasEmpty;
        }
        try (Transaction simulation = MekanismUtils.openTransactionSafe()) {
            //Note: We try to insert a bucket's amount to work around buckets not being able to be filled with a smaller amount
            // We try to insert more than a bucket though in case we have more, and it lets us get a better estimate on some custom handlers
            //TODO - 26.1: Re-evaluate this, do we want to just pass a bucket volume to it so that it potentially has to do less checking
            int toInsert = Math.max(resourceContainer.amountAsInt(), FluidType.BUCKET_VOLUME);
            return resourceHandler.insert(resourceContainer.resource(), toInsert, simulation) > 0;
        }
    }

    public static <RESOURCE extends Resource> boolean canFill(IResourceContainer<RESOURCE> resourceContainer, ItemAccess itemAccess,
          ItemCapability<ResourceHandler<RESOURCE>, @NonNull ItemAccess> itemCapability) {
        ResourceHandler<RESOURCE> resourceHandler = itemAccess.getCapability(itemCapability);
        return resourceHandler != null && canFill(resourceContainer, resourceHandler);
    }

    public static <RESOURCE extends Resource> boolean canFill(ItemAccess attachedAccess, ResourceContainerType<RESOURCE, ?> containerType, int containerIndex,
          ItemResource itemType) {
        ResourceHandler<RESOURCE> resourceHandler = containerType.capability().getCapability(ItemAccessUtils.queryOnlyAccess(itemType));
        if (resourceHandler == null) {
            return false;
        }
        return canFill(containerType.createContainer(attachedAccess, containerIndex), resourceHandler);
    }

    private static <RESOURCE extends Resource> boolean canFill(IResourceContainer<RESOURCE> resourceContainer, ResourceHandler<RESOURCE> resourceHandler) {
        for (int tank = 0, tanks = resourceHandler.size(); tank < tanks; tank++) {
            RESOURCE storedType = resourceHandler.getResource(tank);
            if (!storedType.isEmpty() && simulateCanInsert(resourceContainer, storedType)) {
                //True if we can fill the tank with any of our contents
                // Note: We need to recheck the fact the fluid is not empty and that it is valid,
                // in case the item has multiple tanks and only some of the fluids are valid
                return true;
            }
        }
        return false;
    }

    public static <RESOURCE extends Resource> boolean canDrain(IResourceContainer<RESOURCE> resourceContainer, ItemResource itemType,
          ItemCapability<ResourceHandler<RESOURCE>, @NonNull ItemAccess> itemCapability) {
        return canDrain(resourceContainer, ItemAccessUtils.queryOnlyAccess(itemType), itemCapability);
    }

    public static <RESOURCE extends Resource> boolean canDrain(IResourceContainer<RESOURCE> resourceContainer, ItemAccess itemAccess,
          ItemCapability<ResourceHandler<RESOURCE>, @NonNull ItemAccess> itemCapability) {
        ResourceHandler<RESOURCE> resourceHandler = itemAccess.getCapability(itemCapability);
        return resourceHandler != null && canDrain(resourceContainer, resourceHandler);
    }

    public static <RESOURCE extends Resource> boolean canDrain(ItemAccess attachedAccess, ResourceContainerType<RESOURCE, ?> containerType, int containerIndex,
          ItemResource itemType) {
        ResourceHandler<RESOURCE> resourceHandler = containerType.capability().getCapability(ItemAccessUtils.queryOnlyAccess(itemType));
        if (resourceHandler == null) {
            return false;
        }
        return canDrain(containerType.createContainer(attachedAccess, containerIndex), resourceHandler);
    }

    private static <RESOURCE extends Resource> boolean canDrain(IResourceContainer<RESOURCE> resourceContainer, ResourceHandler<RESOURCE> resourceHandler) {
        //True if the tanks contents are valid, and we can fill the item with any of the contents
        if (resourceContainer.isEmpty()) {
            //TODO - 26.1: Should we make this only check indices of a specific resource type???
            return !ResourceHandlerUtil.isFull(resourceHandler);
        }
        try (Transaction simulation = MekanismUtils.openTransactionSafe()) {
            //TODO - 26.1: Do we need to do similar to the canInput that checks for bucket volume?
            return resourceHandler.insert(resourceContainer.resource(), resourceContainer.amountAsInt(), simulation) > 0;
        }
    }

    protected static <RESOURCE extends Resource> boolean canRotaryInsert(IResourceContainer<RESOURCE> resourceContainer, ItemResource itemType,
          ItemCapability<ResourceHandler<RESOURCE>, @NonNull ItemAccess> itemCapability, BooleanSupplier isProcessingResource) {
        ResourceHandler<RESOURCE> resourceHandler = ItemAccessUtils.queryOnlyAccess(itemType).getCapability(itemCapability);
        return resourceHandler != null && canRotaryInsert(resourceContainer, resourceHandler, isProcessingResource.getAsBoolean());
    }

    public static <RESOURCE extends Resource> boolean canRotaryInsert(ItemAccess attachedAccess, ResourceContainerType<RESOURCE, ?> containerType, int containerIndex,
          ItemResource itemType, boolean isProcessingResource) {
        ResourceHandler<RESOURCE> resourceHandler = containerType.capability().getCapability(ItemAccessUtils.queryOnlyAccess(itemType));
        if (resourceHandler == null) {
            return false;
        }
        return canRotaryInsert(containerType.createContainer(attachedAccess, containerIndex), resourceHandler, isProcessingResource);
    }

    private static <RESOURCE extends Resource> boolean canRotaryInsert(IResourceContainer<RESOURCE> resourceContainer, ResourceHandler<RESOURCE> resourceHandler,
          boolean isProcessingResource) {
        if (isProcessingResource) {
            //If we are processing the resource, see if any of the contents from the item are valid
            return canFill(resourceContainer, resourceHandler);
        }
        //If we are not processing the resource, see if we can drain the contents into the item
        return canDrain(resourceContainer, resourceHandler);
    }

    protected static <RESOURCE extends Resource> boolean simulateCanInsert(IResourceContainer<RESOURCE> resourceContainer, RESOURCE resource) {
        return simulateCanInsert(resourceContainer, resource, AutomationType.INTERNAL);
    }

    protected static <RESOURCE extends Resource> boolean simulateCanInsert(IResourceContainer<RESOURCE> resourceContainer, RESOURCE resource, AutomationType automationType) {
        if (resourceContainer.isValidForInsertion(resource, automationType)) {
            //Calculate if the resource is ever valid for insertion into the resource container
            //If it is and our resource is currently empty or has the same type of resource
            // that means the items contents are valid, and we can fill the resource with any of our contents
            return resourceContainer.isEmpty() || resourceContainer.resource().equals(resource);
        }
        return false;
    }

    public enum LastTransferDirection implements StringRepresentable {
        UNKNOWN,
        FILL_FROM_ITEM,
        DRAIN_INTO_ITEM;

        public static final Codec<LastTransferDirection> CODEC = StringRepresentable.fromEnum(LastTransferDirection::values);

        private final String serializedName;

        LastTransferDirection() {
            this.serializedName = name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
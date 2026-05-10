package mekanism.api.container;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

//TODO - 26.1: Make it so that things like TileEntityMekanism don't directly implement IMekanismInventory and friends so that methods like getContainers are not confusing
@NothingNullByDefault//TODO - 26.1: Docs and re-evaluate the package this is in
public interface IMekanismResourceHandler<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> extends ResourceHandler<RESOURCE>, IContentsListener {

    /**
     * Returns the list of IInventorySlots that this inventory exposes on the given side.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The list of all IInventorySlots that this {@link IMekanismResourceHandler} contains for the given side. If there are no slots for the side or
     * {@link #hasInventory()} is false then it returns an empty list.
     *
     * @implNote When side is null (an internal request), this method <em>MUST</em> return all slots in the inventory. This will be used by the container generating code
     * to add all the proper slots that are needed. Additionally, if {@link #hasInventory()} is false, this <em>MUST</em> return an empty list.
     * @since 10.8.0
     */
    List<CONTAINER> getContainers();

    /**
     * Returns the {@link IResourceContainer} that has the given index from the list of slots on the given side.
     *
     * @param index The index of the container to retrieve.
     * @param side  The side we are interacting with the handler from (null for internal).
     *
     * @return The {@link IResourceContainer} that has the given index from the list of slots on the given side.
     *
     * @since 10.8.0
     */
    @Nullable
    default CONTAINER getContainer(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        //TODO - 26.1: Should we make this throw instead of return null when invalid? That means it would propagate the exception times that resource handler defines
        List<CONTAINER> containers = getContainers();
        return index >= 0 && index < containers.size() ? containers.get(index) : null;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int size() {
        return getContainers().size();
    }

    RESOURCE getEmptyResource();

    @Override
    default RESOURCE getResource(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        CONTAINER container = getContainer(index);
        return container == null ? getEmptyResource() : container.getResource();
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getAmountAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        CONTAINER container = getContainer(index);
        return container == null ? 0 : container.amount();
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    default int insert(@Range(from = 0, to = Integer.MAX_VALUE) int index, RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount,
          TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        CONTAINER container = getContainer(index);
        return container == null ? 0 : container.insert(resource, amount, transaction, automationType);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    default int insert(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        //TODO - 26.1: Add comments and document how this inserts into non empty matching containers first?
        // Also re-evaluate if that is actually the behavior we want vs making call sites use something like ResourceHandlerUtil#insertStacking
        List<CONTAINER> containers = getContainers();
        if (containers.isEmpty()) {
            return 0;
        } else if (containers.size() == 1) {
            return containers.getFirst().insert(resource, amount, transaction, automationType);
        }
        int inserted = 0;
        List<CONTAINER> emptyContainers = new ArrayList<>(containers.size());
        for (CONTAINER container : containers) {
            if (container.isEmpty()) {
                emptyContainers.add(container);
            } else {
                inserted += container.insert(resource, amount - inserted, transaction, automationType);
                if (inserted == amount) {
                    return inserted;
                }
            }
        }
        for (CONTAINER container : emptyContainers) {
            inserted += container.insert(resource, amount - inserted, transaction, automationType);
            if (inserted == amount) {
                return inserted;
            }
        }
        return inserted;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int insert(@Range(from = 0, to = Integer.MAX_VALUE) int index, RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        //TODO - 26.1: Evaluate calls to this and extract for all resource handlers and see what can be moved over to indexless interactions
        //TODO - 26.1: Should this fallback for insert and extract use internal or external as the automation type?
        return insert(index, resource, amount, transaction, AutomationType.INTERNAL);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int insert(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        return insert(resource, amount, transaction, AutomationType.INTERNAL);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    default int extract(@Range(from = 0, to = Integer.MAX_VALUE) int index, RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount,
          TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        CONTAINER container = getContainer(index);
        return container == null ? 0 : container.extract(resource, amount, transaction, automationType);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    default int extract(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int extracted = 0;
        for (CONTAINER container : getContainers()) {
            extracted += container.extract(resource, amount - extracted, transaction, automationType);
            if (extracted == amount) {
                break;
            }
        }
        return extracted;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int extract(@Range(from = 0, to = Integer.MAX_VALUE) int index, RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        return extract(index, resource, amount, transaction, AutomationType.INTERNAL);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int extract(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        return extract(resource, amount, transaction, AutomationType.INTERNAL);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getCapacityAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int index, RESOURCE resource) {
        CONTAINER container = getContainer(index);
        return container == null ? 0 : container.getLimit(resource);
    }

    @Override
    default boolean isValid(@Range(from = 0, to = Integer.MAX_VALUE) int index, RESOURCE resource) {
        CONTAINER container = getContainer(index);
        return container != null && container.isValid(resource);
    }

    /**
     * Sided inventory helper for isEmpty
     *
     * @return true if completely empty on the default side
     */
    default boolean isEmpty() {//TODO - 26.1: Update docs
        for (CONTAINER container : getContainers()) {
            if (!container.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
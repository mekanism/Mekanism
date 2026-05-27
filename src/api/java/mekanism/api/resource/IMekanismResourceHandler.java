package mekanism.api.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

/// A generic handler for the transfer and storage of [`resources`][Resource] whether it be inserting, extracting, querying some value, etc.
/// ## Indices
/// A resource handler is organized into indices, which are addressed using an int between `0` and `size() - 1`.
///
/// An index represents a "slot", "tank", "buffer", depending on the type of resource.
///
/// Out-of-bounds access using methods that accept an `index` will usually throw an exception, so only indices between 0 (included) and the size (excluded) should be
/// used. If a storage has a dynamic size, it should be lenient to accommodate for callers holding onto a previously returned size.
///
/// ## Containers
/// This interface exists as a helper to allow implementing [`resource handlers`][ResourceHandler] on a per-index basis via [`resource containers`][IResourceContainer].
///
/// @param <RESOURCE>  The type of resource this handler manages.
/// @param <CONTAINER> The type of resource containers this handler is backed by.
///
/// @since 10.8.0
@FunctionalInterface
@NothingNullByDefault
public interface IMekanismResourceHandler<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> extends ResourceHandler<RESOURCE> {

    /// {@return the list of containers that this resource handler exposes}
    List<CONTAINER> getContainers();

    /// {@return the resource container at the given index}
    ///
    /// @param index The index to get the resource container from.
    default CONTAINER getContainer(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        List<CONTAINER> containers = getContainers();
        Objects.checkIndex(index, containers.size());
        return containers.get(index);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int size() {
        return getContainers().size();
    }

    @Override
    default RESOURCE getResource(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        return getContainer(index).resource();
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getAmountAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        return getContainer(index).amountAsLong();
    }

    /// Inserts up to the given amount of a resource into the handler at the given index.
    ///
    /// Changes to the handler are made in the context of a [transaction][Transaction].
    ///
    /// @param index          The index to insert the resource into.
    /// @param resource       The resource to insert. **Must be non-empty.**
    /// @param amount         The maximum amount of the resource to insert. **Must be non-negative.**
    /// @param transaction    The transaction that this operation is part of.
    /// @param automationType The method that this handler is being interacted from.
    ///
    /// @return The amount that was inserted. Between `0` (inclusive, nothing was inserted) and `amount` (inclusive, everything was inserted).
    ///
    /// @throws IllegalArgumentException If the resource is empty or the amount is negative. See also [TransferPreconditions#checkNonEmptyNonNegative] to help perform
    /// this check.
    /// @implSpec Implementations must properly support [transactions][Transaction]. Note that [SnapshotJournal] can serve as the base class for a transaction-aware
    /// resource handler.
    /// @see #insert(Resource, int, TransactionContext, AutomationType) Inserting without a specific index, which can be more efficient.
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int insert(@Range(from = 0, to = Integer.MAX_VALUE) int index, RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount,
          TransactionContext transaction, AutomationType automationType) {
        return getContainer(index).insert(resource, amount, transaction, automationType);
    }

    /// Inserts up to the given amount of a resource into the handler.
    ///
    /// This function is preferred to the [index-specific overload][#insert(int, Resource, int, TransactionContext, AutomationType)] since it lets the handler decide how
    /// to distribute the resource.
    ///
    /// This method is expected to be more efficient than callers trying to find a suitable index for insertion themselves.
    ///
    /// Changes to the handler are made in the context of a [transaction][Transaction].
    ///
    /// @param resource       The resource to insert. **Must be non-empty.**
    /// @param amount         The maximum amount of the resource to insert. **Must be non-negative.**
    /// @param transaction    The transaction that this operation is part of.
    /// @param automationType The method that this handler is being interacted from.
    ///
    /// @return The amount that was inserted. Between `0` (inclusive, nothing was inserted) and `amount` (inclusive, everything was inserted).
    ///
    /// @throws IllegalArgumentException If the resource is empty or the amount is negative. See also [TransferPreconditions#checkNonEmptyNonNegative] to help perform
    /// this check.
    /// @implSpec Implementations must properly support [transactions][Transaction]. Note that [SnapshotJournal] can serve as the base class for a transaction-aware
    /// resource handler.
    /// @see #insert(int, Resource, int, TransactionContext, AutomationType) Inserting into a specific index of the handler.
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int insert(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        //TODO - 26.1: Add comments and document how this inserts into non empty matching containers first?
        // Also re-evaluate if that is actually the behavior we want vs making call sites use something like ResourceHandlerUtil#insertStacking
        // We used to only have this for chemical and fluid handlers, which we only had one or two tanks in general, so is that a feature that we ever made use of?
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
        return insert(index, resource, amount, transaction, defaultAutomationType());
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int insert(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        return insert(resource, amount, transaction, defaultAutomationType());
    }

    /// Extracts up to the given amount of a resource from the handler at the given index.
    ///
    /// Changes to the handler are made in the context of a [transaction][Transaction].
    ///
    /// @param index          The index to extract the resource from.
    /// @param resource       The resource to extract. **Must be non-empty.**
    /// @param amount         The maximum amount of the resource to extract. **Must be non-negative.**
    /// @param transaction    The transaction that this operation is part of.
    /// @param automationType The method that this handler is being interacted from.
    ///
    /// @return The amount that was extracted. Between `0` (inclusive, nothing was extracted) and `amount` (inclusive, everything was extracted).
    ///
    /// @throws IllegalArgumentException If the resource is empty or the amount is negative. See also [TransferPreconditions#checkNonEmptyNonNegative] to help perform
    /// this check.
    /// @implSpec Implementations must properly support [transactions][Transaction]. Note that [SnapshotJournal] can serve as the base class for a transaction-aware
    /// resource handler.
    /// @see #extract(Resource, int, TransactionContext, AutomationType) Extracting without a specific index, which can be more efficient.
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int extract(@Range(from = 0, to = Integer.MAX_VALUE) int index, RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount,
          TransactionContext transaction, AutomationType automationType) {
        return getContainer(index).extract(resource, amount, transaction, automationType);
    }

    /// Tries to extract up to the given amount of a resource from the handler.
    ///
    /// This function is preferred to the [index-specific overload][#extract(int, Resource, int, TransactionContext, AutomationType)] since it lets the handler decide how
    /// to find indices that contain the resource.
    ///
    /// This method is expected to be more efficient than callers trying to find indices that contain the resource themselves.
    ///
    /// Changes to the handler are made in the context of a [transaction][Transaction].
    ///
    /// @param resource       The resource to extract. **Must be non-empty.**
    /// @param amount         The maximum amount of the resource to extract. **Must be non-negative.**
    /// @param transaction    The transaction that this operation is part of.
    /// @param automationType The method that this handler is being interacted from.
    ///
    /// @return The amount that was extracted. Between `0` (inclusive, nothing was extracted) and `amount` (inclusive, everything was extracted).
    ///
    /// @throws IllegalArgumentException If the resource is empty or the amount is negative. See also [TransferPreconditions#checkNonEmptyNonNegative] to help perform
    /// this check.
    /// @implSpec Implementations must properly support [transactions][Transaction]. Note that [SnapshotJournal] can serve as the base class for a transaction-aware
    /// resource handler.
    /// @see #extract(int, Resource, int, TransactionContext, AutomationType) Extracting from a specific index of the handler.
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
        //TODO - 26.1: Evaluate calls to this for all our interactions with resource handlers and see what can be moved over to indexless interactions
        return extract(index, resource, amount, transaction, defaultAutomationType());
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int extract(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        return extract(resource, amount, transaction, defaultAutomationType());
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getCapacityAsLong(@Range(from = 0, to = Integer.MAX_VALUE) int index, RESOURCE resource) {
        return getContainer(index).capacityAsLong(resource);
    }

    @Override
    default boolean isValid(@Range(from = 0, to = Integer.MAX_VALUE) int index, RESOURCE resource) {
        return getContainer(index).isValid(resource);
    }

    /// Determines which automation type methods defined via [ResourceHandler] methods will use.
    private AutomationType defaultAutomationType() {
        //TODO - 26.1: Should this fallback for insert and extract use internal or external as the automation type?
        // I think it used to fall back to internal due to technically being the null side, but I think external makes more sense
        return AutomationType.EXTERNAL;
    }
}
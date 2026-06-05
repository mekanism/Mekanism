package mekanism.api.resource;

import com.google.common.primitives.Ints;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

/// A generic container for the transfer and storage of [`resources`][Resource] whether it be inserting, extracting, querying some value, etc.
///
/// @param <RESOURCE> The type of resource this container manages.
///
/// @see BasicResourceContainer A functional implementation of this interface
/// @since 10.8.0
@NothingNullByDefault
public interface IResourceContainer<RESOURCE extends Resource> extends ValueIOSerializable {

    /// {@return the resource in this container, including how much is stored, which may be empty}
    LargeResourceStack<RESOURCE> asStack();

    /// {@return the resource in this container, which may be empty}
    ///
    /// If the resource is empty, the [stored amount][#amountAsLong()] must be 0.
    default RESOURCE resource() {
        return asStack().resource();
    }

    /// Returns the amount of the [currently stored resource][#resource] in this container, as an `int`.
    ///
    /// This is a convenience method to clamp the amount to an `int`, for the cases where the container is known to only support amounts up to `Integer.MAX_VALUE`, or if
    /// the caller prefers to deal in `int`s only.
    ///
    /// The returned amount must be **non-negative**. If the [stored resource][#resource] is empty, the amount must be 0.
    ///
    /// @return the amount in this container, as an `int`
    ///
    /// @implNote This method should not be implemented. The default method will call [#amountAsLong] and convert the result appropriately.
    /// @see #amountAsLong() the long-returning overload
    @NonExtendable
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int amountAsInt() {
        return Ints.saturatedCast(amountAsLong());
    }

    /// Returns the amount of the [currently stored resource][#resource] in this container, as a `long`.
    ///
    /// In general, resource containers can report `long` amounts. However, if the container is known to only support amounts up to `Integer.MAX_VALUE`, or if the caller
    /// prefers to deal in `int`s only, the [int-returning overload][#amountAsInt] can be used instead.
    ///
    /// The returned amount must be **non-negative**. If the [stored resource][#resource] is empty, the amount must be 0.
    ///
    /// @return the amount in this container, as a `long`
    ///
    /// @see #amountAsInt()
    @Range(from = 0, to = Long.MAX_VALUE)
    default long amountAsLong() {
        return asStack().amount();
    }

    /// Inserts up to the given amount of a resource into this container.
    ///
    /// Changes to this container are made in the context of a [transaction][Transaction].
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
    /// resource container.
    @Range(from = 0, to = Integer.MAX_VALUE)
    int insert(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType);

    /// Tries to extract up to the given amount of a resource from this container.
    ///
    /// Changes to this container are made in the context of a [transaction][Transaction].
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
    /// resource container.
    @Range(from = 0, to = Integer.MAX_VALUE)
    int extract(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType);

    /// Returns the capacity of this container for the given resource, irrespective of the current amount or resource currently in this container is, as an `int`.
    ///
    /// This is a convenience method to get the capacity clamped to an `int`, for the cases where this container is known to only support capacities up to
    /// `Integer.MAX_VALUE`, or if the caller prefers to deal in `int`s only.
    ///
    /// This function serves as a hint on the maximum [amount][#amountAsInt()] the resource container might contain, for example the container can be considered full if
    /// `amount >= capacity`. Note that the returned capacity may overestimate the actual allowed amount, and it might be smaller than the current amount. The only way to
    /// know if a container will accept a resource, is to try to [`insert`][#insert] it.
    ///
    /// @param resource The resource to get the limit for. May be empty to get the general capacity of this container.
    ///
    /// @return the capacity in this container, as an `int`
    ///
    /// @implNote This method should not be implemented. The default method will call [#capacityAsLong(Resource)] and convert the result appropriately.
    /// @see #capacityAsLong(Resource)
    @NonExtendable
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int capacityAsInt(RESOURCE resource) {
        return Ints.saturatedCast(capacityAsLong(resource));
    }

    /// Returns the capacity of this container for the given resource, irrespective of the current amount or resource currently in this container is, as a `long`.
    ///
    /// In general, resource containers can report `long` capacities. However, if the container is known to only support capacities up to `Integer.MAX_VALUE`, or if the
    /// caller prefers to deal in `int`s only, the [int-returning overload][#capacityAsInt] can be used instead.
    ///
    /// This function serves as a hint on the maximum [amount][#amountAsLong()] the resource container might contain, for example the container can be considered full if
    /// `amount >= capacity`. Note that the returned capacity may overestimate the actual allowed amount, and it might be smaller than the current amount. The only way to
    /// know if a container will accept a resource, is to try to [`insert`][#insert] it.
    ///
    /// @param resource The resource to get the capacity for. May be empty to get the general capacity of this container.
    ///
    /// @return the capacity in this container, as a `long`
    ///
    /// @implSpec This method should return 0 for any resource for which [#isValid(Resource)] returns `false`.
    /// @see #capacityAsInt(Resource)
    @Range(from = 0, to = Long.MAX_VALUE)
    long capacityAsLong(RESOURCE resource);

    /// Returns the amount needed by this container for the given resource to reach a full state.
    ///
    /// This is a convenience method to get the needed amount clamped to an `int`, for the cases where this container is known to only support capacities up to
    /// `Integer.MAX_VALUE`, or if the caller prefers to deal in `int`s only.
    ///
    /// @param resource The resource to get the amount needed for. May be empty to get the amount needed by this container for the currently stored resource.
    ///
    /// @return the amount needed by this container for the given resource, as an `int`
    ///
    /// @implSpec This method should return 0 for any resource for which [#isValid(Resource)] returns `false`, as well as if the resource does not match the currently
    /// stored resource.
    /// @see #getNeededAsLong(Resource)
    @NonExtendable
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int getNeededAsInt(RESOURCE resource) {
        return Ints.saturatedCast(getNeededAsLong(resource));
    }

    /// Returns the amount needed by this container for the given resource to reach a full state.
    ///
    /// In general, resource containers can report `long` capacities. However, if the container is known to only support capacities up to `Integer.MAX_VALUE`, or if the
    /// caller prefers to deal in `int`s only, the [int-returning overload][#getNeededAsInt] can be used instead.
    ///
    /// @param resource The resource to get the amount needed for. May be empty to get the amount needed by this container for the currently stored resource.
    ///
    /// @return the amount needed by this container for the given resource, as a `long`
    ///
    /// @implSpec This method should return 0 for any resource for which [#isValid(Resource)] returns `false`, as well as if the resource does not match the currently
    /// stored resource.
    /// @see #getNeededAsInt(Resource)
    @NonExtendable
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getNeededAsLong(RESOURCE resource) {
        RESOURCE currentResource = resource();
        if (resource.isEmpty()) {
            return Math.max(0, capacityAsLong(currentResource) - amountAsLong());
        } else if (!currentResource.isEmpty() && !currentResource.equals(resource)) {
            return 0;
        }
        return Math.max(0, capacityAsLong(resource) - amountAsLong());
    }

    /// {@return whether the given resource is generally allowed to be contained in this container, irrespective of the current amount or resource currently in this
    /// container}
    ///
    /// This function serves as a hint on whether this container can contain the resource or not. The only way to know if a container will accept a resource, is to try to
    /// [`insert`][#insert] it.
    ///
    /// @param resource The resource to check. **Must be non-empty.**
    ///
    /// @throws IllegalArgumentException If the resource is empty. See also [TransferPreconditions#checkNonEmpty] to help perform this check.
    boolean isValid(RESOURCE resource);

    /// {@return whether the current stored resource is generally allowed to be extracted from this container using the given automation type}
    ///
    /// This function serves as a hint on whether the current stored resource can be extracted from this container or not. The only way to know if a container will allow
    /// the current resource to be extracted, is to try to [`extract`][#extract] it.
    ///
    /// @param automationType The automation type to check.
    default boolean isCurrentValidForExtraction(AutomationType automationType) {
        return true;
    }

    /// {@return whether the given resource is generally allowed to be inserted into this container when using the given automation type, irrespective of the current
    /// amount or resource currently in this container}
    ///
    /// This function serves as a hint on whether this container is able to accept the resource or not. The only way to know if a container will currently accept a
    /// resource, is to try to [`insert`][#insert] it.
    ///
    /// @param resource       The resource to check. **Must be non-empty.**
    /// @param automationType The automation type to check.
    ///
    /// @throws IllegalArgumentException If the resource is empty. See also [TransferPreconditions#checkNonEmpty] to help perform this check.
    default boolean isValidForInsertion(RESOURCE resource, AutomationType automationType) {
        return true;
    }

    /// Convenience method for checking if this container is empty.
    ///
    /// @return `true` if the container is empty, `false` otherwise.
    @NonExtendable
    default boolean isEmpty() {
        return amountAsLong() == 0;
    }

    /// Convenience method for checking if this container is full.
    ///
    /// @return `true` if the container is , `false` otherwise.
    @NonExtendable
    default boolean isFull() {
        return amountAsLong() >= capacityAsLong(resource());
    }

    @Override
    default void serialize(ValueOutput output) {
        stackHelper().storeNonEmpty(output, SerializationConstants.STORED, asStack());
    }

    @Override
    default void deserialize(ValueInput input) {
        //Set the stack in an unchecked way so that if it is no longer valid, we don't end up crashing due to the stack not being valid
        setContents(stackHelper().readOrEmpty(input, SerializationConstants.STORED), null);
    }

    /// Helper method to copy all pertinent data from another [`resource container`][IResourceContainer] to this one without requiring a serialization, deserialization
    /// cycle.
    ///
    /// @param other       Container to copy data from. Might be [`wrapped`][ResourceContainerWrapper].
    /// @param transaction The transaction that this operation is part of. May be `null`.
    ///
    /// @implSpec If [#serialize] is overridden, this method should be overridden as well to transfer the relevant data.
    /// @see ResourceContainerWrapper#getInternal() Getting the internal container when wrapped if instance checks are necessary.
    default void copyContents(IResourceContainer<RESOURCE> other, @Nullable TransactionContext transaction) {
        setContents(other.asStack(), transaction);
    }

    /// Sets the currently stored contents for this container.
    ///
    /// @param contents    Contents to update the container to.
    /// @param transaction The transaction that this operation is part of. May be `null`.
    void setContents(LargeResourceStack<RESOURCE> contents, @Nullable TransactionContext transaction);

    /// Sets the currently stored contents for this container.
    ///
    /// @param type         Resource type.
    /// @param storedAmount Amount of resource stored.
    /// @param transaction  The transaction that this operation is part of. May be `null`.
    @NonExtendable
    default void setContents(RESOURCE type, @Range(from = 0, to = Long.MAX_VALUE) long storedAmount, @Nullable TransactionContext transaction) {
        setContents(stackHelper().createStack(type, storedAmount), transaction);
    }

    /// {@return the stack helper for the type of resource this container stores}
    LargeResourceStack.StackHelper<RESOURCE> stackHelper();
}
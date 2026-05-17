package mekanism.api.resource;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
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

//TODO - 26.1: Docs
@NothingNullByDefault
public interface IResourceContainer<RESOURCE extends Resource> extends ValueIOSerializable, IContentsListener {

    /// {@return the resource in this container, which may be empty}
    ///
    /// If the resource is empty, the [stored amount][#amountAsLong()] must be 0.
    RESOURCE getResource();//TODO - 26.1: Is the resource guaranteed to be empty if the amount is zero? (For us yes, but for resource handlers in general, figure it out as we assume that to be the case)

    default LargeResourceStack<RESOURCE> asStack() {
        //TODO - 26.1: Re-evaluate this method
        return new LargeResourceStack<>(getResource(), amountAsLong());
    }

    /// Returns the amount of the [currently stored resource][#getResource] in this container, as an `int`.
    ///
    /// This is a convenience method to clamp the amount to an `int`, for the cases where the container is known to only support amounts up to `Integer.MAX_VALUE`, or if
    /// the caller prefers to deal in `int`s only.
    ///
    /// The returned amount must be **non-negative**. If the [stored resource][#getResource] is empty, the amount must be 0.
    ///
    /// @return the amount in this container, as an `int`
    ///
    /// @implNote This method should not be implemented. The default method will call [#amountAsLong] and convert the result appropriately.
    /// @see #amountAsLong() the long-returning overload
    @NonExtendable
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int amountAsInt() {//TODO - 26.1: Review uses and see what should be moved to amountAsLong
        return Ints.saturatedCast(amountAsLong());
    }

    /// Returns the amount of the [currently stored resource][#getResource] in this container, as a `long`.
    ///
    /// In general, resource containers can report `long` amounts. However, if the container is known to only support amounts up to `Integer.MAX_VALUE`, or if the caller
    /// prefers to deal in `int`s only, the [int-returning overload][#amountAsInt] can be used instead.
    ///
    /// The returned amount must be **non-negative**. If the [stored resource][#getResource] is empty, the amount must be 0.
    ///
    /// @return the amount in this container, as a long
    ///
    /// @see #amountAsInt()
    @Range(from = 0, to = Long.MAX_VALUE)
    long amountAsLong();

    /// Inserts up to the given amount of a resource into this container.
    ///
    /// Changes to this container are made in the context of a [transaction][Transaction].
    ///
    /// @param resource    The resource to insert. **Must be non-empty.**
    /// @param amount      The maximum amount of the resource to insert. **Must be non-negative.**
    /// @param transaction The transaction that this operation is part of.
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
    /// @param resource    The resource to extract. **Must be non-empty.**
    /// @param amount      The maximum amount of the resource to extract. **Must be non-negative.**
    /// @param transaction The transaction that this operation is part of.
    ///
    /// @return The amount that was extracted. Between `0` (inclusive, nothing was extracted) and `amount` (inclusive, everything was extracted).
    ///
    /// @throws IllegalArgumentException If the resource is empty or the amount is negative. See also [TransferPreconditions#checkNonEmptyNonNegative] to help perform
    /// this check.
    /// @implSpec Implementations must properly support [transactions][Transaction]. Note that [SnapshotJournal] can serve as the base class for a transaction-aware
    /// resource container.
    @Range(from = 0, to = Integer.MAX_VALUE)
    int extract(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType);
    //TODO - 26.1: Check callers and make sure none are relying on the fact that in the past for items extraction would be clamped at the max stack size

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
    default int capacityAsInt(RESOURCE resource) {//TODO - 26.1: Review uses and see what should be moved to capacityAsLong
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
    /// @return the capacity in this container, as a long
    ///
    /// @implSpec This method should return 0 for any resource for which [#isValid(Resource)] returns `false`.
    /// @see #capacityAsInt(Resource)
    @Range(from = 0, to = Long.MAX_VALUE)
    long capacityAsLong(RESOURCE resource);

    @NonExtendable
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int getNeededAsInt(RESOURCE resource) {
        return Ints.saturatedCast(getNeededAsLong(resource));
    }

    //TODO - 26.1: Re-evaluate callers of this method that used to use IChemicalTank#getNeeded. Do they need to know it as a long? Most probably don't
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getNeededAsLong(RESOURCE resource) {
        return Math.max(0, capacityAsLong(resource) - amountAsLong());
    }

    /// {@return whether the given resource is generally allowed to be contained in this container, irrespective of the current amount or resource currently in this
    /// container}
    ///
    /// This function serves as a hint on whether this container can contain the resource or not. The only way to know if a container will accept a resource, is to try to
    /// [`insert`][#insert] it.
    ///
    /// @param resource The resource to check. **Must be non-empty.**
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
    default boolean isValidForInsertion(RESOURCE resource, AutomationType automationType) {//TODO - 26.1: Update docs and state that the empty type can not be passed for resource
        return true;
    }

    /// Convenience method for checking if this container is empty.
    ///
    /// @return True if the container is empty, false otherwise.
    default boolean isEmpty() {//TODO - 26.1: Should we also validate that the amount isn't somehow zero?
        return getResource().isEmpty();
    }

    /**
     * Convenience method for emptying this {@link IResourceContainer}.
     */
    @NonExtendable
    default void setEmpty() {//TODO - 26.1: Re-evaluate usages and the existence of this method
        setContentsUnchecked(emptyStack());
    }

    @Override
    default void serialize(ValueOutput output) {
        //TODO - 1.21: This is a copy of BasicInventorySlot#serializeNBT. We might need to also grab the specific overrides of
        // that method as special component backed inventory slots, that then access and put that other data as a different component?
        // Also make sure to override things like TileEntityMekanism#applyInventorySlots and TileEntityMekanism#collectInventorySlots
        LargeResourceStack<RESOURCE> stored = asStack();
        if (!stored.isEmpty()) {
            //TODO - 26.1: Does using stored work fine for if something has multiple types of containers on a single stack?
            // Items used to store to the key "item", but fluids and chemicals used "stored"
            output.store(SerializationConstants.STORED, resourceStackCodec(), stored);
            //TODO - 26.1: Should we remove the key if stored is empty like we do for transmitters?
        }
    }

    @Override
    default void deserialize(ValueInput input) {
        LargeResourceStack<RESOURCE> stack = input.read(SerializationConstants.STORED, resourceStackCodec()).orElse(emptyStack());
        //Set the stack in an unchecked way so that if it is no longer valid, we don't end up
        // crashing due to the stack not being valid
        setContentsUnchecked(stack.resource(), stack.amount());
    }

    //TODO - 26.1: Docs that say to override this if serializing is being overridden
    default void copyContents(IResourceContainer<RESOURCE> container) {
        setContentsUnchecked(container.getResource(), container.amountAsLong());
    }

    //TODO - 26.1: Re-evaluate this method
    default void setContentsUnchecked(LargeResourceStack<RESOURCE> stack) {
        setContentsUnchecked(stack.resource(), stack.amount());
    }

    void setContents(RESOURCE type, @Range(from = 0, to = Long.MAX_VALUE) long storedAmount);//TODO - 26.1: Do we want a transactional form of this? Probably would be semi useful

    //TODO - 26.1: Re-evaluate this method and its callers
    void setContentsUnchecked(RESOURCE type, @Range(from = 0, to = Long.MAX_VALUE) long storedAmount);

    Codec<LargeResourceStack<RESOURCE>> resourceStackCodec();

    //TODO - 26.1: Re-evaluate this method vs having inheritors implement deserialize and setEmpty
    LargeResourceStack<RESOURCE> emptyStack();
}
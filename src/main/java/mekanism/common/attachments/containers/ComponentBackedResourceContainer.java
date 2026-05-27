package mekanism.common.attachments.containers;

import com.google.common.primitives.Ints;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.attachments.containers.type.ResourceContainerType;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

/// @implNote This container does not take the backing item access into account. None of the methods for interacting with this resource container scale the inputs based
/// on the backing item access' size.
@NothingNullByDefault
public abstract class ComponentBackedResourceContainer<RESOURCE extends Resource> extends ComponentBackedContainer<LargeResourceStack<RESOURCE>, AttachedResources<RESOURCE>>
      implements IResourceContainer<RESOURCE> {

    private final BiPredicate<RESOURCE, AutomationType> canExtract;
    private final BiPredicate<RESOURCE, AutomationType> canInsert;
    private final Predicate<RESOURCE> validator;
    private final LongSupplier capacity;
    private final IntSupplier rate;

    public ComponentBackedResourceContainer(ItemAccess attachedAccess, int slotIndex, BiPredicate<RESOURCE, AutomationType> canExtract,
          BiPredicate<RESOURCE, AutomationType> canInsert, Predicate<RESOURCE> validator, IntSupplier rate, LongSupplier capacity) {
        super(attachedAccess, slotIndex);
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        this.rate = rate;
        this.capacity = capacity;
        //TODO - 1.21: Serialization for this is copy of BasicInventorySlot#serializeNBT. We might need to also grab the specific overrides of
        // that method as special component backed inventory slots, that then access and put that other data as a different component?
        // Also make sure to override things like TileEntityMekanism#applyInventorySlots and TileEntityMekanism#collectInventorySlots
    }

    @Override
    protected abstract ResourceContainerType<RESOURCE, ? extends IResourceContainer<RESOURCE>> containerType();

    @Override
    protected boolean isEmpty(LargeResourceStack<RESOURCE> stack) {
        return stack.isEmpty();
    }

    @Override
    public LargeResourceStack<RESOURCE> asStack() {
        //Note: We intentionally do not scale this based on the backing item access
        return getContents(getAttached());
    }

    @Override
    public void setContents(LargeResourceStack<RESOURCE> contents, @Nullable TransactionContext transaction) {
        //Note: We intentionally do not scale this based on the backing item access
        setContents(getAttached(), contents, transaction, true);
    }

    protected boolean setContents(AttachedResources<RESOURCE> attached, RESOURCE type, @Range(from = 0, to = Long.MAX_VALUE) long storedAmount, TransactionContext transaction) {
        //Note: We don't havea to check for changes as we know we only call this if things actually would be inserted/extracted which means that the contents changed
        return setContents(attached, stackHelper().createStack(type, storedAmount), transaction, false);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long capacityAsLong(RESOURCE resource) {
        if (!resource.isEmpty() && !isValid(resource)) {
            //If the resource is not valid we need to return zero as the capacity
            return 0;
        }
        return capacity.getAsLong();
    }

    @Override
    public boolean isValid(RESOURCE resource) {
        TransferPreconditions.checkNonEmpty(resource);
        return validator.test(resource);
    }

    @Override
    public final boolean isCurrentValidForExtraction(AutomationType automationType) {
        return isValidForExtraction(resource(), automationType);
    }

    private boolean isValidForExtraction(RESOURCE type, AutomationType automationType) {
        return !type.isEmpty() && canExtract.test(type, automationType);
    }

    @Override
    public final boolean isValidForInsertion(RESOURCE type, AutomationType automationType) {
        return isValid(type) && canInsert.test(type, automationType);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int getInsertionRate(AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType.isManual() ? Integer.MAX_VALUE : rate.getAsInt();
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int getExtractionRate(AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType.isManual() ? Integer.MAX_VALUE : rate.getAsInt();
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public final int insert(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            //"Fail quick" if nothing is being inserted
            return 0;
        }
        long capacity = capacityAsLong(resource);
        if (capacity == 0) {//The resource isn't valid
            return 0;
        }
        AttachedResources<RESOURCE> attached = getAttached();
        LargeResourceStack<RESOURCE> current = getContents(attached);
        return insert(attached, current.resource(), current.amount(), capacity, resource, amount, transaction, automationType);
    }

    /// Inserts up to the given amount of a resource into this container. This method ignores the backing item access, rather than scaling the amounts based on the size
    /// of the backing access.
    ///
    /// Changes to this container are made in the context of a [transaction][Transaction].
    ///
    /// @param currentType    The currently stored resource type.
    /// @param currentAmount  The amount currently stored (ignoring item access scaling).
    /// @param capacity       The capacity of the resource for this container (ignoring item access scaling).
    /// @param resource       The resource to insert. **Must be non-empty.**
    /// @param amount         The maximum amount of the resource to insert (ignoring item access scaling). **Must be non-negative.**
    /// @param transaction    The transaction that this operation is part of.
    /// @param automationType The method that this handler is being interacted from.
    ///
    /// @return The amount that was inserted. Between `0` (inclusive, nothing was inserted) and `amount` (inclusive, everything was inserted).
    ///
    /// @implSpec Implementations must properly support [transactions][Transaction]. Note that [SnapshotJournal] can serve as the base class for a transaction-aware
    /// resource container.
    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int insert(AttachedResources<RESOURCE> attached, RESOURCE currentType, @Range(from = 0, to = Long.MAX_VALUE) long currentAmount, long capacity,
          RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        if (amount == 0) {
            //"Fail quick" if nothing is being inserted
            return 0;
        }
        //Validate that we aren't at max stack size before we try to see if we can insert the resource, as on average this will be a cheaper check
        int needed = Ints.saturatedCast(capacity - currentAmount);
        //Limit how much we can add at once to the insertion rate the container sets
        needed = Math.min(needed, getInsertionRate(automationType));
        if (needed <= 0 || !canInsert.test(resource, automationType)) {
            //Fail if we are a full slot, or we can never insert the resource or currently are unable to insert it
            //Note: We check directly against canInsert, as the capacity returns zero if isValid is false
            return 0;
        } else if (!currentType.isEmpty() && !currentType.equals(resource)) {
            return 0;
        }
        int toAdd = Math.min(amount, needed);
        if (setContents(attached, resource, currentAmount + toAdd, transaction)) {
            return toAdd;
        }
        //If we couldn't update the backing item access, return that we didn't actually insert anything
        return 0;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public final int extract(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            //"Fail quick" if nothing is being extracted
            return 0;
        }
        AttachedResources<RESOURCE> attached = getAttached();
        LargeResourceStack<RESOURCE> current = getContents(attached);
        return extract(attached, current.resource(), current.amount(), resource, amount, transaction, automationType);
    }

    /// Tries to extract up to the given amount of a resource from this container. This method ignores the backing item access, rather than scaling the amounts based on
    /// the size of the backing access.
    ///
    /// Changes to this container are made in the context of a [transaction][Transaction].
    ///
    /// @param currentType    The currently stored resource type.
    /// @param currentAmount  The amount currently stored (ignoring item access scaling).
    /// @param resource       The resource to extract. **Must be non-empty.**
    /// @param amount         The maximum amount of the resource to extract (ignoring item access scaling). **Must be non-negative.**
    /// @param transaction    The transaction that this operation is part of.
    /// @param automationType The method that this handler is being interacted from.
    ///
    /// @return The amount that was extracted. Between `0` (inclusive, nothing was extracted) and `amount` (inclusive, everything was extracted).
    ///
    /// @implSpec Implementations must properly support [transactions][Transaction]. Note that [SnapshotJournal] can serve as the base class for a transaction-aware
    /// resource container.
    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int extract(AttachedResources<RESOURCE> attached, RESOURCE currentType, @Range(from = 0, to = Long.MAX_VALUE) long currentAmount, RESOURCE resource,
          @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        if (amount == 0 || !resource.equals(currentType) || !isValidForExtraction(currentType, automationType)) {
            //"Fail quick" if we are empty, a different type is trying to be extracted, or if we can never extract from this slot
            return 0;
        }
        //If we are trying to extract more than we have, just change it so that we are extracting it all
        int toRemove = Math.min(amount, Ints.saturatedCast(currentAmount));
        //Limit how much we can remove at once to the extraction rate the container sets
        toRemove = Math.min(toRemove, getExtractionRate(automationType));
        //Shrink the stack by the amount removed
        if (toRemove > 0 && setContents(attached, currentType, currentAmount - toRemove, transaction)) {
            return toRemove;
        }
        //If we couldn't update the backing item access, return that we didn't actually extract anything
        return 0;
    }
}
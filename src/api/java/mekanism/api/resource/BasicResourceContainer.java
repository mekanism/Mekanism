package mekanism.api.resource;

import com.google.common.primitives.Ints;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.transaction.ITransactionHelper;
import mekanism.api.transaction.RateLimitTracker;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

/// A basic implementation of a generic container for the transfer and storage of [`resources`][Resource] whether it be inserting, extracting, querying some value, etc.
///
/// @param <RESOURCE> The type of resource this container manages.
///
/// @since 10.8.0
public abstract class BasicResourceContainer<RESOURCE extends Resource> extends SnapshotJournal<LargeResourceStack<RESOURCE>> implements IResourceContainer<RESOURCE> {

    private final BiPredicate<RESOURCE, AutomationType> canExtract;
    private final BiPredicate<RESOURCE, AutomationType> canInsert;
    private final Predicate<RESOURCE> validator;
    //TODO: Figure out how to make InventoryContainerSlot properly support this and extractionRateLimiter for slot interactions
    private final RateLimitTracker insertionRateLimiter;
    private final RateLimitTracker extractionRateLimiter;
    @Nullable
    private final IContentsListener listener;
    @Range(from = 0, to = Long.MAX_VALUE)
    private final long capacity;

    private LargeResourceStack<RESOURCE> current;

    /// @param capacity              Tank capacity.
    /// @param canExtract            Extract predicate.
    /// @param canInsert             Insert predicate.
    /// @param validator             Validation predicate.
    /// @param insertionRateLimiter  Insertion rate limit handler, or `null` to not limit the insertion rate.
    /// @param extractionRateLimiter Extraction rate limit handler, or `null` to not limit the insertion rate.
    /// @param listener              Contents change listener.
    protected BasicResourceContainer(@Range(from = 0, to = Long.MAX_VALUE) long capacity, BiPredicate<RESOURCE, AutomationType> canExtract,
          BiPredicate<RESOURCE, AutomationType> canInsert, Predicate<RESOURCE> validator, @Nullable RateLimitTracker insertionRateLimiter,
          @Nullable RateLimitTracker extractionRateLimiter, @Nullable IContentsListener listener) {
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        this.listener = listener;
        this.capacity = capacity;
        this.insertionRateLimiter = ITransactionHelper.INSTANCE.orInfinite(insertionRateLimiter);
        this.extractionRateLimiter = ITransactionHelper.INSTANCE.orInfinite(extractionRateLimiter);
        this.current = stackHelper().empty();
    }

    @Override
    public final LargeResourceStack<RESOURCE> asStack() {
        return current;
    }

    @Override
    protected final LargeResourceStack<RESOURCE> createSnapshot() {
        return current;
    }

    @Override
    protected final void revertToSnapshot(LargeResourceStack<RESOURCE> snapshot) {
        //Directly set it without triggering onContentsChanged
        current = snapshot;
    }

    @Override
    protected final void onRootCommit(LargeResourceStack<RESOURCE> originalState) {
        super.onRootCommit(originalState);
        if (!originalState.equals(current)) {
            //Fire content change listeners during root commit if the final state is different from the original one
            onContentsChanged(originalState);
        }
    }

    /// Called when the contents this listener is monitoring gets changed. If the contents were changed as part of a transaction, this will get called during
    /// [#onRootCommit(LargeResourceStack)].
    ///
    /// @param originalState Original contents from before the changes occurred.
    protected void onContentsChanged(LargeResourceStack<RESOURCE> originalState) {
        if (listener != null) {
            listener.onContentsChanged();
        }
    }

    @Override
    public boolean isValid(RESOURCE type) {
        TransferPreconditions.checkNonEmpty(type);
        return validator.test(type);
    }

    @Override
    public final boolean isCurrentValidForExtraction(AutomationType automationType) {
        RESOURCE currentType = resource();
        return !currentType.isEmpty() && canExtract.test(currentType, automationType);
    }

    @Override
    public final boolean isValidForInsertion(RESOURCE type, AutomationType automationType) {
        return isValid(type) && canInsert.test(type, automationType);
    }

    @Override
    public final void setContents(LargeResourceStack<RESOURCE> contents, @Nullable TransactionContext transaction) {
        //Skip updating the contents if the target is the same as what is already stored. This prevents onContentsChanged from firing
        if (!contents.equals(current)) {
            if (transaction == null) {
                LargeResourceStack<RESOURCE> originalState = current;
                this.current = contents;
                //TODO - 26.2: do we need a way to avoid calling onContentsChange when loading from disk? I don't think we used to have one
                // but it might be useful to have, especially due to the checks happening in PersonalStorageItemInventory#onContentsChanged
                onContentsChanged(originalState);
            } else {
                updateSnapshots(transaction);
                this.current = contents;
            }
        }
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long capacityAsLong(RESOURCE resource) {
        //Ensure the resource is valid, and otherwise return zero
        if (resource.isEmpty() || isValid(resource)) {
            return capacity;
        }
        return 0;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            //"Fail quick" if the given stack is empty
            return 0;
        } else if (!isEmpty() && !this.current.matches(resource)) {
            //Fail if the type being inserted doesn't match our current stored type
            return 0;
        }
        long currentStored = amountAsLong();
        //Validate that we aren't at max stack size before we try to see if we can insert the resource, as on average this will be a cheaper check
        int needed = Ints.saturatedCast(capacityAsLong(resource) - currentStored);
        int insertionRate = insertionRateLimiter.getRemainingLimit(automationType);
        //Limit how much we can add at once to the insertion rate the container sets
        needed = Math.min(needed, insertionRate);
        if (needed <= 0 || !canInsert.test(resource, automationType)) {
            //Fail if we are a full slot, or we can never insert the resource or currently are unable to insert it
            //Note: We check directly against canInsert, as the capacity returns zero if isValid is false
            return 0;
        }
        int toAdd = Math.min(amount, needed);
        //Note: We know toAdd is greater than zero, so we can just always call setContents
        setContents(resource, currentStored + toAdd, transaction);
        insertionRateLimiter.consumeLimit(toAdd, automationType, transaction);
        return toAdd;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0 || !this.current.matches(resource) || !isCurrentValidForExtraction(automationType)) {
            //"Fail quick" if we are empty, nothing is being extracted, a different type is trying to be extracted, or if we can never extract from this slot
            return 0;
        }
        long currentStored = amountAsLong();
        //If we are trying to extract more than we have, just change it so that we are extracting it all
        int toRemove = Math.min(amount, Ints.saturatedCast(currentStored));
        int extractionRate = extractionRateLimiter.getRemainingLimit(automationType);
        //Limit how much we can remove at once to the extraction rate the container sets
        toRemove = Math.min(toRemove, extractionRate);
        if (toRemove > 0) {
            setContents(resource, currentStored - toRemove, transaction);
            extractionRateLimiter.consumeLimit(toRemove, automationType, transaction);
        }
        return toRemove;
    }
}
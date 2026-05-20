package mekanism.api.resource;

import com.google.common.primitives.Ints;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismPreconditions;
import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

//TODO - 26.1: Docs, and maybe make it a little more protected like BasicChemicalTank?
@NothingNullByDefault
public abstract class BasicResourceContainer<RESOURCE extends Resource> extends SnapshotJournal<LargeResourceStack<RESOURCE>> implements IResourceContainer<RESOURCE> {

    private final BiPredicate<RESOURCE, AutomationType> canExtract;
    private final BiPredicate<RESOURCE, AutomationType> canInsert;
    private final Predicate<RESOURCE> validator;
    @Nullable
    private final IContentsListener listener;
    @Range(from = 0, to = Long.MAX_VALUE)
    private final long limit;

    private LargeResourceStack<RESOURCE> current;

    protected BasicResourceContainer(@Range(from = 0, to = Long.MAX_VALUE) long limit, BiPredicate<RESOURCE, AutomationType> canExtract,
          BiPredicate<RESOURCE, AutomationType> canInsert, Predicate<RESOURCE> validator, @Nullable IContentsListener listener) {
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        this.listener = listener;
        this.limit = limit;
        this.current = stackHelper().empty();
    }

    @Override
    public LargeResourceStack<RESOURCE> asStack() {
        return current;
    }

    @Override
    protected LargeResourceStack<RESOURCE> createSnapshot() {
        return asStack();
    }

    @Override
    protected void revertToSnapshot(LargeResourceStack<RESOURCE> snapshot) {
        setContents(snapshot.resource(), snapshot.amount());
    }

    @Override
    protected void onRootCommit(LargeResourceStack<RESOURCE> originalState) {
        super.onRootCommit(originalState);
        if (amountAsLong() != originalState.amount() || !originalState.resource().equals(resource())) {
            //Fire content change listeners during root commit if the final state is different from the original one
            onContentsChanged(originalState);
        }
    }

    protected void onContentsChanged(LargeResourceStack<RESOURCE> originalState) {
        //TODO - 26.1: Replace the parameterless type with this?
        onContentsChanged();
    }

    @Override
    public void onContentsChanged() {
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
    public void setContents(RESOURCE type, @Range(from = 0, to = Long.MAX_VALUE) long storedAmount) {
        MekanismPreconditions.checkNonNegative(storedAmount);
        LargeResourceStack<RESOURCE> stack = stackHelper().createStack(type, storedAmount);
        if (stack.equals(current)) {
            //Skip updating the contents if the target is the same as what is already stored
            // This prevents onContentsChanged from firing
            return;
        }
        LargeResourceStack<RESOURCE> originalState = current;
        this.current = stack;
        //TODO - 26.1: Delay this until the transactions are committed when setting from a transactional context (some things like setting from slots isn't transactional)
        onContentsChanged(originalState);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long capacityAsLong(RESOURCE resource) {
        return limit;
    }

    /**
     * Helper method to allow easily setting a rate at which resources can be inserted into this {@link BasicResourceContainer}.
     *
     * @param automationType The automation type to limit the rate by or null if we don't have access to an automation type.
     *
     * @return The rate this tank can insert/extract at.
     *
     * @implNote By default, this returns {@link Integer#MAX_VALUE} to not actually limit the tank's rate. By default, this is also ignored for direct setting of the
     * stack.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int getInsertionRate(@Nullable AutomationType automationType) {
        //TODO - 26.1: Make sure that inventory slots properly support this and getExtractionRate
        // Main spot where they might not is for containers
        //TODO - 26.1: Re-evaluate insertion and extraction rate, do we need to make them be tick based in case insertions/extractions are spread across multiple calls
        // but all within the same transaction? Maybe we should have a snapshot that keeps track of how much of the limit is remaining for the given transactional state?
        // Whatever we decide also mirror it for energy containers
        return Integer.MAX_VALUE;
    }

    /**
     * Helper method to allow easily setting a rate at which resources can be extracted from this {@link BasicResourceContainer}.
     *
     * @param automationType The automation type to limit the rate by or null if we don't have access to an automation type.
     *
     * @return The rate this tank can insert/extract at.
     *
     * @implNote By default, this returns {@link Integer#MAX_VALUE} to not actually limit the tank's rate. By default, this is also ignored for direct setting of the
     * stack.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int getExtractionRate(@Nullable AutomationType automationType) {
        return Integer.MAX_VALUE;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            //"Fail quick" if the given stack is empty
            return 0;
        }
        long currentStored = amountAsLong();
        //Validate that we aren't at max stack size before we try to see if we can insert the resource, as on average this will be a cheaper check
        long needed = capacityAsLong(resource) - currentStored;
        //Limit how much we can add at once to the insertion rate the container sets
        needed = Math.min(needed, getInsertionRate(automationType));
        if (needed <= 0 || !isValidForInsertion(resource, automationType)) {
            //Fail if we are a full slot, or we can never insert the resource or currently are unable to insert it
            return 0;
        } else if (!isEmpty() && !this.current.matches(resource)) {
            //Fail if the type being inserted doesn't match our current stored type
            //TODO - 26.1: Re-evaluate if this should be above the isValidForInsertion check
            return 0;
        }
        int toAdd = Math.min(amount, Ints.saturatedCast(needed));
        updateSnapshots(transaction);
        // Note: We just set it as unchecked as we have already validated it
        setContents(resource, currentStored + toAdd);
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
        //Limit how much we can remove at once to the extraction rate the container sets
        toRemove = Math.min(toRemove, getExtractionRate(automationType));
        if (toRemove > 0) {
            updateSnapshots(transaction);
            //Shrink the stack by the amount removed
            setContents(resource, currentStored - toRemove);
        }
        return toRemove;
    }
}
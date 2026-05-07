package mekanism.common.capabilities.resource;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.IResourceContainer;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class BasicResourceContainer<RESOURCE extends Resource> extends SnapshotJournal<ResourceStack<RESOURCE>> implements IResourceContainer<RESOURCE> {

    private final BiPredicate<RESOURCE, AutomationType> canExtract;
    private final BiPredicate<RESOURCE, AutomationType> canInsert;
    private final Predicate<RESOURCE> validator;
    @Nullable
    private final IContentsListener listener;
    private final RESOURCE emptyResource;
    private final int limit;

    private RESOURCE currentType;
    private int storedAmount = 0;

    protected BasicResourceContainer(RESOURCE emptyResource, int limit, BiPredicate<RESOURCE, AutomationType> canExtract, BiPredicate<RESOURCE, AutomationType> canInsert,
          Predicate<RESOURCE> validator, @Nullable IContentsListener listener) {
        this.emptyResource = emptyResource;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        this.listener = listener;
        this.limit = limit;
        this.currentType = this.emptyResource;
    }

    @Override
    public RESOURCE getResource() {
        return this.currentType;
    }

    @Override
    public int amount() {
        return storedAmount;
    }

    @Override
    protected ResourceStack<RESOURCE> createSnapshot() {
        return new ResourceStack<>(getResource(), amount());
    }

    @Override
    protected void revertToSnapshot(ResourceStack<RESOURCE> snapshot) {
        setContentsUnchecked(snapshot.resource(), snapshot.amount());
    }

    @Override
    protected void onRootCommit(ResourceStack<RESOURCE> originalState) {
        if (amount() != originalState.amount() || !originalState.resource().equals(getResource())) {
            //Fire content change listeners during root commit if the final state is different from the original one
            onContentsChanged();
        }
    }

    @Override
    public void onContentsChanged() {
        if (listener != null) {
            listener.onContentsChanged();
        }
    }

    @Override
    public boolean isValid(RESOURCE type) {
        return validator.test(type);
    }

    @Override
    public boolean isCurrentValidForExtraction(AutomationType automationType) {
        return canExtract.test(getResource(), automationType);
    }

    @Override
    public boolean isValidForInsertion(RESOURCE type, AutomationType automationType) {
        return isValid(type) && canInsert.test(type, automationType);
    }

    @Override
    public void setContents(RESOURCE type, int storedAmount) {
        setContents(type, storedAmount, true);
    }

    public void setContentsUnchecked(RESOURCE type, int storedAmount) {
        setContents(type, storedAmount, false);
    }

    private void setContents(RESOURCE type, int storedAmount, boolean validateType) {
        TransferPreconditions.checkNonNegative(storedAmount);
        if (type.isEmpty() || storedAmount == 0) {//TODO - 26.1: Make sure that storedAmount can never have a negative passed,
            if (isEmpty()) {
                //If we are already empty just exit, to not fire onContentsChanged
                return;
            }
            this.currentType = this.emptyResource;
            this.storedAmount = 0;
        } else if (!validateType || isValid(type)) {
            this.currentType = type;
            this.storedAmount = storedAmount;
        } else {
            //Throws a RuntimeException as IItemHandlerModifiable specifies is allowed when something unexpected happens
            // As setStack is more meant to be used as an internal method
            //TODO - 26.1: Evaluate if we still want to be throwing an exception
            throw new RuntimeException("Invalid type for container: " + type);
        }
        //TODO - 26.1: Delay this until the transactions are committed when setting from a transactional context (some things like setting from slots isn't transactional)
        onContentsChanged();
    }

    @Override
    public int getLimit(RESOURCE resource) {
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
    protected int getInsertionRate(@Nullable AutomationType automationType) {
        //TODO - 26.1: Make sure that inventory slots properly support this and getExtractionRate
        // Main spot where they might not is for containers
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
    protected int getExtractionRate(@Nullable AutomationType automationType) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int insert(RESOURCE resource, int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            //"Fail quick" if the given stack is empty
            return 0;
        }
        int currentStored = amount();
        //Validate that we aren't at max stack size before we try to see if we can insert the resource, as on average this will be a cheaper check
        int needed = getLimit(resource) - currentStored;
        if (needed <= 0 || !isValidForInsertion(resource, automationType)) {
            //Fail if we are a full slot, or we can never insert the resource or currently are unable to insert it
            return 0;
        } else if (!isEmpty() && !this.currentType.equals(resource)) {
            //Fail if the type being inserted doesn't match our current stored type
            //TODO - 26.1: Re-evaluate if this should be above the isValidForInsertion check
            return 0;
        }
        int toAdd = Math.min(amount, needed);
        //Limit how much we can add at once to the insertion rate the container sets
        toAdd = Math.min(toAdd, getInsertionRate(automationType));
        if (toAdd > 0) {//TODO - 26.1: Should we allow the insertion rate to be zero?
            updateSnapshots(transaction);
            // Note: We just set it as unchecked as we have already validated it
            setContentsUnchecked(resource, currentStored + toAdd);
        }
        return toAdd;
    }

    @Override
    public int extract(RESOURCE resource, int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (isEmpty() || amount == 0 || !this.currentType.equals(resource) || !isCurrentValidForExtraction(automationType)) {
            //"Fail quick" if we are empty, nothing is being extracted, a different type is trying to be extracted, or if we can never extract from this slot
            return 0;
        }
        int currentStored = amount();
        //If we are trying to extract more than we have, just change it so that we are extracting it all
        int toRemove = Math.min(amount, currentStored);
        //Limit how much we can remove at once to the extraction rate the container sets
        toRemove = Math.min(toRemove, getExtractionRate(automationType));
        if (toRemove > 0) {//TODO - 26.1: Should we allow the insertion rate to be zero?
            updateSnapshots(transaction);
            //Shrink the stack by the amount removed
            setContentsUnchecked(resource, currentStored - toRemove);
        }
        return toRemove;
    }
}
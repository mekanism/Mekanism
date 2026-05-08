package mekanism.common.attachments.containers;

import com.google.common.primitives.Ints;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.IResourceContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault//TODO - 26.1: Do we want to change TYPE to being ResourceStack<RESOURCE>? It would probably make the logic a little cleaner
public abstract class ComponentBackedResourceContainer<RESOURCE extends Resource, TYPE, ATTACHED extends IAttachedContainers<TYPE, ATTACHED>>
      extends ComponentBackedContainer<TYPE, ATTACHED> implements IResourceContainer<RESOURCE> {

    private final BiPredicate<RESOURCE, AutomationType> canExtract;
    private final BiPredicate<RESOURCE, AutomationType> canInsert;
    private final Predicate<RESOURCE> validator;
    private final long limit;

    public ComponentBackedResourceContainer(ItemStack attachedTo, int slotIndex, long limit, BiPredicate<RESOURCE, AutomationType> canExtract,
          BiPredicate<RESOURCE, AutomationType> canInsert, Predicate<RESOURCE> validator) {
        super(attachedTo, slotIndex);
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        this.limit = limit;
    }

    protected abstract RESOURCE asResource(TYPE stack);

    protected abstract long getAmountAsLong(TYPE stack);

    @Override
    public RESOURCE getResource() {
        return asResource(getContents(getAttached()));
    }

    @Override
    public long amountAsLong() {
        return getAmountAsLong(getContents(getAttached()));
    }

    @Override
    public final void setContents(RESOURCE type, long storedAmount) {
        //TODO - 26.1: Re-evaluate this
        setContents(getAttached(), type, storedAmount);
    }

    @Override
    public void setContentsUnchecked(RESOURCE type, long storedAmount) {
        setContents(getAttached(), type, storedAmount);
    }

    protected abstract void setContents(ATTACHED attached, RESOURCE type, long storedAmount);

    @Override
    public long getLimitAsLong(RESOURCE resource) {
        return limit;
    }

    @Override
    public boolean isValid(RESOURCE resource) {
        return validator.test(resource);
    }

    @Override
    public final boolean isCurrentValidForExtraction(AutomationType automationType) {
        return isValidForExtraction(getResource(), automationType);
    }

    private boolean isValidForExtraction(RESOURCE type, AutomationType automationType) {
        return canExtract.test(type, automationType);
    }

    @Override
    public boolean isValidForInsertion(RESOURCE type, AutomationType automationType) {
        return isValid(type) && canInsert.test(type, automationType);
    }

    protected int getInsertionRate(@Nullable AutomationType automationType) {
        //TODO - 26.1: Make sure that inventory slots properly support this and getExtractionRate
        return Integer.MAX_VALUE;
    }

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
        ATTACHED attached = getAttached();
        TYPE current = getContents(attached);
        return insert(attached, asResource(current), getAmountAsLong(current), resource, amount, transaction, automationType);
    }

    protected int insert(ATTACHED attached, RESOURCE currentType, long currentAmount, RESOURCE resource, int amount, TransactionContext transaction, AutomationType automationType) {
        if (amount == 0) {
            //"Fail quick" if the given resource is empty
            return 0;
        }
        //Validate that we aren't at max stack size before we try to see if we can insert the resource, as on average this will be a cheaper check
        long needed = getLimitAsLong(resource) - currentAmount;
        if (needed <= 0 || !isValidForInsertion(resource, automationType)) {
            //Fail if we are a full slot, or we can never insert the resource or currently are unable to insert it
            return 0;
        } else if (!currentType.isEmpty() && !currentType.equals(resource)) {
            return 0;
        }
        int toAdd = Math.min(amount, Ints.saturatedCast(needed));
        //Limit how much we can add at once to the insertion rate the container sets
        toAdd = Math.min(toAdd, getInsertionRate(automationType));
        if (toAdd > 0) {//TODO - 26.1: Should we allow the insertion rate to be zero?
            updateSnapshots(transaction);
            //Note: We let setStack handle updating the backing holding stack
            // We use current.getCount + toAdd so that if we are empty we end up at toAdd
            // but if we aren't then we grow by the given amount
            setContents(attached, resource, currentAmount + toAdd);
        }
        return toAdd;
    }

    @Override
    public int extract(RESOURCE resource, int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            //"Fail quick" if nothing is actually being extracted
            return 0;
        }
        ATTACHED attached = getAttached();
        TYPE current = getContents(attached);
        RESOURCE currentType = asResource(current);
        long currentStored = getAmountAsLong(current);
        if (currentType.isEmpty() || !resource.equals(currentType) || !isValidForExtraction(currentType, automationType)) {
            //"Fail quick" if we are empty, a different type is trying to be extracted, or if we can never extract from this slot
            return 0;
        }
        //If we are trying to extract more than we have, just change it so that we are extracting it all
        int toRemove = Math.min(amount, Ints.saturatedCast(currentStored));
        //Limit how much we can remove at once to the extraction rate the container sets
        toRemove = Math.min(toRemove, getExtractionRate(automationType));
        if (toRemove > 0) {//TODO - 26.1: Should we allow the insertion rate to be zero?
            updateSnapshots(transaction);
            //Shrink the stack by the amount removed
            setContents(attached, currentType, currentStored - toRemove);
        }
        return toRemove;
    }
}
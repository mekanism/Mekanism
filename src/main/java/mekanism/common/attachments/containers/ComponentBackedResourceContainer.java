package mekanism.common.attachments.containers;

import com.google.common.primitives.Ints;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.MekanismPreconditions;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault//TODO - 26.1: Do we want to change TYPE to being ResourceStack<RESOURCE>? It would probably make the logic a little cleaner
public abstract class ComponentBackedResourceContainer<RESOURCE extends Resource> extends ComponentBackedContainer<LargeResourceStack<RESOURCE>, AttachedResources<RESOURCE>> implements IResourceContainer<RESOURCE> {

    private final BiPredicate<RESOURCE, AutomationType> canExtract;
    private final BiPredicate<RESOURCE, AutomationType> canInsert;
    private final Predicate<RESOURCE> validator;
    private final LongSupplier capacity;
    private final IntSupplier rate;

    public ComponentBackedResourceContainer(ItemStack attachedTo, int slotIndex, BiPredicate<RESOURCE, AutomationType> canExtract,
          BiPredicate<RESOURCE, AutomationType> canInsert, Predicate<RESOURCE> validator, IntSupplier rate, LongSupplier capacity) {
        super(attachedTo, slotIndex);
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
    protected boolean isEmpty(LargeResourceStack<RESOURCE> stack) {
        return stack.isEmpty();
    }

    @Override
    public LargeResourceStack<RESOURCE> asStack() {
        return getContents(getAttached());
    }

    @Override
    public RESOURCE getResource() {
        return asStack().resource();
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long amountAsLong() {
        return asStack().amount();
    }

    @Override
    public final void setContents(RESOURCE type, @Range(from = 0, to = Long.MAX_VALUE) long storedAmount) {
        //TODO - 26.1: Re-evaluate this
        setContents(getAttached(), type, storedAmount);
    }

    @Override
    public void setContentsUnchecked(RESOURCE type, @Range(from = 0, to = Long.MAX_VALUE) long storedAmount) {
        setContents(getAttached(), type, storedAmount);
    }

    protected void setContents(AttachedResources<RESOURCE> attached, RESOURCE type, @Range(from = 0, to = Long.MAX_VALUE) long storedAmount) {
        MekanismPreconditions.checkNonNegative(storedAmount);
        setContents(attached, stackHelper().createStack(type, storedAmount));
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long capacityAsLong(RESOURCE resource) {
        return capacity.getAsLong();
    }

    @Override
    public boolean isValid(RESOURCE resource) {
        TransferPreconditions.checkNonEmpty(resource);
        return validator.test(resource);
    }

    @Override
    public final boolean isCurrentValidForExtraction(AutomationType automationType) {
        return isValidForExtraction(getResource(), automationType);
    }

    private boolean isValidForExtraction(RESOURCE type, AutomationType automationType) {
        return !type.isEmpty() && canExtract.test(type, automationType);
    }

    @Override
    public boolean isValidForInsertion(RESOURCE type, AutomationType automationType) {
        return isValid(type) && canInsert.test(type, automationType);
    }

    protected int getInsertionRate(@Nullable AutomationType automationType) {
        //TODO - 26.1: Make sure that inventory slots properly support this and getExtractionRate
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? Integer.MAX_VALUE : rate.getAsInt();
    }

    protected int getExtractionRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? Integer.MAX_VALUE : rate.getAsInt();
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            //"Fail quick" if the given stack is empty
            return 0;
        }
        AttachedResources<RESOURCE> attached = getAttached();
        LargeResourceStack<RESOURCE> current = getContents(attached);
        return insert(attached, current.resource(), current.amount(), resource, amount, transaction, automationType);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int insert(AttachedResources<RESOURCE> attached, RESOURCE currentType, @Range(from = 0, to = Long.MAX_VALUE) long currentAmount, RESOURCE resource,
          @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        if (amount == 0) {
            //"Fail quick" if the given resource is empty
            return 0;
        }
        //Validate that we aren't at max stack size before we try to see if we can insert the resource, as on average this will be a cheaper check
        long needed = capacityAsLong(resource) - currentAmount;
        //Limit how much we can add at once to the insertion rate the container sets
        needed = Math.min(needed, getInsertionRate(automationType));
        if (needed <= 0 || !isValidForInsertion(resource, automationType)) {
            //Fail if we are a full slot, or we can never insert the resource or currently are unable to insert it
            return 0;
        } else if (!currentType.isEmpty() && !currentType.equals(resource)) {
            return 0;
        }
        int toAdd = Math.min(amount, Ints.saturatedCast(needed));
        updateSnapshots(transaction);
        //Note: We let setStack handle updating the backing holding stack
        // We use current.getCount + toAdd so that if we are empty we end up at toAdd
        // but if we aren't then we grow by the given amount
        setContents(attached, resource, currentAmount + toAdd);
        return toAdd;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(RESOURCE resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            //"Fail quick" if nothing is actually being extracted
            return 0;
        }
        AttachedResources<RESOURCE> attached = getAttached();
        LargeResourceStack<RESOURCE> current = getContents(attached);
        RESOURCE currentType = current.resource();
        if (!resource.equals(currentType) || !isValidForExtraction(currentType, automationType)) {
            //"Fail quick" if we are empty, a different type is trying to be extracted, or if we can never extract from this slot
            return 0;
        }
        long currentStored = current.amount();
        //If we are trying to extract more than we have, just change it so that we are extracting it all
        int toRemove = Math.min(amount, Ints.saturatedCast(currentStored));
        //Limit how much we can remove at once to the extraction rate the container sets
        toRemove = Math.min(toRemove, getExtractionRate(automationType));
        if (toRemove > 0) {
            updateSnapshots(transaction);
            //Shrink the stack by the amount removed
            setContents(attached, currentType, currentStored - toRemove);
        }
        return toRemove;
    }
}
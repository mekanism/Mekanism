package mekanism.common.attachments.containers;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.IResourceContainer;
import mekanism.api.container.LargeResourceStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault//TODO - 26.1: Do we want to change TYPE to being ResourceStack<RESOURCE>? It would probably make the logic a little cleaner
public abstract class ComponentBackedResourceContainer<RESOURCE extends Resource> extends ComponentBackedContainer<LargeResourceStack<RESOURCE>, AttachedResources<RESOURCE>> implements IResourceContainer<RESOURCE> {

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

    protected abstract RESOURCE getEmptyResource();

    protected abstract Codec<RESOURCE> getResourceCodec();

    @Override
    protected boolean isEmpty(LargeResourceStack<RESOURCE> stack) {
        return stack.isEmpty();
    }

    protected LargeResourceStack<RESOURCE> getResourceStack() {
        return getContents(getAttached());
    }

    @Override
    public RESOURCE getResource() {
        return getResourceStack().resource();
    }

    @Override
    public long amountAsLong() {
        return getResourceStack().amount();
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

    protected void setContents(AttachedResources<RESOURCE> attached, RESOURCE type, long storedAmount) {
        setContents(attached, new LargeResourceStack<>(type, storedAmount));
    }

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
        AttachedResources<RESOURCE> attached = getAttached();
        LargeResourceStack<RESOURCE> current = getContents(attached);
        return insert(attached, current.resource(), current.amount(), resource, amount, transaction, automationType);
    }

    protected int insert(AttachedResources<RESOURCE> attached, RESOURCE currentType, long currentAmount, RESOURCE resource, int amount, TransactionContext transaction, AutomationType automationType) {
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
        AttachedResources<RESOURCE> attached = getAttached();
        LargeResourceStack<RESOURCE> current = getContents(attached);
        RESOURCE currentType = current.resource();
        if (currentType.isEmpty() || !resource.equals(currentType) || !isValidForExtraction(currentType, automationType)) {
            //"Fail quick" if we are empty, a different type is trying to be extracted, or if we can never extract from this slot
            return 0;
        }
        long currentStored = current.amount();
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

    @Override
    public void serialize(ValueOutput output) {
        //TODO - 1.21: This is a copy of BasicInventorySlot#serializeNBT. We might need to also grab the specific overrides of
        // that method as special component backed inventory slots, that then access and put that other data as a different component?
        // Also make sure to override things like TileEntityMekanism#applyInventorySlots and TileEntityMekanism#collectInventorySlots
        LargeResourceStack<RESOURCE> stored = getResourceStack();
        if (!stored.isEmpty()) {
            //TODO - 26.1: Does using stored work fine for if something has multiple types of containers on a single stack?
            // Items used to store to the key "item", but fluids and chemicals used "stored"
            ValueOutput storedOutput = output.child(SerializationConstants.STORED);
            storedOutput.store(SerializationConstants.TYPE, getResourceCodec(), stored.resource());
            storedOutput.putLong(SerializationConstants.AMOUNT, stored.amount());
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        Optional<ValueInput> child = input.child(SerializationConstants.STORED);
        if (child.isPresent()) {
            ValueInput storedInput = child.get();
            RESOURCE resource = storedInput.read(SerializationConstants.TYPE, getResourceCodec()).orElse(getEmptyResource());
            setContentsUnchecked(resource, storedInput.getLongOr(SerializationConstants.AMOUNT, 0));
        }
    }
}
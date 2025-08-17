package mekanism.common.attachments.containers.chemical;

import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.common.attachments.containers.ComponentBackedContainer;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedChemicalTank extends ComponentBackedContainer<ChemicalStack, AttachedChemicals> implements IChemicalTank {

    private final BiPredicate<ChemicalStack, @NotNull AutomationType> canExtract;
    private final BiPredicate<ChemicalStack, @NotNull AutomationType> canInsert;
    private final Predicate<ChemicalStack> validator;
    @Nullable
    private final ChemicalAttributeValidator attributeValidator;
    private final LongSupplier capacity;
    private final LongSupplier rate;

    public ComponentBackedChemicalTank(ItemStack attachedTo, int tankIndex, BiPredicate<ChemicalStack, @NotNull AutomationType> canExtract,
          BiPredicate<ChemicalStack, @NotNull AutomationType> canInsert, Predicate<ChemicalStack> validator, LongSupplier rate, LongSupplier capacity,
          @Nullable ChemicalAttributeValidator attributeValidator) {
        super(attachedTo, tankIndex);
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        this.capacity = capacity;
        this.rate = rate;
        this.attributeValidator = attributeValidator;
    }

    @Override
    protected ChemicalStack copy(ChemicalStack toCopy) {
        return toCopy.copy();
    }

    @Override
    protected boolean isEmpty(ChemicalStack value) {
        return value.isEmpty();
    }

    @Override
    protected ContainerType<?, AttachedChemicals, ?> containerType() {
        return ContainerType.CHEMICAL;
    }

    /**
     * @apiNote Try to minimize the number of calls to this method so that we don't have to look up the data component multiple times.
     */
    @Override
    public ChemicalStack getStack() {
        return getContents(getAttached());
    }

    @Override
    public void setStack(ChemicalStack stack) {
        setStackUnchecked(stack);
    }

    @Override
    public void setStackUnchecked(ChemicalStack stack) {
        setContents(getAttached(), stack);
    }

    @Override
    public ChemicalAttributeValidator getAttributeValidator() {
        return attributeValidator == null ? IChemicalTank.super.getAttributeValidator() : attributeValidator;
    }

    protected long getInsertRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? Long.MAX_VALUE : rate.getAsLong();
    }

    protected long getExtractRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? Long.MAX_VALUE : rate.getAsLong();
    }

    @Override
    public long getCapacity() {
        return capacity.getAsLong();
    }

    @Override
    public boolean isValid(ChemicalStack stack) {
        return getAttributeValidator().process(stack) && validator.test(stack);
    }

    @Override
    public ChemicalStack insert(ChemicalStack stack, Action action, AutomationType automationType) {
        //TODO - 1.21: Items do the is valid and canInsert check after checking the needed amount. Should we do the same for fluids
        // or should items have the order flipped? In general calculating the needed amount is likely cheaper which is likely why items do it first
        if (stack.isEmpty() || !isValid(stack) || !canInsert.test(stack, automationType)) {
            //"Fail quick" if the given stack is empty, or we can never insert the fluid or currently are unable to insert it
            return stack;
        }
        AttachedChemicals attachedChemicals = getAttached();
        ChemicalStack stored = getContents(attachedChemicals);
        long needed = Math.min(getInsertRate(automationType), getNeeded(stored));
        if (needed <= 0) {
            //Fail if we are a full tank or our rate is zero
            return stack;
        } else if (stored.isEmpty() || ChemicalStack.isSameChemical(stored, stack)) {
            long toAdd = Math.min(stack.getAmount(), needed);
            if (action.execute()) {
                //Note: We let setStack handle updating the backing holding stack
                // We use stored.getAmount + toAdd so that if we are empty we end up at toAdd
                // but if we aren't then we grow by the given amount
                setContents(attachedChemicals, stack.copyWithAmount(stored.getAmount() + toAdd));
            }
            return stack.copyWithAmount(stack.getAmount() - toAdd);
        }
        //If we didn't accept this fluid, then just return the given stack
        return stack;
    }

    @Override
    public final ChemicalStack extract(long amount, Action action, AutomationType automationType) {
        if (amount < 1) {
            //"Fail quick" if the amount being requested is less than one
            return ChemicalStack.EMPTY;
        }
        AttachedChemicals attachedChemicals = getAttached();
        return extract(attachedChemicals, getContents(attachedChemicals), amount, action, automationType);
    }

    protected ChemicalStack extract(AttachedChemicals attachedChemicals, ChemicalStack stored, long amount, Action action, AutomationType automationType) {
        if (amount < 1 || stored.isEmpty() || !canExtract.test(stored, automationType)) {
            //"Fail quick" if we don't can never extract from this tank, have a fluid stored, or the amount being requested is less than one
            return ChemicalStack.EMPTY;
        }
        //Note: While we technically could just return the stack itself if we are removing all that we have, it would require a lot more checks
        // We also are limiting it by the rate this tank has
        long size = Math.min(Math.min(getExtractRate(automationType), stored.getAmount()), amount);
        if (size == 0) {
            return ChemicalStack.EMPTY;
        }
        ChemicalStack ret = stored.copyWithAmount(size);
        if (!ret.isEmpty() && action.execute()) {
            //Note: We let setStack handle updating the backing holding stack
            setContents(attachedChemicals, stored.copyWithAmount(stored.getAmount() - ret.getAmount()));
        }
        return ret;
    }

    @Override
    public final long setStackSize(long amount, Action action) {
        AttachedChemicals attachedChemicals = getAttached();
        return setStackSize(attachedChemicals, getContents(attachedChemicals), amount, action);
    }

    protected long setStackSize(AttachedChemicals attachedChemicals, ChemicalStack stored, long amount, Action action) {
        if (stored.isEmpty()) {
            return 0;
        } else if (amount <= 0) {
            if (action.execute()) {
                setContents(attachedChemicals, ChemicalStack.EMPTY);
            }
            return 0;
        }
        long maxStackSize = getCapacity();
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }
        if (stored.getAmount() == amount || action.simulate()) {
            //If our size is not changing, or we are only simulating the change, don't do anything
            return amount;
        }
        setContents(attachedChemicals, stored.copyWithAmount(amount));
        return amount;
    }

    @Override
    public long growStack(long amount, Action action) {
        AttachedChemicals attachedChemicals = getAttached();
        ChemicalStack stored = getContents(attachedChemicals);
        long current = stored.getAmount();
        if (current == 0) {
            //"Fail quick" if our stack is empty, so we can't grow it
            return 0;
        } else if (amount > 0) {
            //Cap adding amount at how much we need, so that we don't risk integer overflow
            //If we are increasing the stack's size, use the insert rate
            amount = Math.min(Math.min(amount, getNeeded(stored)), getInsertRate(null));
        } else if (amount < 0) {
            //If we are decreasing the stack's size, use the extract rate
            amount = Math.max(amount, -getExtractRate(null));
        }
        long newSize = setStackSize(attachedChemicals, stored, current + amount, action);
        return newSize - current;
    }

    protected long getNeeded(ChemicalStack stored) {
        //Skip the stack lookup for getNeeded
        return Math.max(0, getCapacity() - stored.getAmount());
    }

    @Override
    public CompoundTag serializeNBT(Provider provider) {
        CompoundTag nbt = new CompoundTag();
        ChemicalStack stored = getStack();
        if (!stored.isEmpty()) {
            nbt.put(SerializationConstants.STORED, stored.save(provider));
        }
        return nbt;
    }
}
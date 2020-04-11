package mekanism.api.chemical;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.inventory.AutomationType;
import net.minecraft.nbt.CompoundNBT;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BasicChemicalTank<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements IChemicalTank<CHEMICAL, STACK> {

    private final Predicate<@NonNull CHEMICAL> validator;
    protected final BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canExtract;
    protected final BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canInsert;
    @Nullable
    private final ChemicalAttributeValidator attributeValidator;
    private final long capacity;
    /**
     * @apiNote This is only protected for direct querying access. To modify this stack the external methods or {@link #setStackUnchecked(STACK)} should be used instead.
     */
    protected STACK stored;

    protected BasicChemicalTank(long capacity, BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canExtract, BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canInsert,
        Predicate<@NonNull CHEMICAL> validator, @Nullable ChemicalAttributeValidator attributeValidator) {
        this.capacity = capacity;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        this.attributeValidator = attributeValidator;
        stored = getEmptyStack();
    }

    /**
     * {@inheritDoc}
     *
     * @apiNote We return a cached value from this that if modified won't actually end up having any information about the tank get changed.
     */
    @Override
    public STACK getStack() {
        //TODO: Debate doing what the JavaDoc says. See BasicInventorySlot#getStack for details
        return stored;
    }

    @Override
    public void setStack(STACK stack) {
        setStack(stack, true);
    }

    /**
     * Helper method to allow easily setting a rate at which this {@link BasicChemicalTank} can insert/extract chemicals.
     *
     * @param automationType The automation type to limit the rate by or null if we don't have access to an automation type.
     *
     * @return The rate this tank can insert/extract at.
     *
     * @implNote By default this returns {@link Long#MAX_VALUE} so as to not actually limit the tank's rate. By default this is also ignored for direct setting of the
     * stack/stack size
     */
    protected long getRate(@Nullable AutomationType automationType) {
        //TODO: Decide if we want to split this into a rate for inserting and a rate for extracting.
        return Long.MAX_VALUE;
    }

    protected void setStackUnchecked(STACK stack) {
        setStack(stack, false);
    }

    private void setStack(STACK stack, boolean validateStack) {
        if (stack.isEmpty()) {
            stored = getEmptyStack();
        } else if (!validateStack || isValid(stack)) {
            stored = createStack(stack, stack.getAmount());
        } else {
            //Throws a RuntimeException as specified is allowed when something unexpected happens
            // As setStack is more meant to be used as an internal method
            throw new RuntimeException("Invalid chemical for tank: " + stack.getType().getRegistryName() + " " + stack.getAmount());
        }
        onContentsChanged();
    }

    @Override
    public STACK insert(@Nonnull STACK stack, Action action, AutomationType automationType) {
        if (stack.isEmpty() || !isValid(stack) || !canInsert.test(stack.getType(), automationType)) {
            //"Fail quick" if the given stack is empty or we can never insert the chemical or currently are unable to insert it
            return stack;
        }
        long needed = Math.min(getRate(automationType), getNeeded());
        if (needed <= 0) {
            //Fail if we are a full tank or our rate is zero
            return stack;
        }
        boolean sameType = false;
        if (isEmpty() || (sameType = isTypeEqual(stack))) {
            long toAdd = Math.min(stack.getAmount(), needed);
            if (action.execute()) {
                //If we want to actually insert the chemical, then update the current chemical
                if (sameType) {
                    //We can just grow our stack by the amount we want to increase it
                    stored.grow(toAdd);
                    onContentsChanged();
                } else {
                    //If we are not the same type then we have to copy the stack and set it
                    // Just set it unchecked as we have already validated it
                    // Note: this also will mark that the contents changed
                    setStackUnchecked(createStack(stack, toAdd));
                }
            }
            return createStack(stack, stack.getAmount() - toAdd);
        }
        //If we didn't accept this chemical, then just return the given stack
        return stack;
    }

    @Override
    public STACK extract(long amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount < 1 || !canExtract.test(stored.getType(), automationType)) {
            //"Fail quick" if we don't can never extract from this tank, have a chemical stored, or the amount being requested is less than one
            return getEmptyStack();
        }
        //Note: While we technically could just return the stack itself if we are removing all that we have, it would require a lot more checks
        // We also are limiting it by the rate this tank has
        long size = Math.min(Math.min(getRate(automationType), getStored()), amount);
        if (size == 0) {
            return getEmptyStack();
        }
        STACK ret = createStack(stored, size);
        if (!ret.isEmpty() && action.execute()) {
            //If shrink gets the size to zero it will update the empty state so that isEmpty() returns true.
            stored.shrink(ret.getAmount());
            onContentsChanged();
        }
        return ret;
    }

    @Override
    public boolean isValid(STACK stack) {
        return ChemicalAttributeValidator.process(stack, getAttributeValidator()) && validator.test(stack.getType());
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public long setStackSize(long amount, Action action) {
        if (isEmpty()) {
            return 0;
        } else if (amount <= 0) {
            if (action.execute()) {
                setEmpty();
            }
            return 0;
        }
        long maxStackSize = getCapacity();
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }
        if (getStored() == amount || action.simulate()) {
            //If our size is not changing or we are only simulating the change, don't do anything
            return amount;
        }
        stored.setAmount(amount);
        onContentsChanged();
        return amount;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten so that we can make this obey the rate limit our tank may have
     */
    @Override
    public long growStack(long amount, Action action) {
        //TODO: We should go through all the places we have TODOs about errors/warnings, and debate removing them/add
        // some form of graceful handling as it is valid they may not grow the full amount due to rate limiting
        // Though I believe most places we manually call it we have already done a simulation, which should really
        // have caught any rate limit issues
        long current = getStored();
        if (amount > 0) {
            //Cap adding amount at how much we need, so that we don't risk long overflow
            amount = Math.min(Math.min(amount, getNeeded()), getRate(null));
        } else if (amount < 0) {
            amount = Math.max(amount, -getRate(null));
        }
        long newSize = setStackSize(current + amount, action);
        return newSize - current;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying.
     */
    @Override
    public boolean isEmpty() {
        return stored.isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying.
     */
    @Override
    public long getStored() {
        return stored.getAmount();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying.
     */
    @Override
    public CHEMICAL getType() {
        return stored.getType();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying.
     */
    @Override
    public boolean isTypeEqual(STACK other) {
        return stored.isTypeEqual(other);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying.
     */
    @Override
    public boolean isTypeEqual(CHEMICAL other) {
        return stored.isTypeEqual(other);
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public ChemicalAttributeValidator getAttributeValidator() {
        return attributeValidator != null ? attributeValidator : IChemicalTank.super.getAttributeValidator();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying.
     */
    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (!isEmpty()) {
            nbt.put(NBTConstants.STORED, stored.write(new CompoundNBT()));
        }
        return nbt;
    }
}
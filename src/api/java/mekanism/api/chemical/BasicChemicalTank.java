package mekanism.api.chemical;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.AutomationType;
import net.minecraft.nbt.CompoundNBT;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BasicChemicalTank<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements IChemicalTank<CHEMICAL, STACK> {

    private final Predicate<@NonNull CHEMICAL> validator;
    private final BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canExtract;
    private final BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canInsert;
    private final int capacity;
    /**
     * @apiNote This is only protected for direct querying access. To modify this stack the external methods or {@link #setStackUnchecked(STACK)} should be used
     * instead.
     */
    protected STACK stored;

    protected BasicChemicalTank(int capacity, BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canExtract, BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canInsert,
          Predicate<@NonNull CHEMICAL> validator) {
        this.capacity = capacity;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        stored = getEmptyStack();
    }

    /**
     * {@inheritDoc}
     *
     * @apiNote We return a cached value from this that if modified won't actually end up having any information about the slot get changed.
     */
    @Override
    public STACK getStack() {
        //TODO: Debate doing what the JavaDoc says. See BasicInventorySlot#getStack for details
        return stored;
    }

    @Override
    public void setStack(STACK stack) {
        //TODO: Should we allow forcefully setting invalid items? At least we need to go through them and check to make sure we allow setting an empty container??
        // This error of empty container not being valid may not even be an issue once we move logic for resources into the specific slots
        setStack(stack, true);
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

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public STACK insert(@Nonnull STACK stack, Action action, AutomationType automationType) {
        if (stack.isEmpty() || !isValid(stack) || !canInsert.test(stack.getType(), automationType)) {
            //"Fail quick" if the given stack is empty or we can never insert the item or currently are unable to insert it
            return stack;
        }
        int needed = getNeeded();
        if (needed <= 0) {
            //Fail if we are a full slot
            return stack;
        }
        boolean sameType = false;
        if (isEmpty() || (sameType = stored.isTypeEqual(stack))) {
            int toAdd = Math.min(stack.getAmount(), needed);
            if (action.execute()) {
                //If we want to actually insert the item, then update the current item
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

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public STACK extract(int amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount < 1 || !canExtract.test(stored.getType(), automationType)) {
            //"Fail quick" if we don't can never extract from this slot, have an item stored, or the amount being requested is less than one
            return getEmptyStack();
        }
        //Note: While we technically could just return the stack itself if we are removing all that we have, it would require a lot more checks
        STACK ret = createStack(stored, Math.min(getStored(), amount));
        if (!ret.isEmpty() && action.execute()) {
            //If shrink gets the size to zero it will update the empty state so that isEmpty() returns true.
            stored.shrink(ret.getAmount());
            onContentsChanged();
        }
        return ret;
    }

    @Override
    public boolean isValid(STACK stack) {
        return validator.test(stack.getType());
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public int setStackSize(int amount, Action action) {
        if (isEmpty()) {
            return 0;
        } else if (amount <= 0) {
            if (action.execute()) {
                setStack(getEmptyStack());
            }
            return 0;
        }
        int maxStackSize = getCapacity();
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
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public boolean isEmpty() {
        return stored.isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public int getStored() {
        return stored.getAmount();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public CHEMICAL getType() {
        return stored.getType();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (!isEmpty()) {
            nbt.put("stored", stored.write(new CompoundNBT()));
        }
        return nbt;
    }
}
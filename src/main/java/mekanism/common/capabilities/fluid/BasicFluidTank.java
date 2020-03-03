package mekanism.common.capabilities.fluid;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.inventory.AutomationType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BasicFluidTank implements IExtendedFluidTank {

    public static final Predicate<@NonNull FluidStack> alwaysTrue = stack -> true;
    public static final Predicate<@NonNull FluidStack> alwaysFalse = stack -> false;
    public static final BiPredicate<@NonNull FluidStack, @NonNull AutomationType> alwaysTrueBi = (stack, automationType) -> true;
    public static final BiPredicate<@NonNull FluidStack, @NonNull AutomationType> manualOnly = (stack, automationType) -> automationType == AutomationType.MANUAL;
    public static final BiPredicate<@NonNull FluidStack, @NonNull AutomationType> internalOnly = (stack, automationType) -> automationType == AutomationType.INTERNAL;

    public static BasicFluidTank create(int capacity, @Nullable IMekanismFluidHandler fluidHandler) {
        //TODO: Validate capacity is positive
        return new BasicFluidTank(capacity, alwaysTrueBi, alwaysTrueBi, alwaysTrue, fluidHandler);
    }

    public static BasicFluidTank create(int capacity, Predicate<@NonNull FluidStack> validator, @Nullable IMekanismFluidHandler fluidHandler) {
        //TODO: Validate capacity is positive
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new BasicFluidTank(capacity, alwaysTrueBi, alwaysTrueBi, validator, fluidHandler);
    }

    public static BasicFluidTank create(int capacity, Predicate<@NonNull FluidStack> canExtract, Predicate<@NonNull FluidStack> canInsert,
          @Nullable IMekanismFluidHandler fluidHandler) {
        return create(capacity, canExtract, canInsert, alwaysTrue, fluidHandler);
    }

    public static BasicFluidTank input(int capacity, Predicate<@NonNull FluidStack> validator, @Nullable IMekanismFluidHandler fluidHandler) {
        //TODO: Validate capacity is positive
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new BasicFluidTank(capacity, manualOnly, alwaysTrueBi, validator, fluidHandler);
    }

    public static BasicFluidTank output(int capacity, @Nullable IMekanismFluidHandler fluidHandler) {
        //TODO: Validate capacity is positive
        return new BasicFluidTank(capacity, alwaysTrueBi, internalOnly, alwaysTrue, fluidHandler);
    }

    public static BasicFluidTank create(int capacity, Predicate<@NonNull FluidStack> canExtract, Predicate<@NonNull FluidStack> canInsert,
          Predicate<@NonNull FluidStack> validator, @Nullable IMekanismFluidHandler fluidHandler) {
        //TODO: Validate capacity is positive
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new BasicFluidTank(capacity, canExtract, canInsert, validator, fluidHandler);
    }

    /**
     * @apiNote This is only protected for direct querying access. To modify this stack the external methods or {@link #setStackUnchecked(FluidStack)} should be used
     * instead.
     */
    protected FluidStack stored = FluidStack.EMPTY;
    private final Predicate<@NonNull FluidStack> validator;
    protected final BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canExtract;
    protected final BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canInsert;
    private final int capacity;
    @Nullable
    private final IMekanismFluidHandler fluidHandler;

    protected BasicFluidTank(int capacity, Predicate<@NonNull FluidStack> canExtract, Predicate<@NonNull FluidStack> canInsert, Predicate<@NonNull FluidStack> validator,
          @Nullable IMekanismFluidHandler fluidHandler) {
        this(capacity, (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), (stack, automationType) -> canInsert.test(stack),
              validator, fluidHandler);
    }

    protected BasicFluidTank(int capacity, BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canExtract, BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canInsert,
          Predicate<@NonNull FluidStack> validator, @Nullable IMekanismFluidHandler fluidHandler) {
        this.capacity = capacity;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        this.fluidHandler = fluidHandler;
    }

    @Override
    public void onContentsChanged() {
        if (fluidHandler != null) {
            fluidHandler.onContentsChanged();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @apiNote We return a cached value from this that if modified won't actually end up having any information about the slot get changed.
     */
    @Override
    public FluidStack getFluid() {
        //TODO: Debate doing what the JavaDoc says. See BasicInventorySlot#getStack for details
        return stored;
    }

    @Override
    public void setStack(FluidStack stack) {
        //TODO: Should we allow forcefully setting invalid items? At least we need to go through them and check to make sure we allow setting an empty container??
        // This error of empty container not being valid may not even be an issue once we move logic for resources into the specific slots
        setStack(stack, true);
    }

    /**
     * Helper method to allow easily setting a rate at which this {@link BasicFluidTank} can insert/extract fluids.
     *
     * @param automationType The automation type to limit the rate by or null if we don't have access to an automation type.
     *
     * @return The rate this tank can insert/extract at.
     *
     * @implNote By default this returns {@link Integer#MAX_VALUE} so as to not actually limit the tank's rate.
     * @apiNote By default this is ignored for direct setting of the stack/stack size
     */
    protected int getRate(@Nullable AutomationType automationType) {
        //TODO: Decide if we want to split this into a rate for inserting and a rate for extracting.
        return Integer.MAX_VALUE;
    }

    protected void setStackUnchecked(FluidStack stack) {
        setStack(stack, false);
    }

    private void setStack(FluidStack stack, boolean validateStack) {
        if (stack.isEmpty()) {
            stored = FluidStack.EMPTY;
        } else if (!validateStack || isFluidValid(stack)) {
            stored = new FluidStack(stack, stack.getAmount());
        } else {
            //Throws a RuntimeException as specified is allowed when something unexpected happens
            // As setStack is more meant to be used as an internal method
            throw new RuntimeException("Invalid fluid for tank: " + stack.getFluid().getRegistryName() + " " + stack.getAmount());
        }
        onContentsChanged();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getFluid()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public FluidStack insert(@Nonnull FluidStack stack, Action action, AutomationType automationType) {
        if (stack.isEmpty() || !isFluidValid(stack) || !canInsert.test(stack, automationType)) {
            //"Fail quick" if the given stack is empty or we can never insert the item or currently are unable to insert it
            return stack;
        }
        int needed = Math.min(getRate(automationType), getNeeded());
        if (needed <= 0) {
            //Fail if we are a full slot or our rate is zero
            return stack;
        }
        boolean sameType = false;
        if (isEmpty() || (sameType = stored.isFluidEqual(stack))) {
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
                    setStackUnchecked(new FluidStack(stack, toAdd));
                }
            }
            return new FluidStack(stack, stack.getAmount() - toAdd);
        }
        //If we didn't accept this fluid, then just return the given stack
        return stack;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getFluid()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public FluidStack extract(int amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount < 1 || !canExtract.test(stored, automationType)) {
            //"Fail quick" if we don't can never extract from this slot, have an item stored, or the amount being requested is less than one
            return FluidStack.EMPTY;
        }
        //Note: While we technically could just return the stack itself if we are removing all that we have, it would require a lot more checks
        // We also are limiting it by the rate this tank has
        int size = Math.min(Math.min(getRate(automationType), getFluidAmount()), amount);
        if (size == 0) {
            return FluidStack.EMPTY;
        }
        FluidStack ret = new FluidStack(stored, size);
        if (!ret.isEmpty() && action.execute()) {
            //If shrink gets the size to zero it will update the empty state so that isEmpty() returns true.
            stored.shrink(ret.getAmount());
            onContentsChanged();
        }
        return ret;
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
        return validator.test(stack);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getFluid()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public int setStackSize(int amount, Action action) {
        if (isEmpty()) {
            return 0;
        } else if (amount <= 0) {
            if (action.execute()) {
                setEmpty();
            }
            return 0;
        }
        int maxStackSize = getCapacity();
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }
        if (getFluidAmount() == amount || action.simulate()) {
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
    public int growStack(int amount, Action action) {
        //TODO: We should go through all the places we have TODOs about errors/warnings, and debate removing them/add
        // some form of graceful handling as it is valid they may not grow the full amount due to rate limiting
        // Though I believe most places we manually call it we have already done a simulation, which should really
        // have caught any rate limit issues
        int current = getFluidAmount();
        if (amount > 0) {
            amount = Math.min(amount, getRate(null));
        } else if (amount < 0) {
            amount = Math.max(amount, -getRate(null));
        }
        int newSize = setStackSize(current + amount, action);
        return newSize - current;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getFluid()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public boolean isEmpty() {
        return stored.isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getFluid()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public boolean isFluidEqual(FluidStack other) {
        return stored.isFluidEqual(other);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getFluid()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public int getFluidAmount() {
        return stored.getAmount();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getFluid()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (!isEmpty()) {
            nbt.put("stored", stored.writeToNBT(new CompoundNBT()));
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("stored")) {
            setStackUnchecked(FluidStack.loadFluidStackFromNBT(nbt.getCompound("stored")));
        }
    }
}
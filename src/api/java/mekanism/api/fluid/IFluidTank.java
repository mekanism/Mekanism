package mekanism.api.fluid;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.IResourceContainer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

@NothingNullByDefault
public interface IFluidTank extends IResourceContainer<FluidResource> {

    /**
     * Overrides the stack in this {@link IFluidTank}.
     *
     * @param stack {@link FluidStack} to set this tanks' contents to (may be empty).
     *
     * @throws RuntimeException if this tank is called in a way that it was not expecting.
     * @implNote If the internal stack does get updated make sure to call {@link #onContentsChanged()}
     */
    default void setStack(FluidStack stack) {//TODO - 26.1: Re-evaluate callers
        setContents(FluidResource.of(stack), stack.amount());
    }

    /**
     * Overrides the stack in this {@link IFluidTank}.
     *
     * @param stack {@link FluidStack} to set this tank's contents to (may be empty).
     *
     * @apiNote Unsafe version of {@link #setStack(FluidStack)}. This method is exposed for implementation and code deduplication reasons only and should
     * <strong>NOT</strong> be directly called outside your own {@link IFluidTank} where you already know the given {@link FluidStack} is valid, or on the
     * client side for purposes of receiving sync data and rendering.
     * @implNote If the internal stack does get updated make sure to call {@link #onContentsChanged()}
     */
    default void setStackUnchecked(FluidStack stack) {//TODO - 26.1: Re-evaluate callers
        setContentsUnchecked(FluidResource.of(stack), stack.amount());
    }

    void setContentsUnchecked(FluidResource type, int storedAmount);

    /**
     * <p>
     * Inserts a {@link FluidStack} into this {@link IFluidTank} and return the remainder. The {@link FluidStack} <em>should not</em> be modified in this
     * function!
     * </p>
     * Note: This behaviour is subtly <strong>different</strong> from {@link IFluidHandler#fill(FluidStack, FluidAction)}
     *
     * @param stack          {@link FluidStack} to insert. This must not be modified by the tank.
     * @param action         The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param automationType The method that this tank is being interacted from.
     *
     * @return The remaining {@link FluidStack} that was not inserted (if the entire stack is accepted, then return an empty {@link FluidStack}). May be the same as the
     * input {@link FluidStack} if unchanged, otherwise a new {@link FluidStack}. The returned {@link FluidStack} can be safely modified after
     *
     * @implNote The {@link FluidStack} <em>should not</em> be modified in this function! If the internal stack does get updated make sure to call
     * {@link #onContentsChanged()}. It is also recommended to override this if your internal {@link FluidStack} is mutable so that a copy does not have to be made every
     * run
     */
    @Deprecated(forRemoval = true)//TODO - 26.1: Remove this
    default FluidStack insert(FluidStack stack, Action action, AutomationType automationType) {
        if (stack.isEmpty()) {
            //"Fail quick" if the given stack is empty, or we can never insert the item or currently are unable to insert it
            return stack;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = insert(FluidResource.of(stack), stack.amount(), transaction, automationType);
            if (action.execute()) {
                transaction.commit();
            }
            return stack.copyWithAmount(stack.amount() - inserted);
        }
    }

    /**
     * Extracts a {@link FluidStack} from this {@link IFluidTank}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount         Amount to extract (may be greater than the current stack's max limit)
     * @param action         The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param automationType The method that this tank is being interacted from.
     *
     * @return {@link FluidStack} extracted from the tank, must be empty if nothing can be extracted. The returned {@link FluidStack} can be safely modified after, so the
     * tank should return a new or copied stack.
     *
     * @implNote The returned {@link FluidStack} can be safely modified after, so a new or copied stack should be returned. If the internal stack does get updated make
     * sure to call {@link #onContentsChanged()}. It is also recommended to override this if your internal {@link FluidStack} is mutable so that a copy does not have to
     * be made every run
     */
    @Deprecated(forRemoval = true)//TODO - 26.1: Remove this
    default FluidStack extract(int amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount < 1) {
            return FluidStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            FluidResource resource = getResource();
            int extracted = extract(resource, amount, transaction, automationType);
            if (action.execute()) {
                transaction.commit();
            }
            return resource.toStack(extracted);
        }
    }

    /**
     * Convenience method for modifying the size of the stored stack.
     * <p>
     * If there is a stack stored in this tank, set the size of it to the given amount. Capping at this fluid tank's limit. If the amount is less than or equal to zero,
     * then this instead sets the stack to the empty stack.
     *
     * @param amount The desired size to set the stack to.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return Actual size the stack was set to.
     *
     * @implNote It is recommended to override this if your internal {@link FluidStack} is mutable so that a copy does not have to be made every run. If the internal
     * stack does get updated make sure to call {@link #onContentsChanged()}
     */
    @Deprecated(forRemoval = true)//TODO - 26.1: Remove this
    default int setStackSize(int amount, Action action) {
        if (isEmpty()) {
            return 0;
        } else if (amount <= 0) {
            if (action.execute()) {
                setEmpty();
            }
            return 0;
        }
        int maxStackSize = getCurrentLimit();
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }
        if (amount() == amount || action.simulate()) {
            //If our size is not changing, or we are only simulating the change, don't do anything
            return amount;
        }
        setContentsUnchecked(getResource(), amount);
        return amount;
    }

    /**
     * Convenience method for growing the size of the stored stack.
     * <p>
     * If there is a stack stored in this tank, increase its size by the given amount. Capping at this fluid tank's limit. If the stack shrinks to an amount of less than
     * or equal to zero, then this instead sets the stack to the empty stack.
     *
     * @param amount The desired amount to grow the stack by.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return Actual amount the stack grew.
     *
     * @apiNote Negative values for amount are valid, and will instead cause the stack to shrink.
     * @implNote If the internal stack does get updated make sure to call {@link #onContentsChanged()}
     */
    @Deprecated(forRemoval = true)//TODO - 26.1: Remove this
    default int growStack(int amount, Action action) {
        int current = amount();
        if (current == 0) {
            //"Fail quick" if our stack is empty, so we can't grow it
            return 0;
        } else if (amount > 0) {
            //Cap adding amount at how much we need, so that we don't risk integer overflow
            amount = Math.min(amount, getNeeded());
        }
        int newSize = setStackSize(current + amount, action);
        return newSize - current;
    }

    /**
     * Convenience method for shrinking the size of the stored stack.
     * <p>
     * If there is a stack stored in this tank, shrink its size by the given amount. If this causes its size to become less than or equal to zero, then the stack is set
     * to the empty stack. If this method is used to grow the stack the size gets capped at this fluid tank's limit.
     *
     * @param amount The desired amount to shrink the stack by.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return Actual amount the stack shrunk.
     *
     * @apiNote Negative values for amount are valid, and will instead cause the stack to grow.
     * @implNote If the internal stack does get updated make sure to call {@link #onContentsChanged()}
     */
    @Deprecated(forRemoval = true)//TODO - 26.1: Remove this
    default int shrinkStack(int amount, Action action) {
        return -growStack(-amount, action);
    }

    @Override
    default void setEmpty() {
        setContents(FluidResource.EMPTY, 0);
    }

    /**
     * Convenience method for checking if this tank's contents are of an equal type to a given fluid stack's.
     *
     * @param other The stack to compare to.
     *
     * @return True if the tank's contents are equal, false otherwise.
     *
     * @implNote If your implementation of {@link #getFluid()} returns a copy, this should be overridden to directly check against the internal stack.
     */
    default boolean isFluidEqual(FluidStack other) {
        return getResource().matches(other);
    }

    @Override
    default void serialize(ValueOutput output) {
        if (!isEmpty()) {
            output.store(SerializationConstants.STORED, FluidStack.CODEC, getFluid());
        }
    }

    @Override
    default void deserialize(ValueInput input) {
        setStackUnchecked(input.read(SerializationConstants.STORED, FluidStack.CODEC).orElse(FluidStack.EMPTY));
    }

    @Deprecated(forRemoval = true)//TODO - 26.1: From IFluidTank
    default FluidStack getFluid() {
        return getResource().toStack(amount());
    }
}
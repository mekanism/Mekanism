package mekanism.api.chemical;

import mekanism.api.Action;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.container.IResourceContainer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

@NothingNullByDefault
public interface IChemicalTank extends IResourceContainer<ChemicalResource> {

    /**
     * Returns the {@link ChemicalStack} in this tank.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link ChemicalStack} <em>MUST NOT</em> be modified. This method is not for altering internal contents. Any implementers who are
     * able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the stack returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED CHEMICAL STACK</em></strong>
     * </p>
     *
     * @return {@link ChemicalStack} in this tank. EMPTY instance of the {@link ChemicalStack} if the tank is empty.
     */
    default ChemicalStack getStack() {
        return getResource().toStack(amountAsLong());
    }

    /**
     * Overrides the stack in this {@link IChemicalTank}.
     *
     * @param stack {@link ChemicalStack} to set this tank's contents to (may be empty).
     *
     * @throws RuntimeException if this tank is called in a way that it was not expecting.
     * @implNote If the internal stack does get updated make sure to call {@link #onContentsChanged()}
     */
    default void setStack(ChemicalStack stack) {
        setContents(ChemicalResource.of(stack), stack.amount());
    }

    /**
     * Overrides the stack in this {@link IChemicalTank}.
     *
     * @param stack {@link ChemicalStack} to set this tank's contents to (may be empty).
     *
     * @apiNote Unsafe version of {@link #setStack(ChemicalStack)}. This method is exposed for implementation and code deduplication reasons only and should
     * <strong>NOT</strong> be directly called outside your own {@link IChemicalTank} where you already know the given {@link ChemicalStack} is valid, or on the
     * client side for purposes of receiving sync data and rendering.
     * @implNote If the internal stack does get updated make sure to call {@link #onContentsChanged()}
     */
    default void setStackUnchecked(ChemicalStack stack) {
        setContentsUnchecked(ChemicalResource.of(stack), stack.amount());
    }

    /**
     * Retrieves the maximum stack size allowed to exist in this {@link IChemicalTank}.
     *
     * @return The maximum stack size allowed in this {@link IChemicalTank}.
     */
    @Deprecated(forRemoval = true)//TODO - 26.1: Replace with limits
    default long getCapacity() {
        return getLimitAsLong(ChemicalResource.EMPTY);
    }

    /**
     * <p>
     * This function should be used instead of simulated insertions in cases where the contents and state of the tank are irrelevant, mainly for the purpose of automation
     * and logic.
     * </p>
     * <ul>
     * <li>isValid is false when insertion of the chemical is never valid.</li>
     * <li>When isValid is true, no assumptions can be made and insertion must be simulated case-by-case.</li>
     * <li>The actual chemical stacks in the tank, its fullness, or any other state are <strong>not</strong> considered by isValid.</li>
     * </ul>
     *
     * @param stack Stack to test with for validity
     *
     * @return true if this {@link IChemicalTank} can accept the {@link ChemicalStack}, not considering the current state of the tank. false if this {@link IChemicalTank}
     * can never insert the {@link ChemicalStack} in any situation.
     */
    @Deprecated(forRemoval = true)//TODO - 26.1: Remove this
    default boolean isChemicalValid(ChemicalStack stack) {
        return isValid(ChemicalResource.of(stack));
    }

    /**
     * Convenience method for modifying the size of the stored stack.
     * <p>
     * If there is a stack stored in this tank, set the size of it to the given amount. Capping at this chemical tank's limit. If the amount is less than or equal to
     * zero, then this instead sets the stack to the empty stack.
     *
     * @param amount The desired size to set the stack to.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return Actual size the stack was set to.
     *
     * @implNote It is recommended to override this if your internal {@link ChemicalStack} is mutable so that a copy does not have to be made every run. If the internal
     * stack does get updated make sure to call {@link #onContentsChanged()}
     */
    @Deprecated(forRemoval = true)//TODO - 26.1: Remove this
    default long setStackSize(long amount, Action action) {
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
        if (amountAsLong() == amount || action.simulate()) {
            //If our size is not changing, or we are only simulating the change, don't do anything
            return amount;
        }
        setContents(getResource(), amount);
        return amount;
    }

    /**
     * Convenience method for growing the size of the stored stack.
     * <p>
     * If there is a stack stored in this tank, increase its size by the given amount. Capping at this chemical tank's limit. If the stack shrinks to an amount of less
     * than or equal to zero, then this instead sets the stack to the empty stack.
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
    default long growStack(long amount, Action action) {
        long current = amountAsLong();
        if (current == 0) {
            //"Fail quick" if our stack is empty, so we can't grow it
            return 0;
        } else if (amount > 0) {
            //Cap adding amount at how much we need, so that we don't risk long overflow
            amount = Math.min(amount, getNeededAsLong());
        }
        long newSize = setStackSize(current + amount, action);
        return newSize - current;
    }

    /**
     * Convenience method for shrinking the size of the stored stack.
     * <p>
     * If there is a stack stored in this tank, shrink its size by the given amount. If this causes its size to become less than or equal to zero, then the stack is set
     * to the empty stack. If this method is used to grow the stack the size gets capped at this chemical tank's limit.
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
    default long shrinkStack(long amount, Action action) {
        return -growStack(-amount, action);
    }

    @Override
    default void setEmpty() {
        setContents(ChemicalResource.EMPTY, 0);
    }

    /**
     * Gets the attribute validator used by this tank. By default, this tank will not allow any chemicals that require validation.
     *
     * @return the tank's attribute validator
     */
    default ChemicalAttributeValidator getAttributeValidator() {
        return ChemicalAttributeValidator.DEFAULT;
    }

    @Override
    default void serialize(ValueOutput output) {
        if (!isEmpty()) {
            output.store(SerializationConstants.STORED, ChemicalStack.CODEC, getStack());
        }
    }

    @Override
    default void deserialize(ValueInput input) {
        setStackUnchecked(input.read(SerializationConstants.STORED, ChemicalStack.CODEC).orElse(ChemicalStack.EMPTY));
    }
}
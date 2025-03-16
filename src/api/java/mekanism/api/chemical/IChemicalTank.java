package mekanism.api.chemical;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

@NothingNullByDefault
public interface IChemicalTank extends INBTSerializable<CompoundTag>, IContentsListener {

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
    ChemicalStack getStack();

    /**
     * Overrides the stack in this {@link IChemicalTank}.
     *
     * @param stack {@link ChemicalStack} to set this tank's contents to (may be empty).
     *
     * @throws RuntimeException if this tank is called in a way that it was not expecting.
     * @implNote If the internal stack does get updated make sure to call {@link #onContentsChanged()}
     */
    void setStack(ChemicalStack stack);

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
    void setStackUnchecked(ChemicalStack stack);

    /**
     * <p>
     * Inserts a {@link ChemicalStack} into this {@link IChemicalTank} and return the remainder. The {@link ChemicalStack} <em>should not</em> be modified in this
     * function!
     * </p>
     * Note: This behaviour is subtly different from {@link IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction)}
     *
     * @param stack          {@link ChemicalStack} to insert. This must not be modified by the tank.
     * @param action         The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param automationType The method that this tank is being interacted from.
     *
     * @return The remaining {@link ChemicalStack} that was not inserted (if the entire stack is accepted, then return an empty {@link ChemicalStack}). May be the same as
     * the input {@link ChemicalStack} if unchanged, otherwise a new {@link ChemicalStack}. The returned {@link ChemicalStack} can be safely modified after
     *
     * @implNote The {@link ChemicalStack} <em>should not</em> be modified in this function! If the internal stack does get updated make sure to call
     * {@link #onContentsChanged()}. It is also recommended to override this if your internal {@link ChemicalStack} is mutable so that a copy does not have to be made
     * every run
     */
    default ChemicalStack insert(ChemicalStack stack, Action action, AutomationType automationType) {
        if (stack.isEmpty() || !isValid(stack)) {
            //"Fail quick" if the given stack is empty, or we can never insert the item or currently are unable to insert it
            return stack;
        }
        long needed = getNeeded();
        if (needed <= 0) {
            //Fail if we are a full tank
            return stack;
        }
        boolean sameType = false;
        if (isEmpty() || (sameType = isTypeEqual(stack))) {
            long toAdd = Math.min(stack.getAmount(), needed);
            if (action.execute()) {
                //If we want to actually insert the chemical, then update the current chemical
                if (sameType) {
                    //We can just grow our stack by the amount we want to increase it
                    // Note: this also will mark that the contents changed
                    growStack(toAdd, action);
                } else {
                    //If we are not the same type then we have to copy the stack and set it
                    // Note: this also will mark that the contents changed
                    setStack(stack.copyWithAmount(toAdd));
                }
            }
            return stack.copyWithAmount(stack.getAmount() - toAdd);
        }
        //If we didn't accept this chemical, then just return the given stack
        return stack;
    }

    /**
     * Extracts a {@link ChemicalStack} from this {@link IChemicalTank}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount         Amount to extract (may be greater than the current stack's max limit)
     * @param action         The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param automationType The method that this tank is being interacted from.
     *
     * @return {@link ChemicalStack} extracted from the tank, must be empty if nothing can be extracted. The returned {@link ChemicalStack} can be safely modified after,
     * so the tank should return a new or copied stack.
     *
     * @implNote The returned {@link ChemicalStack} can be safely modified after, so a new or copied stack should be returned. If the internal stack does get updated make
     * sure to call {@link #onContentsChanged()}. It is also recommended to override this if your internal {@link ChemicalStack} is mutable so that a copy does not have
     * to be made every run
     */
    default ChemicalStack extract(long amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount < 1) {
            return ChemicalStack.EMPTY;
        }
        ChemicalStack ret = getStack().copyWithAmount(Math.min(getStored(), amount));
        if (!ret.isEmpty() && action.execute()) {
            // Note: this also will mark that the contents changed
            shrinkStack(ret.getAmount(), action);
        }
        return ret;
    }

    /**
     * Retrieves the maximum stack size allowed to exist in this {@link IChemicalTank}.
     *
     * @return The maximum stack size allowed in this {@link IChemicalTank}.
     */
    long getCapacity();

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
    boolean isValid(ChemicalStack stack);

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
        if (getStored() == amount || action.simulate()) {
            //If our size is not changing, or we are only simulating the change, don't do anything
            return amount;
        }
        setStack(getStack().copyWithAmount(amount));
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
    default long growStack(long amount, Action action) {
        long current = getStored();
        if (current == 0) {
            //"Fail quick" if our stack is empty, so we can't grow it
            return 0;
        } else if (amount > 0) {
            //Cap adding amount at how much we need, so that we don't risk long overflow
            amount = Math.min(amount, getNeeded());
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
    default long shrinkStack(long amount, Action action) {
        return -growStack(-amount, action);
    }

    /**
     * Convenience method for checking if this tank is empty.
     *
     * @return True if the tank is empty, false otherwise.
     *
     * @implNote If your implementation of {@link #getStack()} returns a copy, this should be overridden to directly check against the internal stack.
     */
    default boolean isEmpty() {
        return getStack().isEmpty();
    }

    /**
     * Convenience method for emptying this {@link IChemicalTank}.
     */
    default void setEmpty() {
        setStack(ChemicalStack.EMPTY);
    }

    /**
     * Convenience method for checking the amount of chemical in this tank.
     *
     * @return The size of the stored stack, or zero is the stack is empty.
     *
     * @implNote If your implementation of {@link #getStack()} returns a copy, this should be overridden to directly check against the internal stack.
     */
    default long getStored() {
        return getStack().getAmount();
    }

    /**
     * Gets the amount of chemical needed by this {@link IChemicalTank} to reach a filled state.
     *
     * @return Amount of chemical needed
     */
    default long getNeeded() {
        return Math.max(0, getCapacity() - getStored());
    }

    /**
     * Convenience method for getting the holder of the {@link Chemical} stored in this tank.
     *
     * @return Backing chemical's holder.
     *
     * @since 10.7.11
     */
    default Holder<Chemical> getTypeHolder() {
        return getStack().getChemicalHolder();
    }

    /**
     * Convenience method for getting the type of the {@link Chemical} stored in this tank.
     *
     * @return chemical type contained
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    default Chemical getType() {
        return getStack().getChemical();
    }

    /**
     * Convenience method for checking if this tank's contents are of an equal type to a given chemical stack's.
     *
     * @param other The stack to compare to.
     *
     * @return True if the tank's contents are equal, false otherwise.
     *
     * @implNote If your implementation of {@link #getStack()} returns a copy, this should be overridden to directly check against the internal stack.
     */
    default boolean isTypeEqual(ChemicalStack other) {
        return ChemicalStack.isSameChemical(getStack(), other);
    }

    /**
     * Convenience method for checking if this tank's contents are of an equal type to a given chemical's.
     *
     * @param other The chemical to compare to.
     *
     * @return True if the tank's contents are equal, false otherwise.
     *
     * @implNote If your implementation of {@link #getStack()} returns a copy, this should be overridden to directly check against the internal stack.
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    default boolean isTypeEqual(Chemical other) {
        return getStack().is(other);
    }

    /**
     * Convenience method for checking if the type {@link Chemical} stored in this tank is equal to the given holder.
     *
     * @param holder Holder to check for the type matching.
     *
     * @return True if the tank's contents are equal, false otherwise.
     *
     * @implNote If your implementation of {@link #getStack()} returns a copy, this should be overridden to directly check against the internal stack.
     * @since 10.7.11
     */
    default boolean isTypeEqual(Holder<Chemical> holder) {
        return getStack().is(holder);
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
    default CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        if (!isEmpty()) {
            nbt.put(SerializationConstants.STORED, getStack().save(provider));
        }
        return nbt;
    }

    @Override
    default void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains(SerializationConstants.STORED, Tag.TAG_COMPOUND)) {
            setStackUnchecked(ChemicalStack.parseOptional(provider, nbt.getCompound(SerializationConstants.STORED)));
        }
    }
}
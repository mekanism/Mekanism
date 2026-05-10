package mekanism.api.energy;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public interface IEnergyContainer extends ValueIOSerializable, IContentsListener {

    /**
     * Returns the energy in this container.
     *
     * @return Energy in this container.
     */
    @Range(from = 0, to = Long.MAX_VALUE)
    long getEnergy();

    /**
     * Overrides the amount of energy in this {@link IEnergyContainer}.
     *
     * @param energy Energy to set this container's contents to. Must be greater than or equal to 0.
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting. Such as if it was not expecting this to be called at all.
     * @implNote If the internal amount does get updated make sure to call {@link #onContentsChanged()}
     */
    void setEnergy(@Range(from = 0, to = Long.MAX_VALUE) long energy);

    /**
     * <p>
     * Inserts energy into this {@link IEnergyContainer} and return the remainder.
     * </p>
     *
     * @param amount         Energy to insert. Must be positive.
     * @param action         The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param automationType The method that this container is being interacted from.
     *
     * @return The remaining energy that was not inserted (if the entire amount is accepted, then return 0).
     *
     * @implNote If the internal amount does get updated make sure to call {@link #onContentsChanged()}.
     */
    @Range(from = 0, to = Long.MAX_VALUE)
    @Deprecated(forRemoval = true)//TODO - 26.1: Switch usages of this to the transactional form
    default long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, Action action, AutomationType automationType) {
        if (amount <= 0 || !isValidForInsertion(automationType)) {
            //"Fail quick" if the given amount is empty
            return amount;
        }
        long needed = getNeeded();
        if (needed == 0) {
            //Fail if we are a full container
            return amount;
        }
        long toAdd = Math.min(amount, needed);
        if (action.execute()) {
            //If we want to actually insert the energy, then update the current energy
            // Note: this also will mark that the contents changed
            setEnergy(getEnergy() + toAdd);
        }
        return amount - toAdd;
    }

    //TODO - 26.1: Docs
    @Range(from = 0, to = Long.MAX_VALUE)
    long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType);

    //TODO - 26.1: Docs
    @Range(from = 0, to = Long.MAX_VALUE)
    long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType);

    /**
     * Extracts energy from this {@link IEnergyContainer}.
     * <p>
     * The returned value must be 0 if nothing is extracted, otherwise its must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount         Amount of energy to extract (may be greater than the current stored amount or the container's capacity). Must be positive or 0.
     * @param action         The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param automationType The method that this container is being interacted from.
     *
     * @return Energy extracted from the container, must be 0 if no energy can be extracted.
     *
     * @implNote If the internal amount does get updated make sure to call {@link #onContentsChanged()}.
     */
    @Range(from = 0, to = Long.MAX_VALUE)
    @Deprecated(forRemoval = true)//TODO - 26.1: Switch usages of this to the transactional form
    default long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount <= 0 || !isValidForExtraction(automationType)) {
            return 0;
        }
        long ret = Math.min(getEnergy(), amount);
        if (ret > 0 && action.execute()) {
            // Note: this also will mark that the contents changed
            setEnergy(getEnergy() - ret);
        }
        return ret;
    }

    /**
     * Retrieves the maximum amount of energy allowed to exist in this {@link IEnergyContainer}.
     *
     * @return The maximum amount of energy allowed in this {@link IEnergyContainer}.
     */
    @Range(from = 0, to = Long.MAX_VALUE)
    long getMaxEnergy();//TODO - 26.1: Document about the fact it can return zero as the max energy?

    /**
     * Ignores current contents
     */
    default boolean isValidForExtraction(AutomationType automationType) {//TODO - 26.1: Update docs
        return true;
    }

    /**
     * Ignores current contents
     */
    default boolean isValidForInsertion(AutomationType automationType) {//TODO - 26.1: Update docs
        return true;
    }

    /**
     * Convenience method for checking if this container is empty.
     *
     * @return True if the container is empty, false otherwise.
     */
    default boolean isEmpty() {
        return getEnergy() == 0L;
    }

    /**
     * Convenience method for emptying this {@link IEnergyContainer}.
     */
    default void setEmpty() {
        setEnergy(0L);
    }

    /**
     * Gets the amount of energy needed by this {@link IEnergyContainer} to reach a filled state.
     *
     * @return Amount of energy needed
     */
    @Range(from = 0, to = Long.MAX_VALUE)
    default long getNeeded() {
        return Math.max(0L, getMaxEnergy() - getEnergy());
    }

    @Override
    default void serialize(ValueOutput output) {
        if (!isEmpty()) {
            output.putLong(SerializationConstants.STORED, getEnergy());
        }
    }
}
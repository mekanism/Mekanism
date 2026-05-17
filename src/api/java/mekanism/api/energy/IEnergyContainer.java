package mekanism.api.energy;

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

    default void copyContents(IEnergyContainer other) {
        setEnergy(other.getEnergy());
    }

    //TODO - 26.1: Docs
    @Range(from = 0, to = Long.MAX_VALUE)
    long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType);

    //TODO - 26.1: Docs
    @Range(from = 0, to = Long.MAX_VALUE)
    long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType);

    /**
     * Retrieves the maximum amount of energy allowed to exist in this {@link IEnergyContainer}.
     *
     * @return The maximum amount of energy allowed in this {@link IEnergyContainer}.
     */
    @Range(from = 0, to = Long.MAX_VALUE)
    long getCapacity();//TODO - 26.1: Document about the fact it can return zero as the max energy?

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
        return Math.max(0L, getCapacity() - getEnergy());
    }

    @Override
    default void serialize(ValueOutput output) {
        if (!isEmpty()) {
            output.putLong(SerializationConstants.STORED, getEnergy());
        }
    }
}
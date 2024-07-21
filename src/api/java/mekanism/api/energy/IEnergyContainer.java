package mekanism.api.energy;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public interface IEnergyContainer extends INBTSerializable<CompoundTag>, IContentsListener {

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
     * Note: This behaviour is subtly different from {@link IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction)}
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
    default long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, Action action, AutomationType automationType) {
        if (amount <= 0) {
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
    default long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount <= 0) {
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
    long getMaxEnergy();

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
        return getMaxEnergy() - getEnergy();
    }

    @Override
    default CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        if (!isEmpty()) {
            nbt.putLong(SerializationConstants.STORED, getEnergy());
        }
        return nbt;
    }
}
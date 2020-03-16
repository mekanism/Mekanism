package mekanism.api.energy;

import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.inventory.AutomationType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface IEnergyContainer extends INBTSerializable<CompoundNBT> {

    /**
     * Returns the energy in this container.
     *
     * @return Energy in this container. {@code 0} if no energy is stored.
     */
    double getEnergy();

    /**
     * Overrides the amount of energy in this {@link IEnergyContainer}.
     *
     * @param energy Energy to set this container's contents to (may be {@code 0}).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting. (Such as a negative amount of energy)
     * @implNote If the internal amount does get updated make sure to call {@link #onContentsChanged()}
     */
    void setEnergy(double energy);

    /**
     * <p>
     * Inserts energy into this {@link IEnergyContainer} and return the remainder.
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param amount         Energy to insert.
     * @param action         The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param automationType The method that this container is being interacted from.
     *
     * @return The remaining energy that was not inserted (if the entire amount is accepted, then return {@code 0}).
     *
     * @implNote Negative values for {@code amount} <strong>MUST</strong> be supported, and treated as if the passed value was actually {@code 0}. Also if the internal
     * amount does get updated make sure to call {@link #onContentsChanged()}
     */
    default double insert(double amount, Action action, AutomationType automationType) {
        if (amount <= 0) {
            //"Fail quick" if the given amount is empty (zero or negative)
            return amount;
        }
        double needed = getNeeded();
        if (needed <= 0) {
            //Fail if we are a full container
            return amount;
        }
        double toAdd = Math.min(amount, needed);
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
     * The returned value must be {@code 0} if nothing is extracted, otherwise its must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount         Amount of energy to extract (may be greater than the current stored amount or the container's capacity)
     * @param action         The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param automationType The method that this container is being interacted from.
     *
     * @return Energy extracted from the container, must be {@code 0} if no energy can be extracted.
     *
     * @implNote Negative values for {@code amount} <strong>MUST</strong> be supported, and treated as if the passed value was actually {@code 0}. Also if the internal
     * amount does get updated make sure to call {@link #onContentsChanged()}
     */
    default double extract(double amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount <= 0) {
            return 0;
        }
        double ret = Math.min(getEnergy(), amount);
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
    double getMaxEnergy();

    /**
     * Called when the contents of this container changes.
     */
    void onContentsChanged();

    /**
     * Convenience method for checking if this container is empty.
     *
     * @return True if the container is empty, false otherwise.
     */
    default boolean isEmpty() {
        return getEnergy() == 0;
    }

    /**
     * Convenience method for emptying this {@link IEnergyContainer}.
     */
    default void setEmpty() {
        setEnergy(0);
    }

    /**
     * Gets the amount of energy needed by this {@link IEnergyContainer} to reach a filled state.
     *
     * @return Amount of energy needed
     */
    default double getNeeded() {
        return Math.max(0, getMaxEnergy() - getEnergy());
    }


    @Override
    default CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (!isEmpty()) {
            nbt.putDouble(NBTConstants.STORED, getEnergy());
        }
        return nbt;
    }
}
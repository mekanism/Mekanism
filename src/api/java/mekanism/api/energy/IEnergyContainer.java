package mekanism.api.energy;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IEnergyContainer extends INBTSerializable<CompoundNBT>, IContentsListener {

    /**
     * Returns the energy in this container.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link FloatingLong} <em>MUST NOT</em> be modified. This method is not for altering internal contents. Any implementers who are
     * able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the value returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLOATING LONG</em></strong>
     * </p>
     *
     * @return Energy in this container. {@link FloatingLong#ZERO} if no energy is stored.
     */
    FloatingLong getEnergy();

    /**
     * Overrides the amount of energy in this {@link IEnergyContainer}.
     *
     * @param energy Energy to set this container's contents to (may be {@link FloatingLong#ZERO}).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting. Such as if it was not expecting this to be called at all.
     * @implNote If the internal amount does get updated make sure to call {@link #onContentsChanged()}
     */
    void setEnergy(FloatingLong energy);

    /**
     * <p>
     * Inserts energy into this {@link IEnergyContainer} and return the remainder. The {@link FloatingLong} <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param amount         Energy to insert. This must not be modified by the container.
     * @param action         The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param automationType The method that this container is being interacted from.
     *
     * @return The remaining energy that was not inserted (if the entire amount is accepted, then return {@link FloatingLong#ZERO}). The returned {@link FloatingLong} can
     * be safely modified afterwards.
     *
     * @implNote The {@link FloatingLong} <em>should not</em> be modified in this function! If the internal amount does get updated make sure to call {@link
     * #onContentsChanged()}. It is also recommended to override this if your internal {@link FloatingLong} is mutable so that a copy does not have to be made every run.
     */
    default FloatingLong insert(FloatingLong amount, Action action, AutomationType automationType) {
        if (amount.isZero()) {
            //"Fail quick" if the given amount is empty
            return amount;
        }
        FloatingLong needed = getNeeded();
        if (needed.isZero()) {
            //Fail if we are a full container
            return amount;
        }
        FloatingLong toAdd = amount.min(needed);
        if (!toAdd.isZero() && action.execute()) {
            //If we want to actually insert the energy, then update the current energy
            // Note: this also will mark that the contents changed
            setEnergy(getEnergy().add(toAdd));
        }
        return amount.subtract(toAdd);
    }

    /**
     * Extracts energy from this {@link IEnergyContainer}.
     * <p>
     * The returned value must be {@link FloatingLong#ZERO} if nothing is extracted, otherwise its must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount         Amount of energy to extract (may be greater than the current stored amount or the container's capacity) This must not be modified by the
     *                       handler.
     * @param action         The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param automationType The method that this container is being interacted from.
     *
     * @return Energy extracted from the container, must be {@link FloatingLong#ZERO} if no energy can be extracted. The returned {@link FloatingLong} can be safely
     * modified after, so the container should return a new or copied {@link FloatingLong}.
     *
     * @implNote The returned {@link FloatingLong} can be safely modified after, so a new or copied {@link FloatingLong} should be returned. If the internal amount does
     * get updated make sure to call {@link #onContentsChanged()}. It is also recommended to override this if your internal {@link FloatingLong} is mutable so that a copy
     * does not have to be made every run.
     */
    default FloatingLong extract(FloatingLong amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount.isZero()) {
            return FloatingLong.ZERO;
        }
        FloatingLong ret = getEnergy().min(amount).copy();
        if (!ret.isZero() && action.execute()) {
            // Note: this also will mark that the contents changed
            setEnergy(getEnergy().subtract(ret));
        }
        return ret;
    }

    /**
     * Retrieves the maximum amount of energy allowed to exist in this {@link IEnergyContainer}.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link FloatingLong} <em>MUST NOT</em> be modified. This method is not for altering internal max energy. Any implementers who are
     * able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the value returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLOATING LONG</em></strong>
     * </p>
     *
     * @return The maximum amount of energy allowed in this {@link IEnergyContainer}.
     */
    FloatingLong getMaxEnergy();

    /**
     * Convenience method for checking if this container is empty.
     *
     * @return True if the container is empty, false otherwise.
     */
    default boolean isEmpty() {
        return getEnergy().isZero();
    }

    /**
     * Convenience method for emptying this {@link IEnergyContainer}.
     */
    default void setEmpty() {
        setEnergy(FloatingLong.ZERO);
    }

    /**
     * Gets the amount of energy needed by this {@link IEnergyContainer} to reach a filled state.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link FloatingLong} <em>MUST NOT</em> be modified. This method is not for altering remaining needed amount. Any implementers who
     * are able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the value returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLOATING LONG</em></strong>
     * </p>
     *
     * @return Amount of energy needed
     */
    default FloatingLong getNeeded() {
        return FloatingLong.ZERO.max(getMaxEnergy().subtract(getEnergy()));
    }

    @Override
    default CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (!isEmpty()) {
            nbt.putString(NBTConstants.STORED, getEnergy().toString());
        }
        return nbt;
    }
}
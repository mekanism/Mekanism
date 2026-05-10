package mekanism.api.energy;

import com.google.common.primitives.Ints;
import mekanism.api.Action;
import mekanism.api.MekanismPreconditions;
import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

@NothingNullByDefault
public interface IStrictEnergyHandler {//TODO - 26.1: Redo the documentation for this class

    int size();

    long getAmountAsLong(int index);

    @ApiStatus.NonExtendable
    default int getAmountAsInt(int index) {
        return Ints.saturatedCast(getAmountAsLong(index));
    }

    //TODO - 26.1: Do we want to name this getLimit to be closer to the resource handlers?
    long getCapacityAsLong(int index);

    @ApiStatus.NonExtendable
    default int getCapacityAsInt(int index) {
        return Ints.saturatedCast(getCapacityAsLong(index));
    }

    /**
     * Overrides the energy stored in the given container. This method may throw an error if it is called unexpectedly.
     *
     * @param container Container to modify
     * @param energy    Energy to set the container to (may be 0).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting. Such as if it was not expecting this to be called at all.
     **/
    void setEnergy(int container, long energy);

    /**
     * Retrieves the amount of energy that is needed to fill a given container.
     *
     * @param container Container to query.
     *
     * @return The energy needed to fill the container.
     */
    default long getNeededEnergy(int container) {
        return Math.max(0L, getCapacityAsLong(container) - getAmountAsLong(container));
    }

    /**
     * <p>
     * Inserts energy into this handler, distribution is left <strong>entirely</strong> to this {@link IStrictEnergyHandler}.
     * </p>
     *
     * @param amount Energy to insert. This must not be modified by the handler.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return The remaining energy that was not inserted (if the entire amount is accepted, then return 0).
     *
     * @implNote The default implementation of this method, attempts to insert into containers that contain the energy, and if it will not all fit, falls back to
     * inserting into any empty containers.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IStrictEnergyHandler} ends up distributing the insertion.
     */
    @Deprecated(forRemoval = true)//TODO - 26.1: Remove this
    default long insertEnergy(long amount, Action action) {
        return amount;
    }

    /**
     * Extracts energy from this handler, distribution is left <strong>entirely</strong> to this {@link IStrictEnergyHandler}.
     * <p>
     * The returned value must be 0 if nothing is extracted, otherwise its must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount Amount of energy to extract (may be greater than the current stored amount or the container's capacity) This must not be modified by the handler.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return Energy extracted from the container, must be 0 if no energy can be extracted.
     *
     * @implNote The default implementation of this method, extracts across all containers to try and reach the desired amount to extract.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IStrictEnergyHandler} ends up distributing the extraction.
     */
    @Deprecated(forRemoval = true)//TODO - 26.1: Remove this
    default long extractEnergy(long amount, Action action) {
        return 0;
    }

    long insert(int index, long amount, TransactionContext transaction);

    default long insert(long amount, TransactionContext transaction) {
        MekanismPreconditions.checkNonNegative(amount);

        long inserted = 0;
        for (int index = 0, size = size(); index < size; index++) {
            inserted += insert(index, amount - inserted, transaction);
            if (inserted == amount) {
                break;
            }
        }
        return inserted;
    }

    long extract(int index, long amount, TransactionContext transaction);

    default long extract(long amount, TransactionContext transaction) {
        MekanismPreconditions.checkNonNegative(amount);
        long extracted = 0;
        for (int index = 0, size = size(); index < size; index++) {
            extracted += extract(index, amount - extracted, transaction);
            if (extracted == amount) {
                break;
            }
        }
        return extracted;
    }
}
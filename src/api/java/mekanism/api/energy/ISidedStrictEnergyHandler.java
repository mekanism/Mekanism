package mekanism.api.energy;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.LongTransferUtils;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * A sided variant of {@link IStrictEnergyHandler}
 */
@NothingNullByDefault
public interface ISidedStrictEnergyHandler extends IStrictEnergyHandler {

    /**
     * The side this {@link ISidedStrictEnergyHandler} is for. This defaults to null, which is for internal use.
     *
     * @return The default side to use for the normal {@link IStrictEnergyHandler} methods when wrapping them into {@link ISidedStrictEnergyHandler} methods.
     */
    @Nullable
    default Direction getEnergySideFor() {
        return null;
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#getEnergyContainerCount()}, docs copied for convenience.
     * <p>
     * Returns the number of energy storage units ("containers") available
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The number of containers available
     */
    int getEnergyContainerCount(@Nullable Direction side);

    @Override
    default int getEnergyContainerCount() {
        return getEnergyContainerCount(getEnergySideFor());
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#getEnergy(int)}, docs copied for convenience.
     * <p>
     * Returns the energy stored in a given container.
     *
     * @param container Container to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return Energy in a given container. 0 if the container has no energy stored.
     */
    long getEnergy(int container, @Nullable Direction side);

    @Override
    default long getEnergy(int container) {
        return getEnergy(container, getEnergySideFor());
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#setEnergy(int, long)}, docs copied for convenience.
     * <p>
     * Overrides the energy stored in the given container. This method may throw an error if it is called unexpectedly.
     *
     * @param container Container to modify
     * @param energy    Energy to set the container to (may be 0).
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting. Such as if it was not expecting this to be called at all.
     **/
    void setEnergy(int container, long energy, @Nullable Direction side);

    @Override
    default void setEnergy(int container, long energy) {
        setEnergy(container, energy, getEnergySideFor());
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#getMaxEnergy(int)}, docs copied for convenience.
     * <p>
     * Retrieves the maximum amount of energy that can be stored in a given container.
     *
     * @param container Container to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return The maximum energy that can be stored in the container.
     */
    long getMaxEnergy(int container, @Nullable Direction side);

    @Override
    default long getMaxEnergy(int container) {
        return getMaxEnergy(container, getEnergySideFor());
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#getNeededEnergy(int)}, docs copied for convenience.
     * <p>
     * Retrieves the amount of energy that is needed to fill a given container.
     *
     * @param container Container to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return The energy needed to fill the container.
     */
    long getNeededEnergy(int container, @Nullable Direction side);

    @Override
    default long getNeededEnergy(int container) {
        return getNeededEnergy(container, getEnergySideFor());
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#insertEnergy(int, long, Action)}, docs copied for convenience.
     *
     * <p>
     * Inserts energy into a given container and return the remainder.
     * </p>
     * Note: This behaviour is subtly different from {@link IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction)}
     *
     * @param container Container to insert to.
     * @param amount    Energy to insert. This must not be modified by the container.
     * @param action    The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return The remaining energy that was not inserted (if the entire amount is accepted, then return 0).
     */
    long insertEnergy(int container, long amount, @Nullable Direction side, Action action);

    @Override
    default long insertEnergy(int container, long amount, Action action) {
        return insertEnergy(container, amount, getEnergySideFor(), action);
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#extractEnergy(int, long, Action)}, docs copied for convenience.
     * <p>
     * Extracts energy from a specific container in this handler.
     * <p>
     * The returned value must be 0 if nothing is extracted, otherwise its must be less than or equal to {@code amount}.
     * </p>
     *
     * @param container Container to extract from.
     * @param amount    Amount of energy to extract (may be greater than the current stored amount or the container's capacity) This must not be modified by the handler.
     * @param action    The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return Energy extracted from the container, must be 0 if no energy can be extracted.
     */
    long extractEnergy(int container, long amount, @Nullable Direction side, Action action);

    @Override
    default long extractEnergy(int container, long amount, Action action) {
        return extractEnergy(container, amount, getEnergySideFor(), action);
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#insertEnergy(long, Action)}, docs copied for convenience.
     *
     * <p>
     * Inserts energy into this handler, distribution is left <strong>entirely</strong> to this {@link IStrictEnergyHandler}.
     * </p>
     * Note: This behaviour is subtly different from {@link IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction)}
     *
     * @param amount Energy to insert. This must not be modified by the handler.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return The remaining energy that was not inserted (if the entire amount is accepted, then return 0).
     *
     * @implNote The default implementation of this method, attempts to insert into containers that contain the energy, and if it will not all fit, falls back to
     * inserting into any empty containers.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IStrictEnergyHandler} ends up distributing the insertion.
     */
    default long insertEnergy(long amount, @Nullable Direction side, Action action) {
        return LongTransferUtils.insert(amount, side, action, this::getEnergyContainerCount, this::getEnergy, this::insertEnergy);
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#extractEnergy(long, Action)}, docs copied for convenience.
     * <p>
     * Extracts energy from this handler, distribution is left <strong>entirely</strong> to this {@link IStrictEnergyHandler}.
     * <p>
     * The returned value must be 0 if nothing is extracted, otherwise its must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount Amount of energy to extract (may be greater than the current stored amount or the container's capacity) This must not be modified by the handler.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return Energy extracted from the container, must be 0 if no energy can be extracted.
     *
     * @implNote The default implementation of this method, extracts across all containers to try and reach the desired amount to extract.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IStrictEnergyHandler} ends up distributing the extraction.
     */
    default long extractEnergy(long amount, @Nullable Direction side, Action action) {
        return LongTransferUtils.extract(amount, side, action, this::getEnergyContainerCount, this::extractEnergy);
    }
}
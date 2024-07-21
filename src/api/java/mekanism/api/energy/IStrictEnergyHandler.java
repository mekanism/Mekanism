package mekanism.api.energy;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.LongTransferUtils;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

@NothingNullByDefault
public interface IStrictEnergyHandler {

    /**
     * Returns the number of energy storage units ("containers") available
     *
     * @return The number of containers available
     */
    int getEnergyContainerCount();

    /**
     * Returns the energy stored in a given container.
     *
     * @param container Container to query.
     *
     * @return Energy in a given container. 0 if the container has no energy stored.
     */
    long getEnergy(int container);

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
     * Retrieves the maximum amount of energy that can be stored in a given container.
     *
     * @param container Container to query.
     *
     * @return The maximum energy that can be stored in the container.
     */
    long getMaxEnergy(int container);

    /**
     * Retrieves the amount of energy that is needed to fill a given container.
     *
     * @param container Container to query.
     *
     * @return The energy needed to fill the container.
     */
    long getNeededEnergy(int container);

    /**
     * <p>
     * Inserts energy into a given container and return the remainder.
     * </p>
     * Note: This behaviour is subtly different from {@link IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction)}
     *
     * @param container Container to insert to.
     * @param amount    Energy to insert. This must not be modified by the container.
     * @param action    The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return The remaining energy that was not inserted (if the entire amount is accepted, then return 0).
     */
    long insertEnergy(int container, long amount, Action action);

    /**
     * Extracts energy from a specific container in this handler.
     * <p>
     * The returned value must be 0 if nothing is extracted, otherwise its must be less than or equal to {@code amount}.
     * </p>
     *
     * @param container Container to extract from.
     * @param amount    Amount of energy to extract (may be greater than the current stored amount or the container's capacity) This must not be modified by the handler.
     * @param action    The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return Energy extracted from the container, must be 0 if no energy can be extracted.
     */
    long extractEnergy(int container, long amount, Action action);

    /**
     * <p>
     * Inserts energy into this handler, distribution is left <strong>entirely</strong> to this {@link IStrictEnergyHandler}.
     * </p>
     * Note: This behaviour is subtly different from {@link IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction)}
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
    default long insertEnergy(long amount, Action action) {
        return LongTransferUtils.insert(amount, null, action, side -> getEnergyContainerCount(), (container, side) -> getEnergy(container),
              (container, amt, side, act) -> insertEnergy(container, amt, act));
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
    default long extractEnergy(long amount, Action action) {
        return LongTransferUtils.extract(amount, null, action, side -> getEnergyContainerCount(), (container, amt, side, act) -> extractEnergy(container, amt, act));
    }
}
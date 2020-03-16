package mekanism.api.energy;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import net.minecraft.util.Direction;

/**
 * A sided variant of {@link IStrictEnergyHandler}
 */
@ParametersAreNonnullByDefault
public interface ISidedStrictEnergyHandler extends IStrictEnergyHandler {

    /**
     * The side this {@link ISidedStrictEnergyHandler} is for. This defaults to null, which is for internal use.
     *
     * @return The default side to use for the normal {@link IStrictEnergyHandler} methods when wrapping them into {@link ISidedStrictEnergyHandler} methods.
     */
    @Nullable
    default Direction getEnergySideFor() {
        //TODO: Decide if having this method even makes sense or would it be better to just inline null for the built in IStrictEnergyHandler
        // methods, given we just handle sides via the ProxyEnergyHandler anyways, in which we use our extended methods.
        return null;
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#getEnergyContainerCount()}, docs copied for convenience.
     *
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
     *
     * Returns the energy stored in a given container.
     *
     * @param container Container to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return Energy in a given container. {@code 0} if the container has no energy stored.
     */
    double getEnergy(int container, @Nullable Direction side);

    @Override
    default double getEnergy(int container) {
        return getEnergy(container, getEnergySideFor());
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#setEnergy(int, double)}, docs copied for convenience.
     *
     * Overrides the energy stored in the given container. This method may throw an error if it is called unexpectedly.
     *
     * @param container Container to modify
     * @param energy    Energy to set the container to (may be {@code 0}).
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting. (Such as a negative amount of energy)
     **/
    void setEnergy(int container, double energy, @Nullable Direction side);

    @Override
    default void setEnergy(int container, double energy) {
        setEnergy(container, energy, getEnergySideFor());
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#getMaxEnergy(int)}, docs copied for convenience.
     *
     * Retrieves the maximum amount of energy that can be stored in a given container.
     *
     * @param container Container to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return The maximum energy that can be stored in the container.
     */
    double getMaxEnergy(int container, @Nullable Direction side);

    @Override
    default double getMaxEnergy(int container) {
        return getMaxEnergy(container, getEnergySideFor());
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#getNeededEnergy(int)}, docs copied for convenience.
     *
     * Retrieves the amount of energy that is needed to fill a given container.
     *
     * @param container Container to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return The energy needed to fill the container.
     */
    double getNeededEnergy(int container, @Nullable Direction side);

    @Override
    default double getNeededEnergy(int container) {
        return getNeededEnergy(container, getEnergySideFor());
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#insertEnergy(int, double, Action)}, docs copied for convenience.
     *
     * <p>
     * Inserts energy into a given container and return the remainder.
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param container Container to insert to.
     * @param amount    Energy to insert
     * @param action    The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return The remaining energy that was not inserted (if the entire amount is accepted, then return {@code 0}).
     *
     * @implNote Negative values for {@code amount} <strong>MUST</strong> be supported, and treated as if the passed value was actually {@code 0}.
     */
    double insertEnergy(int container, double amount, @Nullable Direction side, Action action);

    @Override
    default double insertEnergy(int container, double amount, Action action) {
        return insertEnergy(container, amount, getEnergySideFor(), action);
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#extractEnergy(int, double, Action)}, docs copied for convenience.
     *
     * Extracts energy from a specific container in this handler.
     * <p>
     * The returned value must be {@code 0} if nothing is extracted, otherwise its must be less than or equal to {@code amount}.
     * </p>
     *
     * @param container Container to extract from.
     * @param amount    Amount of energy to extract (may be greater than the current stored amount or the container's capacity)
     * @param action    The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return Energy extracted from the container, must be {@code 0} if no energy can be extracted.
     *
     * @implNote Negative values for {@code amount} <strong>MUST</strong> be supported, and treated as if the passed value was actually {@code 0}.
     */
    double extractEnergy(int container, double amount, @Nullable Direction side, Action action);

    @Override
    default double extractEnergy(int container, double amount, Action action) {
        return extractEnergy(container, amount, getEnergySideFor(), action);
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#insertEnergy(double, Action)}, docs copied for convenience.
     *
     * <p>
     * Inserts energy into this handler, distribution is left <strong>entirely</strong> to this {@link IStrictEnergyHandler}.
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param amount Energy to insert.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return The remaining energy that was not inserted (if the entire amount is accepted, then return {@code 0}).
     *
     * @implNote The default implementation of this method, attempts to insert into containers that contain the energy, and if it will not all fit, falls back to
     * inserting into any empty containers.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IStrictEnergyHandler} ends up distributing the insertion. Additionally
     * negative values for {@code amount} <strong>MUST</strong> be supported, and treated as if the passed value was actually {@code 0}.
     */
    default double insertEnergy(double amount, @Nullable Direction side, Action action) {
        return EnergyTransferUtils.insert(amount, action, () -> getEnergyContainerCount(side), container -> getEnergy(container, side),
              (container, a, act) -> insertEnergy(container, a, side, act));
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#extractEnergy(double, Action)}, docs copied for convenience.
     *
     * Extracts energy from this handler, distribution is left <strong>entirely</strong> to this {@link IStrictEnergyHandler}.
     * <p>
     * The returned value must be {@code 0} if nothing is extracted, otherwise its must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount Amount of energy to extract (may be greater than the current stored amount or the container's capacity)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return Energy extracted from the container, must be {@code 0} if no energy can be extracted.
     *
     * @implNote The default implementation of this method, extracts across all containers to try and reach the desired amount to extract.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IStrictEnergyHandler} ends up distributing the extraction. Additionally
     * negative values for {@code amount} <strong>MUST</strong> be supported, and treated as if the passed value was actually {@code 0}.
     */
    default double extractEnergy(double amount, @Nullable Direction side, Action action) {
        return EnergyTransferUtils.extract(amount, action, () -> getEnergyContainerCount(side), (container, a, act) -> extractEnergy(container, a, side, act));
    }
}
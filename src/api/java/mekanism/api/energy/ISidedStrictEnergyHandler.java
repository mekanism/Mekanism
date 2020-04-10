package mekanism.api.energy;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongTransferUtils;
import net.minecraft.util.Direction;

/**
 * A sided variant of {@link IStrictEnergyHandler}
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
     * <p>
     * <strong>IMPORTANT:</strong> This {@link FloatingLong} <em>MUST NOT</em> be modified. This method is not for altering internal contents. Any implementers who are
     * able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the value returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLOATING LONG</em></strong>
     * </p>
     *
     * @param container Container to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return Energy in a given container. {@link FloatingLong#ZERO} if the container has no energy stored.
     */
    FloatingLong getEnergy(int container, @Nullable Direction side);

    @Override
    default FloatingLong getEnergy(int container) {
        return getEnergy(container, getEnergySideFor());
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#setEnergy(int, FloatingLong)}, docs copied for convenience.
     *
     * Overrides the energy stored in the given container. This method may throw an error if it is called unexpectedly.
     *
     * @param container Container to modify
     * @param energy    Energy to set the container to (may be {@link FloatingLong#ZERO}).
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting. Such as if it was not expecting this to be called at all.
     **/
    void setEnergy(int container, FloatingLong energy, @Nullable Direction side);

    @Override
    default void setEnergy(int container, FloatingLong energy) {
        setEnergy(container, energy, getEnergySideFor());
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#getMaxEnergy(int)}, docs copied for convenience.
     *
     * Retrieves the maximum amount of energy that can be stored in a given container.
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
     * @param container Container to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return The maximum energy that can be stored in the container.
     */
    FloatingLong getMaxEnergy(int container, @Nullable Direction side);

    @Override
    default FloatingLong getMaxEnergy(int container) {
        return getMaxEnergy(container, getEnergySideFor());
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#getNeededEnergy(int)}, docs copied for convenience.
     *
     * Retrieves the amount of energy that is needed to fill a given container.
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
     * @param container Container to query.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return The energy needed to fill the container.
     */
    FloatingLong getNeededEnergy(int container, @Nullable Direction side);

    @Override
    default FloatingLong getNeededEnergy(int container) {
        return getNeededEnergy(container, getEnergySideFor());
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#insertEnergy(int, FloatingLong, Action)}, docs copied for convenience.
     *
     * <p>
     * Inserts energy into a given container and return the remainder. The {@link FloatingLong} <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param container Container to insert to.
     * @param amount    Energy to insert. This must not be modified by the container.
     * @param action    The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return The remaining energy that was not inserted (if the entire amount is accepted, then return {@link FloatingLong#ZERO}). The returned {@link FloatingLong} can
     * be safely modified afterwards.
     */
    FloatingLong insertEnergy(int container, FloatingLong amount, @Nullable Direction side, Action action);

    @Override
    default FloatingLong insertEnergy(int container, FloatingLong amount, Action action) {
        return insertEnergy(container, amount, getEnergySideFor(), action);
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#extractEnergy(int, FloatingLong, Action)}, docs copied for convenience.
     *
     * Extracts energy from a specific container in this handler.
     * <p>
     * The returned value must be {@link FloatingLong#ZERO} if nothing is extracted, otherwise its must be less than or equal to {@code amount}.
     * </p>
     *
     * @param container Container to extract from.
     * @param amount    Amount of energy to extract (may be greater than the current stored amount or the container's capacity) This must not be modified by the handler.
     * @param action    The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return Energy extracted from the container, must be {@link FloatingLong#ZERO} if no energy can be extracted. The returned {@link FloatingLong} can be safely
     * modified after, so the container should return a new or copied {@link FloatingLong}.
     */
    FloatingLong extractEnergy(int container, FloatingLong amount, @Nullable Direction side, Action action);

    @Override
    default FloatingLong extractEnergy(int container, FloatingLong amount, Action action) {
        return extractEnergy(container, amount, getEnergySideFor(), action);
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#insertEnergy(FloatingLong, Action)}, docs copied for convenience.
     *
     * <p>
     * Inserts energy into this handler, distribution is left <strong>entirely</strong> to this {@link IStrictEnergyHandler}. The {@link FloatingLong} <em>should not</em>
     * be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param amount Energy to insert. This must not be modified by the handler.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return The remaining energy that was not inserted (if the entire amount is accepted, then return {@link FloatingLong#ZERO}). The returned {@link FloatingLong} can
     * be safely modified after.
     *
     * @implNote The default implementation of this method, attempts to insert into containers that contain the energy, and if it will not all fit, falls back to
     * inserting into any empty containers.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IStrictEnergyHandler} ends up distributing the insertion.
     */
    default FloatingLong insertEnergy(FloatingLong amount, @Nullable Direction side, Action action) {
        return FloatingLongTransferUtils.insert(amount, action, () -> getEnergyContainerCount(side), container -> getEnergy(container, side),
              (container, a, act) -> insertEnergy(container, a, side, act));
    }

    /**
     * A sided variant of {@link IStrictEnergyHandler#extractEnergy(FloatingLong, Action)}, docs copied for convenience.
     *
     * Extracts energy from this handler, distribution is left <strong>entirely</strong> to this {@link IStrictEnergyHandler}.
     * <p>
     * The returned value must be {@link FloatingLong#ZERO} if nothing is extracted, otherwise its must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount Amount of energy to extract (may be greater than the current stored amount or the container's capacity) This must not be modified by the handler.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return Energy extracted from the container, must be {@link FloatingLong#ZERO} if no energy can be extracted. The returned {@link FloatingLong} can be safely
     * modified after, so the container should return a new or copied {@link FloatingLong}.
     *
     * @implNote The default implementation of this method, extracts across all containers to try and reach the desired amount to extract.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IStrictEnergyHandler} ends up distributing the extraction.
     */
    default FloatingLong extractEnergy(FloatingLong amount, @Nullable Direction side, Action action) {
        return FloatingLongTransferUtils.extract(amount, action, () -> getEnergyContainerCount(side), (container, a, act) -> extractEnergy(container, a, side, act));
    }
}
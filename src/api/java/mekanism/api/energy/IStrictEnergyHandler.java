package mekanism.api.energy;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.math.FloatingLongTransferUtils;
import mekanism.api.math.FloatingLong;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
     * @return Energy in a given container. {@code 0} if the container has no energy stored.
     */
    FloatingLong getEnergy(int container);

    /**
     * Overrides the energy stored in the given container. This method may throw an error if it is called unexpectedly.
     *
     * @param container Container to modify
     * @param energy    Energy to set the container to (may be {@code 0}).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting. (Such as a negative amount of energy)
     **/
    void setEnergy(int container, FloatingLong energy);

    /**
     * Retrieves the maximum amount of energy that can be stored in a given container.
     *
     * @param container Container to query.
     *
     * @return The maximum energy that can be stored in the container.
     */
    FloatingLong getMaxEnergy(int container);

    /**
     * Retrieves the amount of energy that is needed to fill a given container.
     *
     * @param container Container to query.
     *
     * @return The energy needed to fill the container.
     */
    FloatingLong getNeededEnergy(int container);

    /**
     * <p>
     * Inserts energy into a given container and return the remainder.
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param container Container to insert to.
     * @param amount    Energy to insert
     * @param action    The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return The remaining energy that was not inserted (if the entire amount is accepted, then return {@code 0}).
     *
     * @implNote Negative values for {@code amount} <strong>MUST</strong> be supported, and treated as if the passed value was actually {@code 0}.
     */
    FloatingLong insertEnergy(int container, FloatingLong amount, Action action);

    /**
     * Extracts energy from a specific container in this handler.
     * <p>
     * The returned value must be {@code 0} if nothing is extracted, otherwise its must be less than or equal to {@code amount}.
     * </p>
     *
     * @param container Container to extract from.
     * @param amount    Amount of energy to extract (may be greater than the current stored amount or the container's capacity)
     * @param action    The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return Energy extracted from the container, must be {@code 0} if no energy can be extracted.
     *
     * @implNote Negative values for {@code amount} <strong>MUST</strong> be supported, and treated as if the passed value was actually {@code 0}.
     */
    FloatingLong extractEnergy(int container, FloatingLong amount, Action action);

    /**
     * <p>
     * Inserts energy into this handler, distribution is left <strong>entirely</strong> to this {@link IStrictEnergyHandler}.
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param amount Energy to insert.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return The remaining energy that was not inserted (if the entire amount is accepted, then return {@code 0}).
     *
     * @implNote The default implementation of this method, attempts to insert into containers that contain the energy, and if it will not all fit, falls back to
     * inserting into any empty containers.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IStrictEnergyHandler} ends up distributing the insertion. Additionally
     * negative values for {@code amount} <strong>MUST</strong> be supported, and treated as if the passed value was actually {@code 0}.
     */
    default FloatingLong insertEnergy(FloatingLong amount, Action action) {
        return FloatingLongTransferUtils.insert(amount, action, this::getEnergyContainerCount, this::getEnergy, this::insertEnergy);
    }

    /**
     * Extracts energy from this handler, distribution is left <strong>entirely</strong> to this {@link IStrictEnergyHandler}.
     * <p>
     * The returned value must be {@code 0} if nothing is extracted, otherwise its must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount Amount of energy to extract (may be greater than the current stored amount or the container's capacity)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return Energy extracted from the container, must be {@code 0} if no energy can be extracted.
     *
     * @implNote The default implementation of this method, extracts across all containers to try and reach the desired amount to extract.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IStrictEnergyHandler} ends up distributing the extraction. Additionally
     * negative values for {@code amount} <strong>MUST</strong> be supported, and treated as if the passed value was actually {@code 0}.
     */
    default FloatingLong extractEnergy(FloatingLong amount, Action action) {
        return FloatingLongTransferUtils.extract(amount, action, this::getEnergyContainerCount, this::extractEnergy);
    }
}
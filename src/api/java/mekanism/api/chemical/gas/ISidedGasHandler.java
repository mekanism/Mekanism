package mekanism.api.chemical.gas;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalUtils;
import net.minecraft.util.Direction;

/**
 * A sided variant of {@link IGasHandler}
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ISidedGasHandler extends IGasHandler {

    /**
     * The side this {@link ISidedGasHandler} is for. This defaults to null, which is for internal use.
     *
     * @return The default side to use for the normal {@link IGasHandler} methods when wrapping them into {@link ISidedGasHandler} methods.
     */
    @Nullable
    default Direction getGasSideFor() {
        //TODO: Decide if having this method even makes sense or would it be better to just inline null for the built in IGasHandler
        // methods, given we just handle sides via the ProxyGasHandler anyways, in which we use our extended methods.
        return null;
    }

    /**
     * A sided variant of {@link IGasHandler#getGasTankCount()}, docs copied for convenience.
     *
     * Returns the number of gas storage units ("tanks") available
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The number of tanks available
     */
    int getGasTankCount(@Nullable Direction side);

    @Override
    default int getGasTankCount() {
        return getGasTankCount(getGasSideFor());
    }

    /**
     * A sided variant of {@link IGasHandler#getGasInTank(int)}, docs copied for convenience.
     *
     * Returns the {@link GasStack} in a given tank.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link GasStack} <em>MUST NOT</em> be modified. This method is not for altering internal contents. Any implementers who are able
     * to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the stack returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED GAS STACK</em></strong>
     * </p>
     *
     * @param tank Tank to query.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return {@link GasStack} in a given tank. {@link GasStack#EMPTY} if the tank is empty.
     */
    GasStack getGasInTank(int tank, @Nullable Direction side);

    @Override
    default GasStack getGasInTank(int tank) {
        return getGasInTank(tank, getGasSideFor());
    }

    /**
     * A sided variant of {@link IGasHandler#setGasInTank(int, GasStack)}, docs copied for convenience.
     *
     * Overrides the stack in the given tank. This method may throw an error if it is called unexpectedly.
     *
     * @param tank  Tank to modify
     * @param stack {@link GasStack} to set tank to (may be empty).
     * @param side  The side we are interacting with the handler from (null for internal).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting.
     **/
    void setGasInTank(int tank, GasStack stack, @Nullable Direction side);

    @Override
    default void setGasInTank(int tank, GasStack stack) {
        setGasInTank(tank, stack, getGasSideFor());
    }

    /**
     * A sided variant of {@link IGasHandler#getGasTankCapacity(int)}, docs copied for convenience.
     *
     * Retrieves the maximum amount of gas that can be stored in a given tank.
     *
     * @param tank Tank to query.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The maximum gas amount held by the tank.
     */
    int getGasTankCapacity(int tank, @Nullable Direction side);

    @Override
    default int getGasTankCapacity(int tank) {
        return getGasTankCapacity(tank, getGasSideFor());
    }

    /**
     * A sided variant of {@link IGasHandler#isGasValid(int, GasStack)}, docs copied for convenience.
     *
     * <p>
     * This function should be used instead of simulated insertions in cases where the contents and state of the tank are irrelevant, mainly for the purpose of automation
     * and logic.
     * </p>
     * <ul>
     * <li>isGasValid is false when insertion of the gas is never valid.</li>
     * <li>When isGasValid is true, no assumptions can be made and insertion must be simulated case-by-case.</li>
     * <li>The actual gas in the tank, its fullness, or any other state are <strong>not</strong> considered by isGasValid.</li>
     * </ul>
     *
     * @param tank  Tank to query.
     * @param stack Stack to test with for validity @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return true if the tank can accept the {@link GasStack}, not considering the current state of the tank. false if the tank can never support the given {@link
     * GasStack} in any situation.
     */
    boolean isGasValid(int tank, GasStack stack, @Nullable Direction side);

    @Override
    default boolean isGasValid(int tank, GasStack stack) {
        return isGasValid(tank, stack, getGasSideFor());
    }

    /**
     * A sided variant of {@link IGasHandler#insertGas(int, GasStack, Action)}, docs copied for convenience.
     *
     * <p>
     * Inserts a {@link GasStack} into a given tank and return the remainder. The {@link GasStack} <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param tank   Tank to insert to.
     * @param stack  {@link GasStack} to insert. This must not be modified by the tank.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return The remaining {@link GasStack} that was not inserted (if the entire stack is accepted, then return an empty {@link GasStack}). May be the same as the input
     * {@link GasStack} if unchanged, otherwise a new {@link GasStack}. The returned {@link GasStack} can be safely modified after
     */
    GasStack insertGas(int tank, GasStack stack, @Nullable Direction side, Action action);

    @Override
    default GasStack insertGas(int tank, GasStack stack, Action action) {
        return insertGas(tank, stack, getGasSideFor(), action);
    }

    /**
     * A sided variant of {@link IGasHandler#extractGas(int, int, Action)}, docs copied for convenience.
     *
     * Extracts a {@link GasStack} from a specific tank in this handler.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param tank   Tank to extract from.
     * @param amount Amount to extract (may be greater than the current stack's amount or the tank's capacity)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return {@link GasStack} extracted from the tank, must be empty if nothing can be extracted. The returned {@link GasStack} can be safely modified after, so the
     * tank should return a new or copied stack.
     */
    GasStack extractGas(int tank, int amount, @Nullable Direction side, Action action);

    @Override
    default GasStack extractGas(int tank, int amount, Action action) {
        return extractGas(tank, amount, getGasSideFor(), action);
    }

    /**
     * A sided variant of {@link IGasHandler#insertGas(GasStack, Action)}, docs copied for convenience.
     *
     * <p>
     * Inserts a {@link GasStack} into this handler, distribution is left <strong>entirely</strong> to this {@link IGasHandler}. The {@link GasStack} <em>should not</em>
     * be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param stack  {@link GasStack} to insert. This must not be modified by the handler.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return The remaining {@link GasStack} that was not inserted (if the entire stack is accepted, then return an empty {@link GasStack}). May be the same as the input
     * {@link GasStack} if unchanged, otherwise a new {@link GasStack}. The returned {@link GasStack} can be safely modified after
     *
     * @implNote The default implementation of this method, attempts to insert into tanks that contain the same type of gas as the supplied type, and if it will not all
     * fit, falls back to inserting into any empty tanks.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IGasHandler} ends up distributing the insertion.
     */
    default GasStack insertGas(GasStack stack, @Nullable Direction side, Action action) {
        return ChemicalUtils.insert(stack, action, GasStack.EMPTY, () -> getGasTankCount(side), tank -> getGasInTank(tank, side), (tank, s, a) -> insertGas(tank, s, side, a));
    }

    /**
     * A sided variant of {@link IGasHandler#extractGas(int, Action)}, docs copied for convenience.
     *
     * Extracts a {@link GasStack} from this handler, distribution is left <strong>entirely</strong> to this {@link IGasHandler}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount Amount to extract (may be greater than the current stack's amount or the tank's capacity)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return {@link GasStack} extracted from the tank, must be empty if nothing can be extracted. The returned {@link GasStack} can be safely modified after, so the
     * tank should return a new or copied stack.
     *
     * @implNote The default implementation of this method, extracts across all tanks to try and reach the desired amount to extract. Once the first gas that can be
     * extracted is found, all future extractions will make sure to also make sure they are for the same type of gas.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IGasHandler} ends up distributing the extraction.
     */
    default GasStack extractGas(int amount, @Nullable Direction side, Action action) {
        return ChemicalUtils.extract(amount, action, GasStack.EMPTY, () -> getGasTankCount(side), tank -> getGasInTank(tank, side), (tank, a, act) -> extractGas(tank, a, side, act));
    }

    /**
     * A sided variant of {@link IGasHandler#extractGas(GasStack, Action)}, docs copied for convenience.
     *
     * Extracts a {@link GasStack} from this handler, distribution is left <strong>entirely</strong> to this {@link IGasHandler}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param stack  {@link GasStack} representing the {@link Gas} and maximum amount to be drained.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return {@link GasStack} extracted from the tank, must be empty if nothing can be extracted. The returned {@link GasStack} can be safely modified after, so the
     * tank should return a new or copied stack.
     *
     * @implNote The default implementation of this method, extracts across all tanks that contents match the type of gas passed into this method.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IGasHandler} ends up distributing the extraction.
     */
    default GasStack extractGas(GasStack stack, @Nullable Direction side, Action action) {
        return ChemicalUtils.extract(stack, action, GasStack.EMPTY, () -> getGasTankCount(side), tank -> getGasInTank(tank, side), (tank, a, act) -> extractGas(tank, a, side, act));
    }
}
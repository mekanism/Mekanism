package mekanism.api.chemical;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import net.minecraft.util.Direction;

/**
 * A sided variant of {@link IChemicalHandler}
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ISidedChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends IChemicalHandler<CHEMICAL, STACK> {

    /**
     * The side this {@link ISidedChemicalHandler} is for. This defaults to null, which is for internal use.
     *
     * @return The default side to use for the normal {@link IChemicalHandler} methods when wrapping them into {@link ISidedChemicalHandler} methods.
     */
    @Nullable
    default Direction getSideFor() {
        return null;
    }

    /**
     * A sided variant of {@link IChemicalHandler#getTanks()}, docs copied for convenience.
     *
     * Returns the number of chemical storage units ("tanks") available
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The number of tanks available
     */
    int getTanks(@Nullable Direction side);

    @Override
    default int getTanks() {
        return getTanks(getSideFor());
    }

    /**
     * A sided variant of {@link IChemicalHandler#getChemicalInTank(int)}, docs copied for convenience.
     *
     * Returns the {@link STACK} in a given tank.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link STACK} <em>MUST NOT</em> be modified. This method is not for altering internal contents. Any implementers who are
     * able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the stack returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED CHEMICAL STACK</em></strong>
     * </p>
     *
     * @param tank Tank to query.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return {@link STACK} in a given tank. {@link #getEmptyStack()} if the tank is empty.
     */
    STACK getChemicalInTank(int tank, @Nullable Direction side);

    @Override
    default STACK getChemicalInTank(int tank) {
        return getChemicalInTank(tank, getSideFor());
    }

    /**
     * A sided variant of {@link IChemicalHandler#setChemicalInTank(int, STACK)}, docs copied for convenience.
     *
     * Overrides the stack in the given tank. This method may throw an error if it is called unexpectedly.
     *
     * @param tank  Tank to modify
     * @param stack {@link STACK} to set tank to (may be empty).
     * @param side  The side we are interacting with the handler from (null for internal).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting.
     **/
    void setChemicalInTank(int tank, STACK stack, @Nullable Direction side);

    @Override
    default void setChemicalInTank(int tank, STACK stack) {
        setChemicalInTank(tank, stack, getSideFor());
    }

    /**
     * A sided variant of {@link IChemicalHandler#getTankCapacity(int)}, docs copied for convenience.
     *
     * Retrieves the maximum amount of chemical that can be stored in a given tank.
     *
     * @param tank Tank to query.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The maximum chemical amount held by the tank.
     */
    long getTankCapacity(int tank, @Nullable Direction side);

    @Override
    default long getTankCapacity(int tank) {
        return getTankCapacity(tank, getSideFor());
    }

    /**
     * A sided variant of {@link IChemicalHandler#isValid(int, STACK)}, docs copied for convenience.
     *
     * <p>
     * This function should be used instead of simulated insertions in cases where the contents and state of the tank are irrelevant, mainly for the purpose of automation
     * and logic.
     * </p>
     * <ul>
     * <li>isValid is false when insertion of the chemical type is never valid.</li>
     * <li>When isValid is true, no assumptions can be made and insertion must be simulated case-by-case.</li>
     * <li>The actual chemical in the tank, its fullness, or any other state are <strong>not</strong> considered by isValid.</li>
     * </ul>
     *
     * @param tank  Tank to query.
     * @param stack Stack to test with for validity @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return true if the tank can accept the {@link STACK}, not considering the current state of the tank. false if the tank can never support the given {@link STACK}
     * in any situation.
     */
    boolean isValid(int tank, STACK stack, @Nullable Direction side);

    @Override
    default boolean isValid(int tank, STACK stack) {
        return isValid(tank, stack, getSideFor());
    }

    /**
     * A sided variant of {@link IChemicalHandler#insertChemical(int, STACK, Action)}, docs copied for convenience.
     *
     * <p>
     * Inserts a {@link STACK} into a given tank and return the remainder. The {@link STACK} <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param tank   Tank to insert to.
     * @param stack  {@link STACK} to insert. This must not be modified by the tank.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return The remaining {@link STACK} that was not inserted (if the entire stack is accepted, then return an empty {@link STACK}). May be the same as the input
     * {@link STACK} if unchanged, otherwise a new {@link STACK}. The returned {@link STACK} can be safely modified after
     */
    STACK insertChemical(int tank, STACK stack, @Nullable Direction side, Action action);

    @Override
    default STACK insertChemical(int tank, STACK stack, Action action) {
        return insertChemical(tank, stack, getSideFor(), action);
    }

    /**
     * A sided variant of {@link IChemicalHandler#extractChemical(int, long, Action)}, docs copied for convenience.
     *
     * Extracts a {@link STACK} from a specific tank in this handler.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param tank   Tank to extract from.
     * @param amount Amount to extract (may be greater than the current stack's amount or the tank's capacity)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return {@link STACK} extracted from the tank, must be empty if nothing can be extracted. The returned {@link STACK} can be safely modified after, so the tank
     * should return a new or copied stack.
     */
    STACK extractChemical(int tank, long amount, @Nullable Direction side, Action action);

    @Override
    default STACK extractChemical(int tank, long amount, Action action) {
        return extractChemical(tank, amount, getSideFor(), action);
    }

    /**
     * A sided variant of {@link IChemicalHandler#insertChemical(STACK, Action)}, docs copied for convenience.
     *
     * <p>
     * Inserts a {@link STACK} into this handler, distribution is left <strong>entirely</strong> to this {@link IChemicalHandler}. The {@link STACK}
     * <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param stack  {@link STACK} to insert. This must not be modified by the handler.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return The remaining {@link STACK} that was not inserted (if the entire stack is accepted, then return an empty {@link STACK}). May be the same as the input
     * {@link STACK} if unchanged, otherwise a new {@link STACK}. The returned {@link STACK} can be safely modified after
     *
     * @implNote The default implementation of this method, attempts to insert into tanks that contain the same type of chemical as the supplied type, and if it will not
     * all fit, falls back to inserting into any empty tanks.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IChemicalHandler} ends up distributing the insertion.
     */
    default STACK insertChemical(STACK stack, @Nullable Direction side, Action action) {
        return ChemicalUtils.insert(stack, action, getEmptyStack(), () -> getTanks(side), tank -> getChemicalInTank(tank, side),
              (tank, s, a) -> insertChemical(tank, s, side, a));
    }

    /**
     * A sided variant of {@link IChemicalHandler#extractChemical(long, Action)}, docs copied for convenience.
     *
     * Extracts a {@link STACK} from this handler, distribution is left <strong>entirely</strong> to this {@link IChemicalHandler}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount Amount to extract (may be greater than the current stack's amount or the tank's capacity)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return {@link STACK} extracted from the tank, must be empty if nothing can be extracted. The returned {@link STACK} can be safely modified after, so the tank
     * should return a new or copied stack.
     *
     * @implNote The default implementation of this method, extracts across all tanks to try and reach the desired amount to extract. Once the first chemical that can be
     * extracted is found, all future extractions will make sure to also make sure they are for the same type of chemical.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IChemicalHandler} ends up distributing the extraction.
     */
    default STACK extractChemical(long amount, @Nullable Direction side, Action action) {
        return ChemicalUtils.extract(amount, action, getEmptyStack(), () -> getTanks(side), tank -> getChemicalInTank(tank, side),
              (tank, a, act) -> extractChemical(tank, a, side, act));
    }

    /**
     * A sided variant of {@link IChemicalHandler#extractChemical(STACK, Action)}, docs copied for convenience.
     *
     * Extracts a {@link STACK} from this handler, distribution is left <strong>entirely</strong> to this {@link IChemicalHandler}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param stack  {@link STACK} representing the {@link CHEMICAL} and maximum amount to be drained.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return {@link STACK} extracted from the tank, must be empty if nothing can be extracted. The returned {@link STACK} can be safely modified after, so the tank
     * should return a new or copied stack.
     *
     * @implNote The default implementation of this method, extracts across all tanks that contents match the type of chemical passed into this method.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IChemicalHandler} ends up distributing the extraction.
     */
    default STACK extractChemical(STACK stack, @Nullable Direction side, Action action) {
        return ChemicalUtils.extract(stack, action, getEmptyStack(), () -> getTanks(side), tank -> getChemicalInTank(tank, side),
              (tank, a, act) -> extractChemical(tank, a, side, act));
    }
}
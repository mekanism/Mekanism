package mekanism.api.chemical;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * A sided variant of {@link IChemicalHandler}
 */
@NothingNullByDefault
public interface ISidedChemicalHandler extends IChemicalHandler {

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
     * A sided variant of {@link IChemicalHandler#getChemicalTanks()}, docs copied for convenience.
     * <p>
     * Returns the number of chemical storage units ("tanks") available
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The number of tanks available
     */
    int getCountChemicalTanks(@Nullable Direction side);

    @Override
    default int getChemicalTanks() {
        return getCountChemicalTanks(getSideFor());
    }

    /**
     * A sided variant of {@link IChemicalHandler#getChemicalInTank(int)}, docs copied for convenience.
     * <p>
     * Returns the ChemicalStack in a given tank.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This ChemicalStack <em>MUST NOT</em> be modified. This method is not for altering internal contents. Any implementers who are
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
     * @return ChemicalStack in a given tank. {@link ChemicalStack#EMPTY} if the tank is empty.
     */
    ChemicalStack getChemicalInTank(int tank, @Nullable Direction side);

    @Override
    default ChemicalStack getChemicalInTank(int tank) {
        return getChemicalInTank(tank, getSideFor());
    }

    /**
     * A sided variant of {@link IChemicalHandler#setChemicalInTank(int, ChemicalStack)}, docs copied for convenience.
     * <p>
     * Overrides the stack in the given tank. This method may throw an error if it is called unexpectedly.
     *
     * @param tank  Tank to modify
     * @param stack ChemicalStack to set tank to (may be empty).
     * @param side  The side we are interacting with the handler from (null for internal).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting.
     **/
    void setChemicalInTank(int tank, ChemicalStack stack, @Nullable Direction side);

    @Override
    default void setChemicalInTank(int tank, ChemicalStack stack) {
        setChemicalInTank(tank, stack, getSideFor());
    }

    /**
     * A sided variant of {@link IChemicalHandler#getChemicalTankCapacity(int)}, docs copied for convenience.
     * <p>
     * Retrieves the maximum amount of chemical that can be stored in a given tank.
     *
     * @param tank Tank to query.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The maximum chemical amount held by the tank.
     */
    long getChemicalTankCapacity(int tank, @Nullable Direction side);

    @Override
    default long getChemicalTankCapacity(int tank) {
        return getChemicalTankCapacity(tank, getSideFor());
    }

    /**
     * A sided variant of {@link IChemicalHandler#isValid(int, ChemicalStack)}, docs copied for convenience.
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
     * @return true if the tank can accept the ChemicalStack, not considering the current state of the tank. false if the tank can never support the given ChemicalStack
     * in any situation.
     */
    boolean isValid(int tank, ChemicalStack stack, @Nullable Direction side);

    @Override
    default boolean isValid(int tank, ChemicalStack stack) {
        return isValid(tank, stack, getSideFor());
    }

    /**
     * A sided variant of {@link IChemicalHandler#insertChemical(int, ChemicalStack, Action)}, docs copied for convenience.
     *
     * <p>
     * Inserts a ChemicalStack into a given tank and return the remainder. The ChemicalStack <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction)}
     *
     * @param tank   Tank to insert to.
     * @param stack  ChemicalStack to insert. This must not be modified by the tank.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return The remaining ChemicalStack that was not inserted (if the entire stack is accepted, then return an empty ChemicalStack). May be the same as the input
     * ChemicalStack if unchanged, otherwise a new ChemicalStack. The returned ChemicalStack can be safely modified after
     */
    ChemicalStack insertChemical(int tank, ChemicalStack stack, @Nullable Direction side, Action action);

    @Override
    default ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
        return insertChemical(tank, stack, getSideFor(), action);
    }

    /**
     * A sided variant of {@link IChemicalHandler#extractChemical(int, long, Action)}, docs copied for convenience.
     * <p>
     * Extracts a ChemicalStack from a specific tank in this handler.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param tank   Tank to extract from.
     * @param amount Amount to extract (may be greater than the current stack's amount or the tank's capacity)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return ChemicalStack extracted from the tank, must be empty if nothing can be extracted. The returned ChemicalStack can be safely modified after, so the tank
     * should return a new or copied stack.
     */
    ChemicalStack extractChemical(int tank, long amount, @Nullable Direction side, Action action);

    @Override
    default ChemicalStack extractChemical(int tank, long amount, Action action) {
        return extractChemical(tank, amount, getSideFor(), action);
    }

    /**
     * A sided variant of {@link IChemicalHandler#insertChemical(ChemicalStack, Action)}, docs copied for convenience.
     *
     * <p>
     * Inserts a ChemicalStack into this handler, distribution is left <strong>entirely</strong> to this {@link IChemicalHandler}. The ChemicalStack
     * <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction)}
     *
     * @param stack  ChemicalStack to insert. This must not be modified by the handler.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return The remaining ChemicalStack that was not inserted (if the entire stack is accepted, then return an empty ChemicalStack). May be the same as the input
     * ChemicalStack if unchanged, otherwise a new ChemicalStack. The returned ChemicalStack can be safely modified after
     *
     * @implNote The default implementation of this method, attempts to insert into tanks that contain the same type of chemical as the supplied type, and if it will not
     * all fit, falls back to inserting into any empty tanks.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IChemicalHandler} ends up distributing the insertion.
     */
    default ChemicalStack insertChemical(ChemicalStack stack, @Nullable Direction side, Action action) {
        return ChemicalUtils.insert(stack, side, action, this::getCountChemicalTanks, this::getChemicalInTank, this::insertChemical);
    }

    /**
     * A sided variant of {@link IChemicalHandler#extractChemical(long, Action)}, docs copied for convenience.
     * <p>
     * Extracts a ChemicalStack from this handler, distribution is left <strong>entirely</strong> to this {@link IChemicalHandler}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount Amount to extract (may be greater than the current stack's amount or the tank's capacity)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return ChemicalStack extracted from the tank, must be empty if nothing can be extracted. The returned ChemicalStack can be safely modified after, so the tank
     * should return a new or copied stack.
     *
     * @implNote The default implementation of this method, extracts across all tanks to try and reach the desired amount to extract. Once the first chemical that can be
     * extracted is found, all future extractions will make sure to also make sure they are for the same type of chemical.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IChemicalHandler} ends up distributing the extraction.
     */
    default ChemicalStack extractChemical(long amount, @Nullable Direction side, Action action) {
        return ChemicalUtils.extract(amount, side, action, this::getCountChemicalTanks, this::getChemicalInTank, this::extractChemical);
    }

    /**
     * A sided variant of {@link IChemicalHandler#extractChemical(ChemicalStack, Action)}, docs copied for convenience.
     * <p>
     * Extracts a ChemicalStack from this handler, distribution is left <strong>entirely</strong> to this {@link IChemicalHandler}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param stack  ChemicalStack representing the {@link Chemical} and maximum amount to be drained.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param side   The side we are interacting with the handler from (null for internal).
     *
     * @return ChemicalStack extracted from the tank, must be empty if nothing can be extracted. The returned ChemicalStack can be safely modified after, so the tank
     * should return a new or copied stack.
     *
     * @implNote The default implementation of this method, extracts across all tanks that contents match the type of chemical passed into this method.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IChemicalHandler} ends up distributing the extraction.
     */
    default ChemicalStack extractChemical(ChemicalStack stack, @Nullable Direction side, Action action) {
        return ChemicalUtils.extract(stack, side, action, this::getCountChemicalTanks, this::getChemicalInTank, this::extractChemical);
    }
}
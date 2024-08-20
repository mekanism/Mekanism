package mekanism.api.chemical;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

@NothingNullByDefault
public interface IChemicalHandler {

    /**
     * Returns the number of chemical storage units ("tanks") available
     *
     * @return The number of tanks available
     */
    int getChemicalTanks();

    /**
     * Returns the {@link ChemicalStack} in a given tank.
     *
     * <p>
     * <strong>IMPORTANT:</strong> This {@link ChemicalStack} <em>MUST NOT</em> be modified. This method is not for altering internal contents. Any implementers who are
     * able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the stack returned here will be a copy.
     * </p>
     *
     * <p>
     * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED CHEMICAL STACK</em></strong>
     * </p>
     *
     * @param tank Tank to query.
     *
     * @return {@link ChemicalStack} in a given tank. {@link ChemicalStack#EMPTY} if the tank is empty.
     */
    ChemicalStack getChemicalInTank(int tank);

    /**
     * Overrides the stack in the given tank. This method may throw an error if it is called unexpectedly.
     *
     * @param tank  Tank to modify
     * @param stack {@link ChemicalStack} to set tank to (may be empty).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting.
     **/
    void setChemicalInTank(int tank, ChemicalStack stack);

    /**
     * Retrieves the maximum amount of chemical that can be stored in a given tank.
     *
     * @param tank Tank to query.
     *
     * @return The maximum chemical amount held by the tank.
     */
    long getChemicalTankCapacity(int tank);

    /**
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
     * @param stack Stack to test with for validity
     *
     * @return true if the tank can accept the {@link ChemicalStack}, not considering the current state of the tank. false if the tank can never support the given {@link ChemicalStack}
     * in any situation.
     */
    boolean isValid(int tank, ChemicalStack stack);

    /**
     * <p>
     * Inserts a {@link ChemicalStack} into a given tank and return the remainder. The {@link ChemicalStack} <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction)}
     *
     * @param tank   Tank to insert to.
     * @param stack  {@link ChemicalStack} to insert. This must not be modified by the tank.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return The remaining {@link ChemicalStack} that was not inserted (if the entire stack is accepted, then return an empty {@link ChemicalStack}). May be the same as the input
     * {@link ChemicalStack} if unchanged, otherwise a new {@link ChemicalStack}. The returned {@link ChemicalStack} can be safely modified after
     */
    ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action);

    /**
     * Extracts a {@link ChemicalStack} from a specific tank in this handler.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param tank   Tank to extract from.
     * @param amount Amount to extract (may be greater than the current stack's amount or the tank's capacity)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return {@link ChemicalStack} extracted from the tank, must be empty if nothing can be extracted. The returned {@link ChemicalStack} can be safely modified after, so the tank
     * should return a new or copied stack.
     */
    ChemicalStack extractChemical(int tank, long amount, Action action);

    /**
     * <p>
     * Inserts a {@link ChemicalStack} into this handler, distribution is left <strong>entirely</strong> to this {@link IChemicalHandler}. The {@link ChemicalStack}
     * <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link IFluidHandler#fill(FluidStack, IFluidHandler.FluidAction)}
     *
     * @param stack  {@link ChemicalStack} to insert. This must not be modified by the handler.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return The remaining {@link ChemicalStack} that was not inserted (if the entire stack is accepted, then return an empty {@link ChemicalStack}). May be the same as the input
     * {@link ChemicalStack} if unchanged, otherwise a new {@link ChemicalStack}. The returned {@link ChemicalStack} can be safely modified after
     *
     * @implNote The default implementation of this method, attempts to insert into tanks that contain the same type of chemical as the supplied type, and if it will not
     * all fit, falls back to inserting into any empty tanks.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IChemicalHandler} ends up distributing the insertion.
     */
    default ChemicalStack insertChemical(ChemicalStack stack, Action action) {
        return ChemicalUtils.insert(stack, null, action, side -> getChemicalTanks(), (tank, side) -> getChemicalInTank(tank),
              (tank, chemical, s, act) -> insertChemical(tank, chemical, act));
    }

    /**
     * Extracts a {@link ChemicalStack} from this handler, distribution is left <strong>entirely</strong> to this {@link IChemicalHandler}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount Amount to extract (may be greater than the current stack's amount or the tank's capacity)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return {@link ChemicalStack} extracted from the tank, must be empty if nothing can be extracted. The returned {@link ChemicalStack} can be safely modified after, so the tank
     * should return a new or copied stack.
     *
     * @implNote The default implementation of this method, extracts across all tanks to try and reach the desired amount to extract. Once the first chemical that can be
     * extracted is found, all future extractions will make sure to also make sure they are for the same type of chemical.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IChemicalHandler} ends up distributing the extraction.
     */
    default ChemicalStack extractChemical(long amount, Action action) {
        return ChemicalUtils.extract(amount, null, action, side -> getChemicalTanks(), (tank, side) -> getChemicalInTank(tank),
              (tank, amt, side, act) -> extractChemical(tank, amt, act));
    }

    /**
     * Extracts a {@link ChemicalStack} from this handler, distribution is left <strong>entirely</strong> to this {@link IChemicalHandler}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param stack  {@link ChemicalStack} representing the {@link Chemical} and maximum amount to be drained.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return {@link ChemicalStack} extracted from the tank, must be empty if nothing can be extracted. The returned {@link ChemicalStack} can be safely modified after, so the tank
     * should return a new or copied stack.
     *
     * @implNote The default implementation of this method, extracts across all tanks that contents match the type of chemical passed into this method.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IChemicalHandler} ends up distributing the extraction.
     */
    default ChemicalStack extractChemical(ChemicalStack stack, Action action) {
        return ChemicalUtils.extract(stack, null, action, side -> getChemicalTanks(), (tank, side) -> getChemicalInTank(tank),
              (tank, chemical, side, act) -> extractChemical(tank, chemical, act));
    }
}
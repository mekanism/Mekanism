package mekanism.api.chemical;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends IEmptyStackProvider<CHEMICAL, STACK> {

    /**
     * Returns the number of chemical storage units ("tanks") available
     *
     * @return The number of tanks available
     */
    int getTanks();

    /**
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
     *
     * @return {@link STACK} in a given tank. {@link #getEmptyStack()} if the tank is empty.
     */
    STACK getChemicalInTank(int tank);

    /**
     * Overrides the stack in the given tank. This method may throw an error if it is called unexpectedly.
     *
     * @param tank  Tank to modify
     * @param stack {@link STACK} to set tank to (may be empty).
     *
     * @throws RuntimeException if the handler is called in a way that the handler was not expecting.
     **/
    void setChemicalInTank(int tank, STACK stack);

    /**
     * Retrieves the maximum amount of chemical that can be stored in a given tank.
     *
     * @param tank Tank to query.
     *
     * @return The maximum chemical amount held by the tank.
     */
    long getTankCapacity(int tank);

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
     * @return true if the tank can accept the {@link STACK}, not considering the current state of the tank. false if the tank can never support the given {@link STACK}
     * in any situation.
     */
    boolean isValid(int tank, STACK stack);

    /**
     * <p>
     * Inserts a {@link STACK} into a given tank and return the remainder. The {@link STACK} <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param tank   Tank to insert to.
     * @param stack  {@link STACK} to insert. This must not be modified by the tank.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return The remaining {@link STACK} that was not inserted (if the entire stack is accepted, then return an empty {@link STACK}). May be the same as the input
     * {@link STACK} if unchanged, otherwise a new {@link STACK}. The returned {@link STACK} can be safely modified after
     */
    STACK insertChemical(int tank, STACK stack, Action action);

    /**
     * Extracts a {@link STACK} from a specific tank in this handler.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param tank   Tank to extract from.
     * @param amount Amount to extract (may be greater than the current stack's amount or the tank's capacity)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return {@link STACK} extracted from the tank, must be empty if nothing can be extracted. The returned {@link STACK} can be safely modified after, so the tank
     * should return a new or copied stack.
     */
    STACK extractChemical(int tank, long amount, Action action);

    /**
     * <p>
     * Inserts a {@link STACK} into this handler, distribution is left <strong>entirely</strong> to this {@link IChemicalHandler}. The {@link STACK}
     * <em>should not</em> be modified in this function!
     * </p>
     * Note: This behaviour is subtly different from {@link net.minecraftforge.fluids.capability.IFluidHandler#fill(net.minecraftforge.fluids.FluidStack,
     * net.minecraftforge.fluids.capability.IFluidHandler.FluidAction)}
     *
     * @param stack  {@link STACK} to insert. This must not be modified by the handler.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return The remaining {@link STACK} that was not inserted (if the entire stack is accepted, then return an empty {@link STACK}). May be the same as the input
     * {@link STACK} if unchanged, otherwise a new {@link STACK}. The returned {@link STACK} can be safely modified after
     *
     * @implNote The default implementation of this method, attempts to insert into tanks that contain the same type of chemical as the supplied type, and if it will not
     * all fit, falls back to inserting into any empty tanks.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IChemicalHandler} ends up distributing the insertion.
     */
    default STACK insertChemical(STACK stack, Action action) {
        return ChemicalUtils.insert(stack, action, getEmptyStack(), this::getTanks, this::getChemicalInTank, this::insertChemical);
    }

    /**
     * Extracts a {@link STACK} from this handler, distribution is left <strong>entirely</strong> to this {@link IChemicalHandler}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param amount Amount to extract (may be greater than the current stack's amount or the tank's capacity)
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return {@link STACK} extracted from the tank, must be empty if nothing can be extracted. The returned {@link STACK} can be safely modified after, so the tank
     * should return a new or copied stack.
     *
     * @implNote The default implementation of this method, extracts across all tanks to try and reach the desired amount to extract. Once the first chemical that can be
     * extracted is found, all future extractions will make sure to also make sure they are for the same type of chemical.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IChemicalHandler} ends up distributing the extraction.
     */
    default STACK extractChemical(long amount, Action action) {
        return ChemicalUtils.extract(amount, action, getEmptyStack(), this::getTanks, this::getChemicalInTank, this::extractChemical);
    }

    /**
     * Extracts a {@link STACK} from this handler, distribution is left <strong>entirely</strong> to this {@link IChemicalHandler}.
     * <p>
     * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
     * </p>
     *
     * @param stack  {@link STACK} representing the {@link CHEMICAL} and maximum amount to be drained.
     * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     *
     * @return {@link STACK} extracted from the tank, must be empty if nothing can be extracted. The returned {@link STACK} can be safely modified after, so the tank
     * should return a new or copied stack.
     *
     * @implNote The default implementation of this method, extracts across all tanks that contents match the type of chemical passed into this method.
     * @apiNote It is not guaranteed that the default implementation will be how this {@link IChemicalHandler} ends up distributing the extraction.
     */
    default STACK extractChemical(STACK stack, Action action) {
        return ChemicalUtils.extract(stack, action, getEmptyStack(), this::getTanks, this::getChemicalInTank, this::extractChemical);
    }
}
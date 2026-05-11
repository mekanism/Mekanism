package mekanism.api.chemical;

import mekanism.api.annotations.NothingNullByDefault;

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
}
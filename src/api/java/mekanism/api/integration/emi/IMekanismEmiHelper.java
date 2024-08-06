package mekanism.api.integration.emi;

import dev.emi.emi.api.stack.EmiStack;
import java.util.Optional;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;

/**
 * Helper for interacting with Mekanism's internals related to EMI. Get an instance via {@link mekanism.api.IMekanismAccess#emiHelper()} after ensuring that EMI is
 * loaded.
 *
 * @since 10.5.10
 */
public interface IMekanismEmiHelper {

    /**
     * Creates an EmiStack for the given chemical stack.
     */
    default EmiStack createEmiStack(ChemicalStack stack) {
        return createEmiStack(stack.getChemical(), stack.getAmount());
    }

    /**
     * Creates an EmiStack of the given size for the given chemical.
     */
    EmiStack createEmiStack(Chemical chemical, long size);

    /**
     * Tries to convert a chemical EmiStack to a normal ChemicalStack
     *
     * @param stack EmiStack to convert.
     *
     * @return ChemicalStack or an empty optional if the EmiStack doesn't represent a ChemicalStack.
     */
    Optional<ChemicalStack> asChemicalStack(EmiStack stack);
}
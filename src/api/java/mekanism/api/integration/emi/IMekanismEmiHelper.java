package mekanism.api.integration.emi;

import dev.emi.emi.api.stack.EmiStack;
import java.util.Optional;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.core.Holder;

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
        return createEmiStack(stack.getChemicalHolder(), stack.getAmount());
    }

    /**
     * Creates an EmiStack of the given size for the given chemical.
     *
     * @deprecated Use {@link #createEmiStack(Holder, long)} instead
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default EmiStack createEmiStack(Chemical chemical, long size) {
        return createEmiStack(chemical.getAsHolder(), size);
    }

    /**
     * Creates an EmiStack of the given size for the given chemical.
     *
     * @since 10.7.11
     */
    EmiStack createEmiStack(Holder<Chemical> chemical, long size);

    /**
     * Tries to convert a chemical EmiStack to a normal ChemicalStack
     *
     * @param stack EmiStack to convert.
     *
     * @return ChemicalStack or an empty optional if the EmiStack doesn't represent a ChemicalStack.
     */
    Optional<ChemicalStack> asChemicalStack(EmiStack stack);
}
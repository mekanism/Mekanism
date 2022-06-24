package mekanism.api.chemical;

import org.jetbrains.annotations.NotNull;

public interface IEmptyStackProvider<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> {

    /**
     * Helper for default implementations to get the empty stack corresponding to the type of chemical this provider is for
     *
     * @return The empty stack.
     */
    @NotNull
    STACK getEmptyStack();
}
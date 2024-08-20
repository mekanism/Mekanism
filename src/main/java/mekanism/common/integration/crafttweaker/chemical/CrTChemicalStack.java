package mekanism.common.integration.crafttweaker.chemical;

import mekanism.api.chemical.ChemicalStack;

public class CrTChemicalStack extends BaseCrTChemicalStack {

    public CrTChemicalStack(ChemicalStack stack) {
        super(stack, CrTChemicalStack::new);
    }

    @Override
    public ICrTChemicalStack setAmount(long amount) {
        return stackConverter.apply(stack.copyWithAmount(amount));
    }

    @Override
    public ICrTChemicalStack asMutable() {
        return new CrTMutableChemicalStack(stack);
    }

    @Override
    public ICrTChemicalStack asImmutable() {
        return this;
    }

    @Override
    public ChemicalStack getImmutableInternal() {
        return getInternal();
    }

}
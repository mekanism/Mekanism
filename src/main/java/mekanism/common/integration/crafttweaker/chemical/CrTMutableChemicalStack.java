package mekanism.common.integration.crafttweaker.chemical;

import mekanism.api.chemical.ChemicalStack;

public class CrTMutableChemicalStack extends BaseCrTChemicalStack {

    public CrTMutableChemicalStack(ChemicalStack stack) {
        super(stack, CrTMutableChemicalStack::new);
    }

    @Override
    public ICrTChemicalStack setAmount(long amount) {
        stack.setAmount(amount);
        return asMutable();
    }

    @Override
    public ICrTChemicalStack asMutable() {
        return this;
    }

    @Override
    public ICrTChemicalStack asImmutable() {
        return new CrTChemicalStack(stack);
    }

    @Override
    public ChemicalStack getImmutableInternal() {
        return stack.copy();
    }

    @Override
    protected StringBuilder getBracket() {
        return super.getBracket().append(".mutable()");
    }
}
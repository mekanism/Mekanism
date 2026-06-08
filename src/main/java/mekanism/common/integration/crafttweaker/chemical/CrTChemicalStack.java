package mekanism.common.integration.crafttweaker.chemical;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalStackTemplate;

public class CrTChemicalStack extends BaseCrTChemicalStack {

    public CrTChemicalStack(ChemicalStackTemplate template) {
        this(template.create());
    }

    public CrTChemicalStack(ChemicalStack stack) {
        super(stack, CrTChemicalStack::new);
    }

    @Override
    public ICrTChemicalStack setAmount(int amount) {
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
package mekanism.common.integration.crafttweaker.chemical;

import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.util.RegistryUtils;

public abstract class BaseCrTChemicalStack implements ICrTChemicalStack {

    protected final ChemicalStack stack;
    protected final Function<ChemicalStack, ICrTChemicalStack> stackConverter;

    public BaseCrTChemicalStack(ChemicalStack stack, Function<ChemicalStack, ICrTChemicalStack> stackConverter) {
        this.stack = stack;
        this.stackConverter = stackConverter;
    }

    protected StringBuilder getBracket() {
        return new StringBuilder().append('<')
              .append(CrTConstants.BRACKET_CHEMICAL)
              .append(':')
              .append(RegistryUtils.getName(stack.getChemicalHolder(), MekanismAPI.CHEMICAL_REGISTRY))
              .append('>');
    }

    @Override
    public String getCommandString() {
        StringBuilder builder = getBracket();
        if (!stack.isEmpty() && stack.getAmount() != 1) {
            builder.append(" * ").append(stack.getAmount());
        }
        return builder.toString();
    }

    @Override
    public ICrTChemicalStack copy() {
        //We have to copy, in case someone calls ".copy().mutable"
        return stackConverter.apply(stack.copy());
    }

    @Override
    public ChemicalStack getInternal() {
        return stack;
    }

    @Override
    public String toString() {
        return getCommandString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return stack.equals(((BaseCrTChemicalStack) o).stack);
    }

    @Override
    public int hashCode() {
        return stack.hashCode();
    }
}
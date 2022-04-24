package mekanism.common.integration.crafttweaker.chemical;

import java.util.function.Function;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.util.ChemicalUtil;

public abstract class BaseCrTChemicalStack<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>> implements ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK> {

    protected final STACK stack;
    protected final Function<STACK, CRT_STACK> stackConverter;

    public BaseCrTChemicalStack(STACK stack, Function<STACK, CRT_STACK> stackConverter) {
        this.stack = stack;
        this.stackConverter = stackConverter;
    }

    protected StringBuilder getBracket() {
        return new StringBuilder().append('<')
              .append(getBracketName())
              .append(':')
              .append(stack.getTypeRegistryName())
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
    public CRT_STACK copy() {
        //We have to copy, in case someone calls ".copy().mutable"
        return stackConverter.apply(ChemicalUtil.copy(stack));
    }

    @Override
    public STACK getInternal() {
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
        return stack.equals(((BaseCrTChemicalStack<?, ?, ?>) o).stack);
    }

    @Override
    public int hashCode() {
        return stack.hashCode();
    }
}
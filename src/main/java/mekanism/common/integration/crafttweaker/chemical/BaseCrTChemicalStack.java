package mekanism.common.integration.crafttweaker.chemical;

import java.util.function.Function;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.util.ChemicalUtil;

public abstract class BaseCrTChemicalStack<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      CRT_CHEMICAL extends ICrTChemical<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>>
      implements ICrTChemicalStack<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK> {

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
        if (stack.getAmount() != 1) {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        STACK otherStack = ((BaseCrTChemicalStack<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>) o).stack;
        //TODO: If we decide to make equals care about amount (which we probably will do), then we can
        // remove comparing the stack size here (and in the below isEqual method)
        return stack.equals(otherStack) && stack.getAmount() == otherStack.getAmount();
    }

    @Override
    public boolean isEqual(CRT_STACK other) {
        //Allow mutable and non mutable CrT stacks, to be equal when comparing them in CrT
        STACK otherStack = other.getInternal();
        return stack.equals(otherStack) && stack.getAmount() == otherStack.getAmount();
    }

    @Override
    public int hashCode() {
        return stack.hashCode();
    }
}
package mekanism.common.content.network.distribution;

import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;

public class ChemicalHandlerTarget extends Target<IChemicalHandler, ChemicalStack> {

    public ChemicalHandlerTarget(ChemicalStack type) {
        this.extra = type;
    }

    public ChemicalHandlerTarget(ChemicalStack type, int expectedSize) {
        super(expectedSize);
        this.extra = type;
    }

    @Override
    protected void acceptAmount(IChemicalHandler handler, SplitInfo splitInfo, long amount) {
        splitInfo.send(amount - handler.insertChemical(extra.copyWithAmount(amount), Action.EXECUTE).getAmount());
    }

    @Override
    protected long simulate(IChemicalHandler handler, ChemicalStack stack) {
        return stack.getAmount() - handler.insertChemical(stack, Action.SIMULATE).getAmount();
    }
}
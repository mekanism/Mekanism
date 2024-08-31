package mekanism.common.content.network.distribution;

import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;

public class ChemicalHandlerTarget extends Target<IChemicalHandler, ChemicalStack> {

    public ChemicalHandlerTarget() {
    }

    public ChemicalHandlerTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected void acceptAmount(IChemicalHandler handler, SplitInfo splitInfo, ChemicalStack resource, long amount) {
        splitInfo.send(amount - handler.insertChemical(resource.copyWithAmount(amount), Action.EXECUTE).getAmount());
    }

    @Override
    protected long simulate(IChemicalHandler handler, ChemicalStack resource, long amount) {
        return resource.getAmount() - handler.insertChemical(resource.copyWithAmount(amount), Action.SIMULATE).getAmount();
    }
}
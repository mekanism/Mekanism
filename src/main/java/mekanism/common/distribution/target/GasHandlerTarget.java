package mekanism.common.distribution.target;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.distribution.SplitInfo;

public class GasHandlerTarget extends Target<IGasHandler, Long, @NonNull GasStack> {

    public GasHandlerTarget(@Nonnull GasStack type) {
        this.extra = type;
    }

    @Override
    protected void acceptAmount(IGasHandler handler, SplitInfo<Long> splitInfo, Long amount) {
        splitInfo.send(amount - handler.insertChemical(new GasStack(extra, amount), Action.EXECUTE).getAmount());
    }

    @Override
    protected Long simulate(IGasHandler handler, @Nonnull GasStack gasStack) {
        return gasStack.getAmount() - handler.insertChemical(gasStack, Action.SIMULATE).getAmount();
    }
}
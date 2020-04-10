package mekanism.common.distribution.target;

import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.distribution.SplitInfo;

public class GasHandlerTarget extends Target<IGasHandler, Long, @NonNull GasStack> {

    public GasHandlerTarget(@NonNull GasStack type) {
        this.extra = type;
    }

    @Override
    protected void acceptAmount(IGasHandler handler, SplitInfo<Long> splitInfo, Long amount) {
        splitInfo.send(amount - handler.insertGas(new GasStack(extra, amount), Action.EXECUTE).getAmount());
    }

    @Override
    protected Long simulate(IGasHandler handler, @NonNull GasStack gasStack) {
        return gasStack.getAmount() - handler.insertGas(gasStack, Action.SIMULATE).getAmount();
    }
}
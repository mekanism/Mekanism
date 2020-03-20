package mekanism.common.base.target;

import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.base.SplitInfo;

public class GasHandlerTarget extends Target<IGasHandler, Integer, @NonNull GasStack> {

    public GasHandlerTarget(@NonNull GasStack type) {
        this.extra = type;
    }

    @Override
    protected void acceptAmount(IGasHandler handler, SplitInfo<Integer> splitInfo, Integer amount) {
        splitInfo.send(amount - handler.insertGas(new GasStack(extra, amount), Action.EXECUTE).getAmount());
    }

    @Override
    protected Integer simulate(IGasHandler handler, @NonNull GasStack gasStack) {
        return gasStack.getAmount() - handler.insertGas(gasStack, Action.SIMULATE).getAmount();
    }
}
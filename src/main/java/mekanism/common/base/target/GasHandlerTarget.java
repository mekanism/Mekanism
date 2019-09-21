package mekanism.common.base.target;

import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.common.base.SplitInfo;
import net.minecraft.util.Direction;

public class GasHandlerTarget extends Target<IGasHandler, Integer, @NonNull GasStack> {

    public GasHandlerTarget(@NonNull GasStack type) {
        this.extra = type;
    }

    @Override
    protected void acceptAmount(Direction side, SplitInfo<Integer> splitInfo, Integer amount) {
        splitInfo.send(handlers.get(side).receiveGas(side, new GasStack(extra, amount), true));
    }

    @Override
    protected Integer simulate(IGasHandler handler, Direction side, @NonNull GasStack gasStack) {
        return handler.receiveGas(side, gasStack, false);
    }
}
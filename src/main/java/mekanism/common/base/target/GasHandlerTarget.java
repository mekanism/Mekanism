package mekanism.common.base.target;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.common.base.SplitInfo;
import net.minecraft.util.EnumFacing;

public class GasHandlerTarget extends Target<IGasHandler, Integer, GasStack> {

    public GasHandlerTarget(GasStack type) {
        this.extra = type;
    }

    @Override
    protected void acceptAmount(EnumFacing side, SplitInfo<Integer> splitInfo, Integer amount) {
        splitInfo.send(handlers.get(side).receiveGas(side, new GasStack(extra.getGas(), amount), true));
    }

    @Override
    protected Integer simulate(IGasHandler handler, EnumFacing side, GasStack gasStack) {
        return handler.receiveGas(side, gasStack, false);
    }
}
package mekanism.common.base.target;

import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.base.SplitInfo;
import net.minecraft.util.Direction;

public class GasHandlerTarget extends Target<IGasHandler, Integer, @NonNull GasStack> {

    public GasHandlerTarget(@NonNull GasStack type) {
        this.extra = type;
    }

    @Override
    protected void acceptAmount(Direction side, SplitInfo<Integer> splitInfo, Integer amount) {
        splitInfo.send(amount - handlers.get(side).insertGas(new GasStack(extra, amount), Action.EXECUTE).getAmount());
    }

    @Override
    protected Integer simulate(IGasHandler handler, Direction side, @NonNull GasStack gasStack) {
        return gasStack.getAmount() - handler.insertGas(gasStack, Action.SIMULATE).getAmount();
    }
}
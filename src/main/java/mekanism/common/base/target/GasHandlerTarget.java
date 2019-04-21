package mekanism.common.base.target;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import net.minecraft.util.EnumFacing;

public class GasHandlerTarget extends IntegerTypeTarget<IGasHandler> {

    private Gas type;

    public GasHandlerTarget(Gas type) {
        this.type = type;
    }

    @Override
    protected Integer acceptAmount(EnumFacing side, Integer amount) {
        //Give it gas and add how much actually got accepted instead of how much
        // we attempted to give it
        return handlers.get(side).receiveGas(side, new GasStack(type, amount), true);
    }
}
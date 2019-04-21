package mekanism.common.base;

import java.util.Map.Entry;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import net.minecraft.util.EnumFacing;

public class GasHandlerTarget extends Target<IGasHandler, Integer> {

    private Gas type;

    public GasHandlerTarget(Gas type) {
        this.type = type;
    }

    @Override
    public Integer sendGivenWithDefault(Integer amountPer) {
        int sent = 0;
        for (Entry<EnumFacing, Integer> giveInfo : given.entrySet()) {
            sent += acceptAmount(giveInfo.getKey(), giveInfo.getValue());
        }
        //If needed is not empty then we default it to the given calculated fair split amount of remaining energy
        for (EnumFacing side : needed.keySet()) {
            sent += acceptAmount(side, amountPer);
        }
        return sent;
    }

    private int acceptAmount(EnumFacing side, int amount) {
        //Give it gas and add how much actually got accepted instead of how much
        // we attempted to give it
        return wrappers.get(side).receiveGas(side, new GasStack(type, amount), true);
    }
}
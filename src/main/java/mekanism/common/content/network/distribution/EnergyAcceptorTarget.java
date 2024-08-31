package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;

public class EnergyAcceptorTarget extends Target<IStrictEnergyHandler, Void> {

    public EnergyAcceptorTarget() {
    }

    public EnergyAcceptorTarget(Collection<IStrictEnergyHandler> allHandlers) {
        super(allHandlers);
    }

    public EnergyAcceptorTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected void acceptAmount(IStrictEnergyHandler handler, SplitInfo splitInfo, Void unused, long amount) {
        splitInfo.send(amount - (handler.insertEnergy(amount, Action.EXECUTE)));
    }

    @Override
    protected long simulate(IStrictEnergyHandler handler, Void unused, long amount) {
        return amount - (handler.insertEnergy(amount, Action.SIMULATE));
    }
}
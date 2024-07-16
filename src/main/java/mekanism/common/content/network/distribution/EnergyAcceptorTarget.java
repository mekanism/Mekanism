package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;

public class EnergyAcceptorTarget extends Target<IStrictEnergyHandler, Long, Long> {

    public EnergyAcceptorTarget() {
    }

    public EnergyAcceptorTarget(Collection<IStrictEnergyHandler> allHandlers) {
        super(allHandlers);
    }

    public EnergyAcceptorTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected void acceptAmount(IStrictEnergyHandler handler, SplitInfo<Long> splitInfo, Long amount) {
        splitInfo.send(amount - (handler.insertEnergy(amount, Action.EXECUTE)));
    }

    @Override
    protected Long simulate(IStrictEnergyHandler handler, Long energyToSend) {
        return energyToSend - (handler.insertEnergy(energyToSend, Action.SIMULATE));
    }
}
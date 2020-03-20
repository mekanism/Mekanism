package mekanism.common.base.target;

import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.base.SplitInfo;

public class EnergyAcceptorTarget extends Target<IStrictEnergyHandler, Double, Double> {

    @Override
    protected void acceptAmount(IStrictEnergyHandler handler, SplitInfo<Double> splitInfo, Double amount) {
        splitInfo.send(amount - handler.insertEnergy(amount, Action.EXECUTE));
    }

    @Override
    protected Double simulate(IStrictEnergyHandler handler, Double energyToSend) {
        return energyToSend - handler.insertEnergy(energyToSend, Action.SIMULATE);
    }
}
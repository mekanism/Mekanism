package mekanism.common.base.target;

import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.base.SplitInfo;
import net.minecraft.util.Direction;

public class EnergyAcceptorTarget extends Target<IStrictEnergyHandler, Double, Double> {

    @Override
    protected void acceptAmount(Direction side, SplitInfo<Double> splitInfo, Double amount) {
        splitInfo.send(amount - handlers.get(side).insertEnergy(amount, Action.EXECUTE));
    }

    @Override
    protected Double simulate(IStrictEnergyHandler handler, Direction side, Double energyToSend) {
        return energyToSend - handler.insertEnergy(energyToSend, Action.SIMULATE);
    }
}
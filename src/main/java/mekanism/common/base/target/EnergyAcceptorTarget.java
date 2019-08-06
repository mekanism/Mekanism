package mekanism.common.base.target;

import mekanism.common.base.EnergyAcceptorWrapper;
import mekanism.common.base.SplitInfo;
import net.minecraft.util.Direction;

public class EnergyAcceptorTarget extends Target<EnergyAcceptorWrapper, Double, Double> {

    @Override
    protected void acceptAmount(Direction side, SplitInfo<Double> splitInfo, Double amount) {
        splitInfo.send(handlers.get(side).acceptEnergy(side, amount, false));
    }

    @Override
    protected Double simulate(EnergyAcceptorWrapper wrapper, Direction side, Double energyToSend) {
        return wrapper.acceptEnergy(side, energyToSend, true);
    }
}
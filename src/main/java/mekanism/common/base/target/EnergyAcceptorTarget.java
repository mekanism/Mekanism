package mekanism.common.base.target;

import java.util.Map.Entry;
import mekanism.common.base.EnergyAcceptorWrapper;
import net.minecraft.util.EnumFacing;

public class EnergyAcceptorTarget extends Target<EnergyAcceptorWrapper, Double, Double> {

    @Override
    public Double sendGivenWithDefault(Double amountPer) {
        double sent = 0;
        for (Entry<EnumFacing, Double> giveInfo : given.entrySet()) {
            sent += acceptAmount(giveInfo.getKey(), giveInfo.getValue());
        }
        //If needed is not empty then we default it to the given calculated fair split amount of remaining energy
        for (EnumFacing side : needed.keySet()) {
            sent += acceptAmount(side, amountPer);
        }
        return sent;
    }

    @Override
    protected Double acceptAmount(EnumFacing side, Double amount) {
        //Give it power and add how much actually got accepted instead of how much
        // we attempted to give it
        return handlers.get(side).acceptEnergy(side, amount, false);
    }

    @Override
    public Double simulate(EnergyAcceptorWrapper wrapper, EnumFacing side, Double energyToSend) {
        return wrapper.acceptEnergy(side, energyToSend, true);
    }
}
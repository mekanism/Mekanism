package mekanism.common.base.target;

import java.util.Iterator;
import java.util.Map.Entry;
import mekanism.common.base.EnergyAcceptorWrapper;
import mekanism.common.base.SplitInfo;
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

    @Override
    public void shiftNeeded(SplitInfo<Double> splitInfo) {
        //Use an iterator rather than a copy of the keyset of the needed submap
        // This allows for us to remove it once we find it without  having to
        // start looping again or make a large number of copies of the set
        Iterator<Entry<EnumFacing, Double>> iterator = needed.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<EnumFacing, Double> needInfo = iterator.next();
            double amountNeeded = needInfo.getValue();
            if (amountNeeded <= splitInfo.getAmountPer()) {
                addGiven(needInfo.getKey(), amountNeeded);
                //Remove it as it no longer valid
                iterator.remove();
                //Remove this amount from the split calculation
                splitInfo.remove(amountNeeded);
            }
        }
    }
}
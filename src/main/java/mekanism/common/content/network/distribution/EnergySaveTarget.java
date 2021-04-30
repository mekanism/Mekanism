package mekanism.common.content.network.distribution;

import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import net.minecraft.util.Direction;

import java.util.Collection;

public class EnergySaveTarget extends Target<IEnergyContainer, FloatingLong, FloatingLong> {

    private FloatingLong currentStored = FloatingLong.ZERO;

    public EnergySaveTarget() {
    }

    public EnergySaveTarget(Collection<IEnergyContainer> allHandlers) {
        super(allHandlers);
    }

    public EnergySaveTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected void acceptAmount(IEnergyContainer container, SplitInfo<FloatingLong> splitInfo, FloatingLong amount) {
        amount = amount.min(container.getMaxEnergy().subtract(currentStored));
        currentStored = currentStored.plusEqual(amount);
        splitInfo.send(amount);
    }

    @Override
    protected FloatingLong simulate(IEnergyContainer container, FloatingLong energyToSend) {
        return energyToSend.copy().min(container.getMaxEnergy().subtract(currentStored));
    }

    public void save() {
        for (IEnergyContainer container : handlers) {
            if (container != null) {
                container.setEnergy(currentStored);
            }
        }
    }
}
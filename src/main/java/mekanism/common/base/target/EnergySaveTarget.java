package mekanism.common.base.target;

import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.base.SplitInfo;
import net.minecraft.util.Direction;

public class EnergySaveTarget extends Target<IEnergyContainer, FloatingLong, FloatingLong> {

    private FloatingLong currentStored = FloatingLong.ZERO;

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

    public void save(Direction fakeDirection) {
        IEnergyContainer container = handlers.get(fakeDirection);
        if (container != null) {
            container.setEnergy(currentStored);
        }
    }
}
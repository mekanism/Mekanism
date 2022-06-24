package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;

public class EnergySaveTarget extends Target<EnergySaveTarget.SaveHandler, FloatingLong, FloatingLong> {

    public EnergySaveTarget() {
    }

    public EnergySaveTarget(Collection<EnergySaveTarget.SaveHandler> allHandlers) {
        super(allHandlers);
    }

    public EnergySaveTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected void acceptAmount(EnergySaveTarget.SaveHandler handler, SplitInfo<FloatingLong> splitInfo, FloatingLong amount) {
        handler.acceptAmount(splitInfo, amount);
    }

    @Override
    protected FloatingLong simulate(EnergySaveTarget.SaveHandler handler, FloatingLong energyToSend) {
        return handler.simulate(energyToSend);
    }

    public void save() {
        for (SaveHandler handler : handlers) {
            handler.save();
        }
    }

    public void addDelegate(IEnergyContainer delegate) {
        this.addHandler(new SaveHandler(delegate));
    }

    @NothingNullByDefault
    public static class SaveHandler {

        private final IEnergyContainer delegate;
        private FloatingLong currentStored = FloatingLong.ZERO;

        public SaveHandler(IEnergyContainer delegate) {
            this.delegate = delegate;
        }

        protected void acceptAmount(SplitInfo<FloatingLong> splitInfo, FloatingLong amount) {
            amount = amount.min(delegate.getMaxEnergy().subtract(currentStored));
            currentStored = currentStored.plusEqual(amount);
            splitInfo.send(amount);
        }

        protected FloatingLong simulate(FloatingLong energyToSend) {
            return energyToSend.copy().min(delegate.getMaxEnergy().subtract(currentStored));
        }

        protected void save() {
            delegate.setEnergy(currentStored);
        }
    }
}
package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;

public class EnergySaveTarget<HANDLER extends EnergySaveTarget.SaveHandler> extends Target<HANDLER, FloatingLong, FloatingLong> {

    public EnergySaveTarget() {
    }

    public EnergySaveTarget(Collection<HANDLER> allHandlers) {
        super(allHandlers);
    }

    public EnergySaveTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected void acceptAmount(HANDLER handler, SplitInfo<FloatingLong> splitInfo, FloatingLong amount) {
        handler.acceptAmount(splitInfo, amount);
    }

    @Override
    protected FloatingLong simulate(HANDLER handler, FloatingLong energyToSend) {
        return handler.simulate(energyToSend);
    }

    public void save() {
        for (HANDLER handler : handlers) {
            handler.save();
        }
    }

    public FloatingLong getStored() {
        FloatingLong total = FloatingLong.ZERO;
        for (HANDLER handler : handlers) {
            total = total.plusEqual(handler.getStored());
        }
        return total;
    }

    @NothingNullByDefault
    public abstract static class SaveHandler {

        private final FloatingLong maxEnergy;
        private FloatingLong neededEnergy;

        protected SaveHandler(FloatingLong maxEnergy) {
            this.maxEnergy = maxEnergy;
            this.neededEnergy = this.maxEnergy.copy();
        }

        protected void acceptAmount(SplitInfo<FloatingLong> splitInfo, FloatingLong amount) {
            if (neededEnergy.isZero()) {
                splitInfo.send(FloatingLong.ZERO);
            } else {
                amount = amount.min(neededEnergy);
                neededEnergy = neededEnergy.minusEqual(amount);
                splitInfo.send(amount);
            }
        }

        protected FloatingLong simulate(FloatingLong energyToSend) {
            if (neededEnergy.isZero()) {
                return FloatingLong.ZERO;
            }
            return energyToSend.min(neededEnergy).copy();
        }

        protected final void save() {
            save(maxEnergy.subtract(neededEnergy));
        }

        protected abstract void save(FloatingLong currentStored);

        protected abstract FloatingLong getStored();
    }

    @NothingNullByDefault
    public static class DelegateSaveHandler extends SaveHandler {

        private final IEnergyContainer delegate;

        public DelegateSaveHandler(IEnergyContainer delegate) {
            super(delegate.getMaxEnergy());
            this.delegate = delegate;
        }

        @Override
        protected void save(FloatingLong currentStored) {
            delegate.setEnergy(currentStored);
        }

        @Override
        protected FloatingLong getStored() {
            return delegate.getEnergy();
        }
    }
}
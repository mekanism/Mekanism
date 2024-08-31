package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.MathUtils;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;

public class EnergySaveTarget<HANDLER extends EnergySaveTarget.SaveHandler> extends Target<HANDLER, Void> {

    public EnergySaveTarget() {
    }

    public EnergySaveTarget(Collection<HANDLER> allHandlers) {
        super(allHandlers);
    }

    public EnergySaveTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected void acceptAmount(HANDLER handler, SplitInfo splitInfo, Void unused, long amount) {
        handler.acceptAmount(splitInfo, amount);
    }

    @Override
    protected long simulate(HANDLER handler, Void unused, long amount) {
        return handler.simulate(amount);
    }

    public void save() {
        for (HANDLER handler : handlers) {
            handler.save();
        }
    }

    public long getStored() {
        long total = 0;
        for (HANDLER handler : handlers) {
            total = MathUtils.addClamped(total, handler.getStored());
        }
        return total;
    }

    @NothingNullByDefault
    public abstract static class SaveHandler {

        private final long maxEnergy;
        private long neededEnergy;

        protected SaveHandler(long maxEnergy) {
            this.maxEnergy = maxEnergy;
            this.neededEnergy = this.maxEnergy;
        }

        protected void acceptAmount(SplitInfo splitInfo, long amount) {
            if (neededEnergy <= 0L) {
                splitInfo.send(0L);
            } else {
                amount = Math.min(amount, neededEnergy);
                neededEnergy -= amount;
                splitInfo.send(amount);
            }
        }

        protected long simulate(long energyToSend) {
            if (neededEnergy <= 0L || energyToSend <= 0L) {
                return 0L;
            }
            return Math.min(energyToSend, neededEnergy);
        }

        protected final void save() {
            save(maxEnergy - neededEnergy);
        }

        protected abstract void save(long currentStored);

        protected abstract long getStored();
    }

    @NothingNullByDefault
    public static class DelegateSaveHandler extends SaveHandler {

        private final IEnergyContainer delegate;

        public DelegateSaveHandler(IEnergyContainer delegate) {
            super(delegate.getMaxEnergy());
            this.delegate = delegate;
        }

        @Override
        protected void save(long currentStored) {
            delegate.setEnergy(currentStored);
        }

        @Override
        protected long getStored() {
            return delegate.getEnergy();
        }
    }
}
package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.ULong;
import mekanism.api.math.Unsigned;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;

public class EnergySaveTarget<HANDLER extends EnergySaveTarget.SaveHandler> extends Target<HANDLER, @Unsigned Long, @Unsigned Long> {

    public EnergySaveTarget() {
    }

    public EnergySaveTarget(Collection<HANDLER> allHandlers) {
        super(allHandlers);
    }

    public EnergySaveTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected void acceptAmount(HANDLER handler, SplitInfo<@Unsigned Long> splitInfo, @Unsigned Long amount) {
        handler.acceptAmount(splitInfo, amount);
    }

    @Override
    protected @Unsigned Long simulate(HANDLER handler, @Unsigned Long energyToSend) {
        return handler.simulate(energyToSend);
    }

    public void save() {
        for (HANDLER handler : handlers) {
            handler.save();
        }
    }

    public @Unsigned long getStored() {
        @Unsigned long total = 0;
        for (HANDLER handler : handlers) {
            total += handler.getStored();
        }
        return total;
    }

    @NothingNullByDefault
    public abstract static class SaveHandler {

        private final @Unsigned long maxEnergy;
        private @Unsigned long neededEnergy;

        protected SaveHandler(@Unsigned long maxEnergy) {
            this.maxEnergy = maxEnergy;
            this.neededEnergy = this.maxEnergy;
        }

        protected void acceptAmount(SplitInfo<@Unsigned Long> splitInfo, @Unsigned long amount) {
            if (neededEnergy == 0L) {
                splitInfo.send(0L);
            } else {
                amount = ULong.min(amount, neededEnergy);
                neededEnergy -= amount;
                splitInfo.send(amount);
            }
        }

        protected @Unsigned long simulate(@Unsigned long energyToSend) {
            if (neededEnergy == 0L) {
                return 0L;
            }
            return ULong.min(energyToSend, neededEnergy);
        }

        protected final void save() {
            save(maxEnergy - neededEnergy);
        }

        protected abstract void save(@Unsigned long currentStored);

        protected abstract @Unsigned long getStored();
    }

    @NothingNullByDefault
    public static class DelegateSaveHandler extends SaveHandler {

        private final IEnergyContainer delegate;

        public DelegateSaveHandler(IEnergyContainer delegate) {
            super(delegate.getMaxEnergy());
            this.delegate = delegate;
        }

        @Override
        protected void save(@Unsigned long currentStored) {
            delegate.setEnergy(currentStored);
        }

        @Override
        protected @Unsigned long getStored() {
            return delegate.getEnergy();
        }
    }
}
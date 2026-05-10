package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.MathUtils;
import mekanism.common.lib.distribution.Target;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

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
    protected long accept(HANDLER handler, Void unused, long amount, TransactionContext transaction) {
        return handler.accept(amount, transaction);
    }

    public void save() {
        for (HANDLER handler : handlers) {
            handler.save();
        }
    }

    public long getStored() {
        long total = 0;
        for (HANDLER handler : handlers) {
            total = MathUtils.addClamped(total, handler.currentStored);
        }
        return total;
    }

    @NothingNullByDefault
    public abstract static class SaveHandler extends SnapshotJournal<Long> {

        private final long maxEnergy;
        protected long currentStored;

        protected SaveHandler(long maxEnergy) {
            this.maxEnergy = maxEnergy;
        }

        protected Long accept(long amount, TransactionContext transaction) {
            //TODO - 26.1: Check if amount can be zero? If so we can just skip
            // Also see if there is a case an empty type can be passed to this (namely when amount is not zero)
            long toAccept = Math.min(amount, maxEnergy - currentStored);
            if (toAccept > 0) {
                updateSnapshots(transaction);
                currentStored += amount;
            }
            return toAccept;
        }

        protected abstract void save();

        //TODO - 26.1: Re-evaluate this
        protected abstract long getStored();

        @Override
        protected Long createSnapshot() {
            return currentStored;
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            this.currentStored = snapshot;
        }
    }

    @NothingNullByDefault
    public static class DelegateSaveHandler extends SaveHandler {

        private final IEnergyContainer delegate;

        public DelegateSaveHandler(IEnergyContainer delegate) {
            super(delegate.getMaxEnergy());
            this.delegate = delegate;
        }

        @Override
        protected void save() {
            delegate.setEnergy(currentStored);
        }

        @Override
        protected long getStored() {
            return delegate.getEnergy();
        }
    }
}
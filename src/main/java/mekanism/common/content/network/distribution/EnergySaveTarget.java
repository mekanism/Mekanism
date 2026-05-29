package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.lib.distribution.Target;
import mekanism.common.lib.transaction.SimpleLongJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class EnergySaveTarget<HANDLER extends EnergySaveTarget.SaveHandler> extends Target<HANDLER, Void> {

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

    public void save(@Nullable TransactionContext transaction) {
        for (HANDLER handler : handlers) {
            handler.save(transaction);
        }
    }

    @NothingNullByDefault
    public abstract static class SaveHandler extends SimpleLongJournal {

        private final long maxEnergy;

        protected SaveHandler(long maxEnergy) {
            this.maxEnergy = maxEnergy;
        }

        protected Long accept(long amount, TransactionContext transaction) {
            //TODO - 26.1: Check if amount can be zero? If so we can just skip
            // Also see if there is a case an empty type can be passed to this (namely when amount is not zero)
            long toAccept = Math.min(amount, maxEnergy - value);
            if (toAccept > 0) {
                updateSnapshots(transaction);
                value += amount;
            }
            return toAccept;
        }

        protected abstract void save(@Nullable TransactionContext transaction);
    }

    @NothingNullByDefault
    public static class DelegateSaveHandler extends SaveHandler {

        private final IEnergyContainer delegate;

        public DelegateSaveHandler(IEnergyContainer delegate) {
            super(delegate.getCapacityAsLong());
            this.delegate = delegate;
        }

        @Override
        protected void save(@Nullable TransactionContext transaction) {
            delegate.setEnergy(value, transaction);
        }
    }
}
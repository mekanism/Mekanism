package mekanism.common.content.network.distribution;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.lib.distribution.Target;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class EnergySaveTarget extends Target<EnergySaveTarget.SaveHandler, Void> {

    public EnergySaveTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected long accept(EnergySaveTarget.SaveHandler handler, Void unused, long amount, TransactionContext transaction) {
        return handler.accept(amount, transaction);
    }

    @NothingNullByDefault
    public static class SaveHandler {

        private final IEnergyContainer delegate;

        public static SaveHandler startSaveHandling(IEnergyContainer delegate, TransactionContext transaction) {
            delegate.setEnergy(0, transaction);
            return new SaveHandler(delegate);
        }

        private SaveHandler(IEnergyContainer delegate) {
            this.delegate = delegate;
        }

        protected Long accept(long amount, TransactionContext transaction) {
            if (amount == 0) {
                //If there is nothing being accepted (I don't think this ever happens, but validate it), fail
                return 0L;
            }
            long stored = delegate.getAmountAsLong();
            long toAccept = Math.clamp(delegate.getCapacityAsLong() - stored, 0, amount);
            if (toAccept > 0) {
                delegate.setEnergy(stored + toAccept, transaction);
            }
            return toAccept;
        }
    }
}
package mekanism.common.lib.distribution.handler;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SpecificAmountLongHandler extends LongHandler {

    private long toAccept;
    private final SnapshotJournal<Long> toAcceptJournal = new SnapshotJournal<Long>() {

        @Override
        protected Long createSnapshot() {
            return toAccept;
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            toAccept =  snapshot;
        }
    };

    public SpecificAmountLongHandler(long toAccept) {
        this.toAccept = toAccept;
    }

    @Override
    public long perform(long amountOffered, TransactionContext transaction) {
        long amountToTake = accept(Math.min(amountOffered, toAccept), transaction);
        toAcceptJournal.updateSnapshots(transaction);
        toAccept -= amountToTake;
        return amountToTake;
    }
}
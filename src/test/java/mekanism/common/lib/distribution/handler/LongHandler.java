package mekanism.common.lib.distribution.handler;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Created by Thiakil on 30/04/2021.
 */
public abstract class LongHandler extends SnapshotJournal<Long> {

    private long accepted;

    protected long accept(long amount, TransactionContext transaction) {
        updateSnapshots(transaction);
        accepted += amount;
        return amount;
    }

    public long getAccepted() {
        return accepted;
    }

    public abstract long perform(long amountOffered, TransactionContext transaction);

    @Override
    protected Long createSnapshot() {
        return accepted;
    }

    @Override
    protected void revertToSnapshot(Long snapshot) {
        accepted = snapshot;
    }
}
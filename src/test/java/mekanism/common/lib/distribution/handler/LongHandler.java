package mekanism.common.lib.distribution.handler;

import mekanism.common.lib.transaction.SimpleLongJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class LongHandler extends SimpleLongJournal {

    protected long accept(long amount, TransactionContext transaction) {
        updateSnapshots(transaction);
        value += amount;
        return amount;
    }

    public long getAccepted() {
        return value;
    }

    public abstract long perform(long amountOffered, TransactionContext transaction);
}
package mekanism.common.lib.distribution.handler;

import mekanism.common.lib.transaction.SimpleLongJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SpecificAmountLongHandler extends LongHandler {

    private final SimpleLongJournal toAcceptJournal;

    public SpecificAmountLongHandler(long toAccept) {
        this.toAcceptJournal = new SimpleLongJournal(toAccept);
    }

    @Override
    public long perform(long amountOffered, TransactionContext transaction) {
        long amountToTake = accept(Math.min(amountOffered, toAcceptJournal.value), transaction);
        toAcceptJournal.updateSnapshots(transaction);
        toAcceptJournal.value -= amountToTake;
        return amountToTake;
    }
}
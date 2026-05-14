package mekanism.common.lib.distribution.handler;

import mekanism.common.lib.transaction.SimpleIntegerJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SpecificAmountIntegerHandler extends IntegerHandler {

    private final SimpleIntegerJournal toAcceptJournal;

    public SpecificAmountIntegerHandler(int toAccept) {
        this.toAcceptJournal = new SimpleIntegerJournal(toAccept);
    }

    @Override
    public int perform(int amountOffered, TransactionContext transaction) {
        int amountToTake = accept(Math.min(amountOffered, toAcceptJournal.value), transaction);
        toAcceptJournal.updateSnapshots(transaction);
        toAcceptJournal.value -= amountToTake;
        return amountToTake;
    }
}
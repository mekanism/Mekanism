package mekanism.common.lib.distribution.handler;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SpecificAmountIntegerHandler extends IntegerHandler {

    private int toAccept;
    private final SnapshotJournal<Integer> toAcceptJournal = new SnapshotJournal<Integer>() {

        @Override
        protected Integer createSnapshot() {
            return toAccept;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            toAccept =  snapshot;
        }
    };

    public SpecificAmountIntegerHandler(int toAccept) {
        this.toAccept = toAccept;
    }

    @Override
    public int perform(int amountOffered, TransactionContext transaction) {
        int amountToTake = accept(Math.min(amountOffered, toAccept), transaction);
        toAcceptJournal.updateSnapshots(transaction);
        toAccept -= amountToTake;
        return amountToTake;
    }
}
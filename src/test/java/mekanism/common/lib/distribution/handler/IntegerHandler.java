package mekanism.common.lib.distribution.handler;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Created by Thiakil on 30/04/2021.
 */
public abstract class IntegerHandler extends SnapshotJournal<Integer> {

    private int accepted;

    protected int accept(int amount, TransactionContext transaction) {
        updateSnapshots(transaction);
        accepted += amount;
        return amount;
    }

    public int getAccepted() {
        return accepted;
    }

    public abstract int perform(int amountOffered, TransactionContext transaction);

    @Override
    protected Integer createSnapshot() {
        return accepted;
    }

    @Override
    protected void revertToSnapshot(Integer snapshot) {
        accepted = snapshot;
    }
}
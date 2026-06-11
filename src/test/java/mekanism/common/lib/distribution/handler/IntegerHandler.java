package mekanism.common.lib.distribution.handler;

import mekanism.common.lib.transaction.SimpleIntegerJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class IntegerHandler extends SimpleIntegerJournal {

    protected int accept(int amount, TransactionContext transaction) {
        updateSnapshots(transaction);
        value += amount;
        return amount;
    }

    public int getAccepted() {
        return value;
    }

    public abstract int perform(int amountOffered, TransactionContext transaction);
}
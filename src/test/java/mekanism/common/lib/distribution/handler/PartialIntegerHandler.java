package mekanism.common.lib.distribution.handler;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class PartialIntegerHandler extends IntegerHandler {

    @Override
    public int perform(int amountOffered, TransactionContext transaction) {
        return accept(amountOffered / 2, transaction);
    }
}
package mekanism.common.lib.distribution.handler;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class InfiniteIntegerHandler extends IntegerHandler {

    @Override
    public int perform(int amountOffered, TransactionContext transaction) {
        return accept(amountOffered, transaction);
    }
}
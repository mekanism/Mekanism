package mekanism.common.lib.distribution.handler;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class InfiniteLongHandler extends LongHandler {

    @Override
    public long perform(long amountOffered, TransactionContext transaction) {
        return accept(amountOffered, transaction);
    }
}
package mekanism.common.lib.distribution.handler;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class PartialLongHandler extends LongHandler {

    @Override
    public long perform(long amountOffered, TransactionContext transaction) {
        return accept(amountOffered / 2, transaction);
    }
}
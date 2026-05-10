package mekanism.common.lib.distribution.target;

import com.google.common.primitives.Ints;
import mekanism.common.lib.distribution.Target;
import mekanism.common.lib.distribution.handler.IntegerHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class IntegerTarget extends Target<IntegerHandler, Void> {

    @Override
    protected long accept(IntegerHandler integerHandler, Void resource, long amount, TransactionContext transaction) {
        return integerHandler.perform(Ints.saturatedCast(amount), transaction);
    }
}
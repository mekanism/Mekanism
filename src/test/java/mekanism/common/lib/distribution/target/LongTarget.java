package mekanism.common.lib.distribution.target;

import mekanism.common.lib.distribution.Target;
import mekanism.common.lib.distribution.handler.LongHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class LongTarget extends Target<LongHandler, @Nullable Void> {

    @Override
    protected long accept(LongHandler longHandler, @Nullable Void resource, long amount, TransactionContext transaction) {
        return longHandler.perform(amount, transaction);
    }
}
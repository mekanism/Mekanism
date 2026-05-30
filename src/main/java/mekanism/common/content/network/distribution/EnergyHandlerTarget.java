package mekanism.common.content.network.distribution;

import com.google.common.primitives.Ints;
import java.util.Collection;
import mekanism.common.lib.distribution.Target;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class EnergyHandlerTarget extends Target<EnergyHandler, Void> {

    public EnergyHandlerTarget(Collection<EnergyHandler> allHandlers) {
        super(allHandlers);
    }

    public EnergyHandlerTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected long accept(EnergyHandler handler, Void unused, long amount, TransactionContext transaction) {
        return handler.insert(Ints.saturatedCast(amount), transaction);
    }
}
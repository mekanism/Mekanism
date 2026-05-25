package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.lib.distribution.Target;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class EnergyAcceptorTarget extends Target<IStrictEnergyHandler, Void> {

    public EnergyAcceptorTarget(Collection<IStrictEnergyHandler> allHandlers) {
        super(allHandlers);
    }

    public EnergyAcceptorTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected long accept(IStrictEnergyHandler handler, Void unused, long amount, TransactionContext transaction) {
        return handler.insert(amount, transaction);
    }
}
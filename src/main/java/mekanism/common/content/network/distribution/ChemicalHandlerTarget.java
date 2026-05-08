package mekanism.common.content.network.distribution;

import com.google.common.primitives.Ints;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class ChemicalHandlerTarget extends Target<ResourceHandler<ChemicalResource>, ChemicalStack> {

    public ChemicalHandlerTarget() {
    }

    public ChemicalHandlerTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected void acceptAmount(ResourceHandler<ChemicalResource> handler, SplitInfo splitInfo, ChemicalStack resource, long amount) {
        //TODO - 26.1: Make the emit system transaction aware in general, and make chemical targets be for ints?
        try (Transaction transaction = Transaction.openRoot()) {
            splitInfo.send(handler.insert(ChemicalResource.of(resource), Ints.saturatedCast(amount), transaction));
            transaction.commit();
        }
    }

    @Override
    protected long simulate(ResourceHandler<ChemicalResource> handler, ChemicalStack resource, long amount) {
        try (Transaction simulation = Transaction.openRoot()) {
            return handler.insert(ChemicalResource.of(resource), Ints.saturatedCast(amount), simulation);
        }
    }
}
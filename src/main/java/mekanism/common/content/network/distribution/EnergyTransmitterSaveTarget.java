package mekanism.common.content.network.distribution;

import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.lib.distribution.Target;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class EnergyTransmitterSaveTarget extends Target<UniversalCable.SaveShareJournal, @Nullable Void> {

    public EnergyTransmitterSaveTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected long accept(UniversalCable.SaveShareJournal handler, @Nullable Void unused, long amount, TransactionContext transaction) {
        return handler.accept(amount, transaction);
    }
}
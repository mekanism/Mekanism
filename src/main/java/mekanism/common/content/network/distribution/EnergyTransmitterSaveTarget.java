package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.network.distribution.EnergyTransmitterSaveTarget.CableSaveHandler;
import mekanism.common.content.network.transmitter.UniversalCable;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class EnergyTransmitterSaveTarget extends EnergySaveTarget<CableSaveHandler> {

    public EnergyTransmitterSaveTarget(Collection<UniversalCable> transmitters) {
        super(transmitters.stream().map(CableSaveHandler::new).toList());
    }

    @NothingNullByDefault
    public static class CableSaveHandler extends EnergySaveTarget.SaveHandler {

        private final UniversalCable transmitter;

        public CableSaveHandler(UniversalCable transmitter) {
            super(transmitter.getCapacity());
            this.transmitter = transmitter;
        }

        @Override
        protected void save(@Nullable TransactionContext transaction) {
            if (value != transmitter.lastWrite) {
                transmitter.lastWrite = value;
                transmitter.getTransmitterTile().markForSave();
            }
        }
    }
}
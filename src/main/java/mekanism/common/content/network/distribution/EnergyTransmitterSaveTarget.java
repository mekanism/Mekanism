package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.network.distribution.EnergyTransmitterSaveTarget.CableSaveHandler;
import mekanism.common.content.network.transmitter.UniversalCable;

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
        protected void save(long currentStored) {
            if (currentStored != 0L || transmitter.lastWrite != 0L) {
                transmitter.lastWrite = currentStored;
                transmitter.getTransmitterTile().markForSave();
            }
        }

        @Override
        protected long getStored() {
            return transmitter.getShare();
        }
    }
}
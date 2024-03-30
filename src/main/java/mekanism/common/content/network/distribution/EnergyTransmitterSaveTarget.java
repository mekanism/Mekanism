package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.common.content.network.distribution.EnergyTransmitterSaveTarget.CableSaveHandler;
import mekanism.common.content.network.transmitter.UniversalCable;

public class EnergyTransmitterSaveTarget extends EnergySaveTarget<CableSaveHandler> {

    public EnergyTransmitterSaveTarget(Collection<UniversalCable> transmitters) {
        super(transmitters.stream().map(CableSaveHandler::new).toList());
    }

    @NothingNullByDefault
    public static class CableSaveHandler extends SaveHandler {

        private final UniversalCable transmitter;

        public CableSaveHandler(UniversalCable transmitter) {
            super(transmitter.getCapacityAsFloatingLong());
            this.transmitter = transmitter;
        }

        @Override
        protected void save(FloatingLong currentStored) {
            if (!currentStored.isZero() || !transmitter.lastWrite.isZero()) {
                transmitter.lastWrite = currentStored;
                transmitter.getTransmitterTile().markForSave();
            }
        }

        @Override
        protected FloatingLong getStored() {
            return transmitter.getShare();
        }
    }
}
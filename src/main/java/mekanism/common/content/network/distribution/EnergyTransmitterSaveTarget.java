package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.Unsigned;
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
            super(transmitter.getCapacityAsUnsignedLong());
            this.transmitter = transmitter;
        }

        @Override
        protected void save(@Unsigned long currentStored) {
            if (currentStored != 0L || transmitter.lastWrite != 0L) {
                transmitter.lastWrite = currentStored;
                transmitter.getTransmitterTile().markForSave();
            }
        }

        @Override
        protected @Unsigned long getStored() {
            return transmitter.getShare();
        }
    }
}
package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.math.FloatingLong;
import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;

public class EnergyTransmitterSaveTarget extends Target<UniversalCable, FloatingLong, FloatingLong> {

    private FloatingLong currentStored = FloatingLong.ZERO;

    public EnergyTransmitterSaveTarget() {
    }

    public EnergyTransmitterSaveTarget(Collection<UniversalCable> allHandlers) {
        super(allHandlers);
    }

    public EnergyTransmitterSaveTarget(int expectedSize) {
        super(expectedSize);
    }

    @Override
    protected void acceptAmount(UniversalCable transmitter, SplitInfo<FloatingLong> splitInfo, FloatingLong amount) {
        amount = amount.min(transmitter.getCapacityAsFloatingLong().subtract(currentStored));
        currentStored = currentStored.plusEqual(amount);
        splitInfo.send(amount);
    }

    @Override
    protected FloatingLong simulate(UniversalCable transmitter, FloatingLong energyToSend) {
        return energyToSend.copy().min(transmitter.getCapacityAsFloatingLong().subtract(currentStored));
    }

    public void saveShare() {
        for (UniversalCable cable : handlers) {
            if (!currentStored.isZero() || !cable.lastWrite.isZero()) {
                cable.lastWrite = currentStored;
                cable.getTransmitterTile().markDirty(false);
            }
        }
    }
}
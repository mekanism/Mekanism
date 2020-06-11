package mekanism.common.content.transmitter.distribution;

import mekanism.api.math.FloatingLong;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.util.Direction;

public class EnergyTransmitterSaveTarget extends Target<TileEntityUniversalCable, FloatingLong, FloatingLong> {

    private FloatingLong currentStored = FloatingLong.ZERO;

    @Override
    protected void acceptAmount(TileEntityUniversalCable transmitter, SplitInfo<FloatingLong> splitInfo, FloatingLong amount) {
        amount = amount.min(transmitter.getCapacityAsFloatingLong().subtract(currentStored));
        currentStored = currentStored.plusEqual(amount);
        splitInfo.send(amount);
    }

    @Override
    protected FloatingLong simulate(TileEntityUniversalCable transmitter, FloatingLong energyToSend) {
        return energyToSend.copy().min(transmitter.getCapacityAsFloatingLong().subtract(currentStored));
    }

    public void saveShare(Direction handlerDirection) {
        TileEntityUniversalCable cable = handlers.get(handlerDirection);
        if (!currentStored.isZero() || !cable.lastWrite.isZero()) {
            cable.lastWrite = currentStored;
            cable.markDirty(false);
        }
    }
}
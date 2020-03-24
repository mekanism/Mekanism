package mekanism.common.base.target;

import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.base.SplitInfo;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import mekanism.common.transmitters.TransmitterImpl;
import mekanism.common.transmitters.grid.EnergyNetwork;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public class EnergyTransmitterSaveTarget extends Target<IGridTransmitter<IStrictEnergyHandler, EnergyNetwork, FloatingLong>, FloatingLong, FloatingLong> {

    private FloatingLong currentStored = FloatingLong.ZERO;

    @Override
    protected void acceptAmount(IGridTransmitter<IStrictEnergyHandler, EnergyNetwork, FloatingLong> transmitter, SplitInfo<FloatingLong> splitInfo, FloatingLong amount) {
        amount = amount.min(transmitter.getCapacityAsFloatingLong().subtract(currentStored));
        if (currentStored.isEmpty()) {
            currentStored = amount.copy();
        } else {
            currentStored.plusEqual(amount);
        }
        splitInfo.send(amount);
    }

    @Override
    protected FloatingLong simulate(IGridTransmitter<IStrictEnergyHandler, EnergyNetwork, FloatingLong> transmitter, FloatingLong energyToSend) {
        return energyToSend.copy().min(transmitter.getCapacityAsFloatingLong().subtract(currentStored));
    }

    public void saveShare(Direction handlerDirection) {
        IGridTransmitter<IStrictEnergyHandler, EnergyNetwork, FloatingLong> transmitter = handlers.get(handlerDirection);
        if (transmitter instanceof TransmitterImpl<?, ?, ?>) {
            TileEntity tile = ((TransmitterImpl<?, ?, ?>) transmitter).getTileEntity();
            if (tile instanceof TileEntityUniversalCable) {
                TileEntityUniversalCable cable = (TileEntityUniversalCable) tile;
                if (!currentStored.isEmpty() || !cable.lastWrite.isEmpty()) {
                    cable.lastWrite = currentStored;
                    cable.markDirty();
                }
            }
        }
    }
}
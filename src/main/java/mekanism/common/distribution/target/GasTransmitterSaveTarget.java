package mekanism.common.distribution.target;

import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.distribution.SplitInfo;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import mekanism.common.transmitters.TransmitterImpl;
import mekanism.common.transmitters.grid.GasNetwork;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public class GasTransmitterSaveTarget extends Target<IGridTransmitter<IGasHandler, GasNetwork, GasStack>, Long, @NonNull GasStack> {

    private GasStack currentStored = GasStack.EMPTY;

    public GasTransmitterSaveTarget(@NonNull GasStack type) {
        this.extra = type;
    }

    @Override
    protected void acceptAmount(IGridTransmitter<IGasHandler, GasNetwork, GasStack> transmitter, SplitInfo<Long> splitInfo, Long amount) {
        amount = Math.min(amount, transmitter.getCapacity() - currentStored.getAmount());
        GasStack newGas = new GasStack(extra, amount);
        if (currentStored.isEmpty()) {
            currentStored = newGas;
        } else {
            currentStored.grow(amount);
        }
        splitInfo.send(amount);
    }

    @Override
    protected Long simulate(IGridTransmitter<IGasHandler, GasNetwork, GasStack> transmitter, @NonNull GasStack gasStack) {
        if (!currentStored.isEmpty() && !currentStored.isTypeEqual(gasStack)) {
            return 0L;
        }
        return Math.min(gasStack.getAmount(), transmitter.getCapacity() - currentStored.getAmount());
    }

    public void saveShare(Direction handlerDirection) {
        IGridTransmitter<IGasHandler, GasNetwork, GasStack> transmitter = handlers.get(handlerDirection);
        if (transmitter instanceof TransmitterImpl<?, ?, ?>) {
            TileEntity tile = ((TransmitterImpl<?, ?, ?>) transmitter).getTileEntity();
            if (tile instanceof TileEntityPressurizedTube) {
                TileEntityPressurizedTube tube = (TileEntityPressurizedTube) tile;
                if (currentStored.isEmpty() != tube.lastWrite.isEmpty() || (!currentStored.isEmpty() && !currentStored.isStackIdentical(tube.lastWrite))) {
                    tube.lastWrite = currentStored;
                    tube.markDirty(false);
                }
            }
        }
    }
}
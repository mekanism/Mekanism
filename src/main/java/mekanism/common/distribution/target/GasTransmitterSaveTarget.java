package mekanism.common.distribution.target;

import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.common.distribution.SplitInfo;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import net.minecraft.util.Direction;

public class GasTransmitterSaveTarget extends Target<TileEntityPressurizedTube, Long, @NonNull GasStack> {

    private GasStack currentStored = GasStack.EMPTY;

    public GasTransmitterSaveTarget(@Nonnull GasStack type) {
        this.extra = type;
    }

    @Override
    protected void acceptAmount(TileEntityPressurizedTube transmitter, SplitInfo<Long> splitInfo, Long amount) {
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
    protected Long simulate(TileEntityPressurizedTube transmitter, @Nonnull GasStack gasStack) {
        if (!currentStored.isEmpty() && !currentStored.isTypeEqual(gasStack)) {
            return 0L;
        }
        return Math.min(gasStack.getAmount(), transmitter.getCapacity() - currentStored.getAmount());
    }

    public void saveShare(Direction handlerDirection) {
        TileEntityPressurizedTube tube = handlers.get(handlerDirection);
        if (currentStored.isEmpty() != tube.saveShare.isEmpty() || (!currentStored.isEmpty() && !currentStored.isStackIdentical(tube.saveShare))) {
            tube.saveShare = currentStored;
            tube.markDirty(false);
        }
    }
}
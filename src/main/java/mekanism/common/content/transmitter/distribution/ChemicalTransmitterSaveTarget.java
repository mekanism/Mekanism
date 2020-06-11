package mekanism.common.content.transmitter.distribution;

import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.util.Direction;

public class ChemicalTransmitterSaveTarget<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends
      Target<TileEntityPressurizedTube, Long, @NonNull STACK> {

    private STACK currentStored;

    public ChemicalTransmitterSaveTarget(@Nonnull STACK empty, @Nonnull STACK type) {
        this.currentStored = empty;
        this.extra = type;
    }

    @Override
    protected void acceptAmount(TileEntityPressurizedTube transmitter, SplitInfo<Long> splitInfo, Long amount) {
        amount = Math.min(amount, transmitter.getCapacity() - currentStored.getAmount());
        STACK newChemical = ChemicalUtil.copyWithAmount(extra, amount);
        if (currentStored.isEmpty()) {
            currentStored = newChemical;
        } else {
            currentStored.grow(amount);
        }
        splitInfo.send(amount);
    }

    @Override
    protected Long simulate(TileEntityPressurizedTube transmitter, @Nonnull STACK chemicalStack) {
        if (!currentStored.isEmpty() && !currentStored.isTypeEqual(chemicalStack)) {
            return 0L;
        }
        return Math.min(chemicalStack.getAmount(), transmitter.getCapacity() - currentStored.getAmount());
    }

    public void saveShare(Direction handlerDirection) {
        TileEntityPressurizedTube tube = handlers.get(handlerDirection);
        if (currentStored.isEmpty() != tube.saveShare.isEmpty() || (!currentStored.isEmpty() && !currentStored.isStackIdentical(tube.saveShare))) {
            tube.saveShare = currentStored;
            tube.markDirty(false);
        }
    }
}
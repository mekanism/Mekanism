package mekanism.common.content.network.distribution;

import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.content.network.transmitter.chemical.PressurizedTube;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.util.Direction;

public class ChemicalTransmitterSaveTarget<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      TUBE extends PressurizedTube<CHEMICAL, STACK, ?, ?, ?, TUBE>> extends Target<TUBE, Long, @NonNull STACK> {

    private STACK currentStored;

    public ChemicalTransmitterSaveTarget(@Nonnull STACK empty, @Nonnull STACK type) {
        this.currentStored = empty;
        this.extra = type;
    }

    @Override
    protected void acceptAmount(TUBE transmitter, SplitInfo<Long> splitInfo, Long amount) {
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
    protected Long simulate(TUBE transmitter, @Nonnull STACK chemicalStack) {
        if (!currentStored.isEmpty() && !currentStored.isTypeEqual(chemicalStack)) {
            return 0L;
        }
        return Math.min(chemicalStack.getAmount(), transmitter.getCapacity() - currentStored.getAmount());
    }

    public void saveShare(Direction handlerDirection) {
        TUBE tube = handlers.get(handlerDirection);
        if (currentStored.isEmpty() != tube.saveShare.isEmpty() || (!currentStored.isEmpty() && !currentStored.isStackIdentical(tube.saveShare))) {
            tube.saveShare = currentStored;
            tube.getTransmitterTile().markDirty(false);
        }
    }
}
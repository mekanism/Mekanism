package mekanism.common.content.network.distribution;

import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.util.Direction;

public class BoxedChemicalTransmitterSaveTarget<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
      extends Target<BoxedPressurizedTube, Long, @NonNull STACK> {

    private STACK currentStored;

    public BoxedChemicalTransmitterSaveTarget(@Nonnull STACK empty, @Nonnull STACK type) {
        this.currentStored = empty;
        this.extra = type;
    }

    @Override
    protected void acceptAmount(BoxedPressurizedTube transmitter, SplitInfo<Long> splitInfo, Long amount) {
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
    protected Long simulate(BoxedPressurizedTube transmitter, @Nonnull STACK chemicalStack) {
        if (!currentStored.isEmpty() && !currentStored.isTypeEqual(chemicalStack)) {
            return 0L;
        }
        return Math.min(chemicalStack.getAmount(), transmitter.getCapacity() - currentStored.getAmount());
    }

    public void saveShare(Direction handlerDirection) {
        BoxedPressurizedTube tube = handlers.get(handlerDirection);
        boolean shouldSave = false;
        if (currentStored.isEmpty() != tube.saveShare.isEmpty()) {
            shouldSave = true;
        } else if (!currentStored.isEmpty()) {
            ChemicalType chemicalType = ChemicalType.getTypeFor(currentStored);
            shouldSave = chemicalType != tube.saveShare.getChemicalType() || !currentStored.isStackIdentical((STACK) tube.saveShare.getChemicalStack());
        }
        if (shouldSave) {
            tube.saveShare = currentStored.isEmpty() ? BoxedChemicalStack.EMPTY : BoxedChemicalStack.box(currentStored);
            tube.getTransmitterTile().markDirty(false);
        }
    }
}
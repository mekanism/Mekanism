package mekanism.common.content.network.distribution;

import java.util.Collection;
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

public class BoxedChemicalTransmitterSaveTarget<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
      extends Target<BoxedChemicalTransmitterSaveTarget<CHEMICAL, STACK>.SaveHandler, Long, @NonNull STACK> {

    public BoxedChemicalTransmitterSaveTarget(@Nonnull STACK empty, @Nonnull STACK type, Collection<BoxedPressurizedTube> transmitters) {
        super(transmitters.size());
        this.extra = type;
        transmitters.forEach(transmitter -> addHandler(new SaveHandler(empty, transmitter)));
    }

    @Override
    protected void acceptAmount(BoxedChemicalTransmitterSaveTarget<CHEMICAL, STACK>.SaveHandler handler, SplitInfo<Long> splitInfo, Long amount) {
        handler.acceptAmount(splitInfo, amount);
    }

    @Override
    protected Long simulate(BoxedChemicalTransmitterSaveTarget<CHEMICAL, STACK>.SaveHandler handler, @Nonnull STACK chemicalStack) {
        return handler.simulate(chemicalStack);
    }

    public void saveShare() {
        for (SaveHandler handler : handlers) {
            handler.saveShare();
        }
    }

    public class SaveHandler {

        private STACK currentStored;
        private final BoxedPressurizedTube transmitter;

        public SaveHandler(@Nonnull STACK empty, BoxedPressurizedTube transmitter) {
            this.currentStored = empty;
            this.transmitter = transmitter;
        }

        protected void acceptAmount(SplitInfo<Long> splitInfo, Long amount) {
            amount = Math.min(amount, transmitter.getCapacity() - currentStored.getAmount());
            if (currentStored.isEmpty()) {
                currentStored = ChemicalUtil.copyWithAmount(extra, amount);
            } else {
                currentStored.grow(amount);
            }
            splitInfo.send(amount);
        }

        protected Long simulate(@Nonnull STACK chemicalStack) {
            if (!currentStored.isEmpty() && !currentStored.isTypeEqual(chemicalStack)) {
                return 0L;
            }
            return Math.min(chemicalStack.getAmount(), transmitter.getCapacity() - currentStored.getAmount());
        }

        @SuppressWarnings("unchecked")
        protected void saveShare() {
            boolean shouldSave = false;
            if (currentStored.isEmpty() != transmitter.saveShare.isEmpty()) {
                shouldSave = true;
            } else if (!currentStored.isEmpty()) {
                ChemicalType chemicalType = ChemicalType.getTypeFor(currentStored);
                shouldSave = chemicalType != transmitter.saveShare.getChemicalType() || !currentStored.isStackIdentical((STACK) transmitter.saveShare.getChemicalStack());
            }
            if (shouldSave) {
                transmitter.saveShare = currentStored.isEmpty() ? BoxedChemicalStack.EMPTY : BoxedChemicalStack.box(currentStored);
                transmitter.getTransmitterTile().markForSave();
            }
        }
    }
}
package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;

public class ChemicalTransmitterSaveTarget extends Target<ChemicalTransmitterSaveTarget.SaveHandler, Long, ChemicalStack> {

    public ChemicalTransmitterSaveTarget(ChemicalStack type, Collection<BoxedPressurizedTube> transmitters) {
        super(transmitters.size());
        this.extra = type;
        for (BoxedPressurizedTube transmitter : transmitters) {
            addHandler(new SaveHandler(transmitter));
        }
    }

    @Override
    protected void acceptAmount(ChemicalTransmitterSaveTarget.SaveHandler handler, SplitInfo<Long> splitInfo, Long amount) {
        handler.acceptAmount(splitInfo, amount);
    }

    @Override
    protected Long simulate(ChemicalTransmitterSaveTarget.SaveHandler handler, ChemicalStack chemicalStack) {
        return handler.simulate(chemicalStack);
    }

    public void saveShare() {
        for (SaveHandler handler : handlers) {
            handler.saveShare();
        }
    }

    public class SaveHandler {

        private ChemicalStack currentStored;
        private final BoxedPressurizedTube transmitter;

        public SaveHandler(BoxedPressurizedTube transmitter) {
            this.currentStored = ChemicalStack.EMPTY;
            this.transmitter = transmitter;
        }

        protected void acceptAmount(SplitInfo<Long> splitInfo, Long amount) {
            amount = Math.min(amount, transmitter.getCapacity() - currentStored.getAmount());
            if (currentStored.isEmpty()) {
                currentStored = extra.copyWithAmount(amount);
            } else {
                currentStored.grow(amount);
            }
            splitInfo.send(amount);
        }

        protected Long simulate(ChemicalStack chemicalStack) {
            if (!currentStored.isEmpty() && !ChemicalStack.isSameChemical(currentStored, chemicalStack)) {
                return 0L;
            }
            return Math.min(chemicalStack.getAmount(), transmitter.getCapacity() - currentStored.getAmount());
        }

        protected void saveShare() {
            boolean shouldSave = false;
            if (currentStored.isEmpty() != transmitter.saveShare.isEmpty()) {
                shouldSave = true;
            } else if (!currentStored.isEmpty()) {
                shouldSave = !currentStored.equals(transmitter.saveShare);
            }
            if (shouldSave) {
                transmitter.saveShare = currentStored.copy();
                transmitter.getTransmitterTile().markForSave();
            }
        }
    }
}
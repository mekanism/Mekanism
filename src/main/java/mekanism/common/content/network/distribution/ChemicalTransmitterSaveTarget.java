package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.content.network.transmitter.PressurizedTube;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;

public class ChemicalTransmitterSaveTarget extends Target<ChemicalTransmitterSaveTarget.SaveHandler, ChemicalStack> {

    public ChemicalTransmitterSaveTarget(Collection<PressurizedTube> transmitters) {
        super(transmitters.size());
        for (PressurizedTube transmitter : transmitters) {
            addHandler(new SaveHandler(transmitter));
        }
    }

    @Override
    protected void acceptAmount(SaveHandler handler, SplitInfo splitInfo, ChemicalStack resource, long amount) {
        handler.acceptAmount(splitInfo, resource, amount);
    }

    @Override
    protected long simulate(SaveHandler handler, ChemicalStack resource, long amount) {
        return handler.simulate(resource.copyWithAmount(amount));
    }

    public void saveShare() {
        for (SaveHandler handler : handlers) {
            handler.saveShare();
        }
    }

    //todo implement this on the transmitter with slightly different names?
    public static class SaveHandler {

        private ChemicalStack currentStored;
        private final PressurizedTube transmitter;

        public SaveHandler(PressurizedTube transmitter) {
            this.currentStored = ChemicalStack.EMPTY;
            this.transmitter = transmitter;
        }

        protected void acceptAmount(SplitInfo splitInfo, ChemicalStack resource, long amount) {
            amount = Math.min(amount, transmitter.getCapacity() - currentStored.getAmount());
            if (currentStored.isEmpty()) {
                currentStored = resource.copyWithAmount(amount);
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
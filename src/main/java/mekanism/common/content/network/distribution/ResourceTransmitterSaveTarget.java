package mekanism.common.content.network.distribution;

import java.util.Collection;
import mekanism.api.container.LargeResourceStack;
import mekanism.common.content.network.transmitter.BufferedResourceTransmitter;
import mekanism.common.lib.distribution.Target;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;

public class ResourceTransmitterSaveTarget<RESOURCE extends Resource, TRANSMITTER extends BufferedResourceTransmitter<RESOURCE, ?, ?, ?>> extends
      Target<ResourceTransmitterSaveTarget.SaveHandler<RESOURCE, TRANSMITTER>, RESOURCE> {

    public ResourceTransmitterSaveTarget(Collection<TRANSMITTER> transmitters) {
        super(transmitters.size());
        for (TRANSMITTER transmitter : transmitters) {
            addHandler(new SaveHandler<>(transmitter));
        }
    }

    @Override
    protected long accept(SaveHandler<RESOURCE, TRANSMITTER> handler, RESOURCE resource, long amount, TransactionContext transaction) {
        return handler.accept(resource, amount, transaction);
    }

    public void saveShare() {
        for (SaveHandler<RESOURCE, TRANSMITTER> handler : handlers) {
            handler.saveShare();
        }
    }

    //todo implement this on the transmitter with slightly different names?
    public static class SaveHandler<RESOURCE extends Resource, TRANSMITTER extends BufferedResourceTransmitter<RESOURCE, ?, ?, ?>> extends SnapshotJournal<LargeResourceStack<RESOURCE>> {

        private final TRANSMITTER transmitter;
        private final long transmitterCapacity;
        private RESOURCE currentType;
        private long currentStored;

        public SaveHandler(TRANSMITTER transmitter) {
            this.transmitter = transmitter;
            this.currentType = this.transmitter.getEmptyResourceStack().resource();
            this.transmitterCapacity = this.transmitter.getCapacity();
        }

        protected Long accept(RESOURCE type, long amount, TransactionContext transaction) {
            //TODO - 26.1: Check if amount can be zero? If so we can just skip
            // Also see if there is a case an empty type can be passed to this (namely when amount is not zero)
            if (!currentType.isEmpty() && !currentType.equals(type)) {
                //Type doesn't match
                return 0L;
            }
            long toAccept = Math.min(amount, transmitterCapacity - currentStored);
            if (toAccept > 0) {
                updateSnapshots(transaction);
                currentType = type;
                currentStored += amount;
            }
            return toAccept;
        }

        protected void saveShare() {
            RESOURCE saveType = transmitter.getCurrentSaveType();
            boolean shouldSave = false;
            if (currentType.isEmpty() != saveType.isEmpty()) {
                //Empty state changed
                shouldSave = true;
            } else if (!currentType.isEmpty()) {
                //Mark it for save if the type changed, or the amount changed
                shouldSave = !currentType.equals(saveType) || transmitter.getCurrentSaveAmount() != currentStored;
            }
            if (shouldSave) {
                transmitter.setSaveShare(new LargeResourceStack<>(currentType, currentStored));
            }
        }

        @Override
        protected LargeResourceStack<RESOURCE> createSnapshot() {
            return new LargeResourceStack<>(currentType, currentStored);
        }

        @Override
        protected void revertToSnapshot(@NonNull LargeResourceStack<RESOURCE> snapshot) {
            this.currentType = snapshot.resource();
            this.currentStored = snapshot.amount();
        }
    }
}
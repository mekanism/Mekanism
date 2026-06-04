package mekanism.common.lib;

import java.util.function.LongSupplier;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class LastEnergyTracker extends SnapshotJournal<Long> {

    private final LongSupplier gameTimeSupplier;
    private long lastEnergyReceived;
    private long currentEnergyReceived;
    private long currentGameTime;

    public LastEnergyTracker(LongSupplier gameTimeSupplier) {
        this.gameTimeSupplier = gameTimeSupplier;
    }

    public void tickChanged() {
        tickChanged(gameTimeSupplier.getAsLong());
    }

    private void tickChanged(long gameTime) {
        lastEnergyReceived = currentEnergyReceived;
        currentGameTime = gameTime;
        currentEnergyReceived = 0;
    }

    public void received(long amount, TransactionContext transaction) {
        long gameTime = gameTimeSupplier.getAsLong();
        if (currentGameTime != gameTime) {
            //If the tick is different from what the last cached tick was, we need to force mark that the tick changed before doing any transaction based handling
            tickChanged(gameTime);
        }
        updateSnapshots(transaction);
        currentEnergyReceived += amount;
    }

    public long getLastEnergyReceived() {
        return lastEnergyReceived;
    }

    /**
     * @apiNote For use in syncing to client
     */
    public void setLastEnergyReceived(long lastEnergyReceived) {
        this.lastEnergyReceived = lastEnergyReceived;
    }

    @Override
    protected Long createSnapshot() {
        return currentEnergyReceived;
    }

    @Override
    protected void revertToSnapshot(Long snapshot) {
        currentEnergyReceived = snapshot;
    }
}
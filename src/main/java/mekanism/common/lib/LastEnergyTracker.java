package mekanism.common.lib;

import java.util.function.LongSupplier;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class LastEnergyTracker extends SnapshotJournal<Long> {

    private final LongSupplier gameTimeSupplier;
    private long lastEnergyReceived = 0L;
    private long currentEnergyReceived = 0L;
    private long currentGameTime;

    public LastEnergyTracker(LongSupplier gameTimeSupplier) {
        this.gameTimeSupplier = gameTimeSupplier;
    }

    public void tickChanged() {
        lastEnergyReceived = currentEnergyReceived;
        currentGameTime = gameTimeSupplier.getAsLong();
        //TODO - 26.1: Should we be setting this to zero like this?
        currentEnergyReceived = 0;
    }

    //TODO - 26.1: Test this works correctly
    public void received(long amount, TransactionContext transaction) {
        long gameTime = gameTimeSupplier.getAsLong();
        if (currentGameTime == gameTime) {
            //If we are doing the current tick then increment it
            currentEnergyReceived += amount;
        } else {
            //If we aren't then store it in the amount for the last tick
            // update what the current game time is and set the current energy to the amount we received
            //TODO - 26.1: Should we be updating this in other places, or doing anything on root commit?
            lastEnergyReceived = currentEnergyReceived;
            currentGameTime = gameTime;
            //Set it to zero, and then update snapshots to capture the fact this is for a new tick
            currentEnergyReceived = 0;
            updateSnapshots(transaction);
            currentEnergyReceived = amount;
        }
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
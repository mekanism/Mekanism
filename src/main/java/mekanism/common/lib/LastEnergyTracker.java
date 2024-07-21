package mekanism.common.lib;

public class LastEnergyTracker {

    private long lastEnergyReceived = 0L;
    private long currentEnergyReceived = 0L;
    private long currentGameTime;

    public void received(long gameTime, long amount) {
        if (currentGameTime == gameTime) {
            //If we are doing the current tick then increment it
            currentEnergyReceived += amount;
        } else {
            //If we aren't then store it in the amount for the last tick
            // update what the current game time is and set the current energy to the amount we received
            lastEnergyReceived = currentEnergyReceived;
            currentGameTime = gameTime;
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
}
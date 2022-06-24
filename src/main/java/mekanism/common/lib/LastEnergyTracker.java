package mekanism.common.lib;

import mekanism.api.math.FloatingLong;

public class LastEnergyTracker {

    private FloatingLong lastEnergyReceived = FloatingLong.ZERO;
    private FloatingLong currentEnergyReceived = FloatingLong.ZERO;
    private long currentGameTime;

    public void received(long gameTime, FloatingLong amount) {
        if (currentGameTime == gameTime) {
            //If we are doing the current tick then increment it
            currentEnergyReceived = currentEnergyReceived.plusEqual(amount);
        } else {
            //If we aren't then store it in the amount for the last tick
            // update what the current game time is and set the current energy to the amount we received
            lastEnergyReceived = currentEnergyReceived;
            currentGameTime = gameTime;
            currentEnergyReceived = amount.copy();
        }
    }

    public FloatingLong getLastEnergyReceived() {
        return lastEnergyReceived;
    }

    /**
     * @apiNote For use in syncing to client
     */
    public void setLastEnergyReceived(FloatingLong lastEnergyReceived) {
        this.lastEnergyReceived = lastEnergyReceived;
    }
}